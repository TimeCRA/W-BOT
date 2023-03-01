package top.lsyweb.qqbot.service.client;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zhuangxv.bot.core.Bot;
import com.zhuangxv.bot.core.BotFactory;
import com.zhuangxv.bot.core.Group;
import com.zhuangxv.bot.core.Member;
import com.zhuangxv.bot.message.Message;
import com.zhuangxv.bot.message.MessageChain;
import com.zhuangxv.bot.message.support.ImageMessage;
import com.zhuangxv.bot.message.support.ReplyMessage;
import com.zhuangxv.bot.scheduled.FlushCacheScheduled;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import top.lsyweb.qqbot.controller.oms.HealthController;
import top.lsyweb.qqbot.dto.KeyValueDto;
import top.lsyweb.qqbot.dto.VariablePool;
import top.lsyweb.qqbot.entity.GroupInfo;
import top.lsyweb.qqbot.entity.MemberInfo;
import top.lsyweb.qqbot.entity.ValueInfo;
import top.lsyweb.qqbot.exception.ServiceException;
import top.lsyweb.qqbot.service.GroupService;
import top.lsyweb.qqbot.service.MemberService;
import top.lsyweb.qqbot.util.*;
import top.lsyweb.qqbot.util.config.SystemConfigPool;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
public class GroupMessageServiceImpl implements GroupMessageService {
    private RedisUtils redisUtils;
	private VariablePool variablePool;
	private VariablePoolService variablePoolService;
	private MemberService memberService;
	private SpecialService specialService;
	private GroupService groupService;
	private SystemConfigPool pool;
	private HealthController healthController;
	private FlushCacheScheduled flushCacheScheduled;

	@Autowired
	public GroupMessageServiceImpl(RedisUtils redisUtils, VariablePool variablePool,
								   VariablePoolService variablePoolService, MemberService memberService,
								   SpecialService specialService, GroupService groupService, SystemConfigPool pool,
								   HealthController healthController, FlushCacheScheduled flushCacheScheduled) {
		this.redisUtils = redisUtils;
		this.variablePool = variablePool;
		this.variablePoolService = variablePoolService;
		this.memberService = memberService;
		this.specialService = specialService;
		this.groupService = groupService;
		this.pool = pool;
		this.healthController = healthController;
		this.flushCacheScheduled = flushCacheScheduled;
	}

	/**
	 * 接受信息主要入口
	 * @param msg
	 * @param group
	 * @param member
	 * @param messageChain
	 * @param messageId
	 */
    @Override
    public void accept(String msg, Group group, Member member, MessageChain messageChain, int messageId) {
    	log.info("群聊{}({})接收到用户{}({})发出的消息：{}, 消息id：{}",
				 group.getGroupName(), group.getGroupId(), member.getNickname(), member.getUserId(), msg, messageId);


		/**
		 * 该流程必须放在头部
		 * 群聊开关控制
		 * 只有群主和管理员有权限
		 */
		if (getNowRole(member) >= ConstantPool.MEMBER_AUTH_GROUP_ADMIN
				&& (Pattern.matches(ConstantPool.GROUP_TURN_REGEX, msg))) {
			specialService.groupTurn(group, member, msg);
			return;
		}

        /**
         * 群聊、角色状态检查
         */
        if (!statusCheck(group.getGroupId(), member.getUserId())) {
        	return;
		}

		/**
		 * 判断是否包含艾特我
		 * 消息内容排除艾特我，然后去掉收尾空格换行
		 */
        boolean isReferenced = false;
        String content = msg.trim();
		for (String name : pool.NAMES) {
			if (msg.startsWith(name)) {
				isReferenced = true;
				content = msg.replaceFirst(name, "");
				break;
			}
		}

		/**
		 * 根据qq号获取用户信息
		 * 如未获取到用户信息，则创建用户
		 */
		MemberInfo memberInfo = getPoolMemberInfo(member);

		/**
		 * 进入图像匹配
		 */
		String ocrContent = imageKeyMatch(messageChain, content, isReferenced, memberInfo, group, messageId, member);
		if (ocrContent == null) {
			// 图像匹配已成功处理，直接返回
			return;
		} else {
			content = ocrContent;
		}

		/**
		 * 进入常规关键字匹配
		 */
		if (commonKeyMatch(content, isReferenced, memberInfo, group, messageId, member)) {
			return;
		}

		/**
		 * 进入正则关键字匹配
		 */
		if (regularKeyMatch(content, isReferenced, memberInfo, group, messageId, member)) {
			return;
		}

	}

	/**
	 * 图像处理
	 * 返回空字符串，代表这一步已处理，不需要后续常规和正则匹配。
	 * 否则，返回ocr识别的字符串给常规/正则处理。
	 * @param messageChain
	 * @param content
	 * @param isReferenced
	 * @param member
	 * @param group
	 * @return
	 */
	private String imageKeyMatch(MessageChain messageChain, String content, boolean isReferenced, MemberInfo member,
								 Group group, int messageId, Member originMember) {
		for (Message message : messageChain) {
			if (message instanceof ImageMessage) {
				// 图片远程路径
				String url = ((ImageMessage) message).getUrl();
				// 获取当前图像的code
				long sourceCode = ImageSimilarUtil.perceptualHashSimilarity(url);

				// 统计图片code
				// statisticalCode(sourceCode, group, (ImageMessage) message);

				// 与库里的所有图像进行比对
				List<KeyValueDto> imageKeyList = variablePool.getImageKeyList();
				for (KeyValueDto imageInfo : imageKeyList) {
					if (ImageSimilarUtil.isSimilar(sourceCode, Long.parseLong(imageInfo.getKey()),
												   imageInfo.getPrecision())) {

						/**
						 * 过滤规则匹配
						 */
						if (filterRuleMatch(member, group, imageInfo, isReferenced, originMember)) {
							return null;
						}

						/**
						 * 常规过滤已经完成
						 * 直接进入特殊类型处理
						 */
						if (imageInfo.getSpecial().equals(1)) {
							/**
							 * 触发频次统计
							 */
							acceptMessage(member);

							if (pool.OPEN_RECRUITMENT_IDS.contains(imageInfo.getId())) {
								// 公开招募ocr
								specialService.ocrOpenRecruitment(group, member, imageInfo, url);
								return null;
							}
						}

						/**
						 * 好感度过滤
						 */
						List<ValueInfo> favalList = favorabilityFilter(member, imageInfo);

						/**
						 * 过滤规则匹配
						 * 返回过滤后的value集合
						 */
						favalList = valueFilter(member, favalList);

						// 过滤后的回复列表为空
						if (CollectionUtils.isEmpty(favalList)) {
							if (StringUtils.isBlank(imageInfo.getOcrContent())) {
								// 如果图像ocr内容为空，说明没有预设识别内容
								// 直接返回已处理
								return null;
							} else {
								// 如果图像文字内容不为空，将文字内容返回，给后续判断
								return imageInfo.getOcrContent();
							}
						}

						/**
						 * 构造并发送消息
						 */
						sendMessage(member, group, favalList);

						/**
						 * 触发频次统计
						 */
						acceptMessage(member);

						return null;
					}
				}
			}
		}

		// 如果MessageChain里没有任何图片信息，直接返回原始content
		return content;
	}

	/**
	 * 统计图片信息
	 * @param sourceCode
	 * @param group
	 */
//	private void statisticalCode(long sourceCode, Group group, ImageMessage message) {
//		Map<Long, Map<Long, ImageCodeDto>> imageCodeMap = variablePool.getImageCodeMap();
//		// 如果不存在这个群号，则新建一个
//		imageCodeMap.computeIfAbsent(group.getGroupId(), k -> new HashMap<>());
//
//		Map<Long, ImageCodeDto> dtoMap = imageCodeMap.get(group.getGroupId());
//		dtoMap.computeIfAbsent(sourceCode, k -> new ImageCodeDto(message.getUrl(), sourceCode, 0));
//
//		// 获取图片Dto
//		ImageCodeDto imageCodeDto = dtoMap.get(sourceCode);
//		imageCodeDto.setCount(imageCodeDto.getCount() + 1);
//	}

	/**
	 * 根据qq号获取用户信息
	 * 如未获取到用户信息，则创建用户
	 *
	 * 这里保证memberInfo是从变量池取的
	 */
	private MemberInfo getPoolMemberInfo(Member member) {
		MemberInfo findMember = variablePool.getMemberInfoMap().get(member.getUserId());
		if (findMember == null) {
			// 如果没找到用户对象，则写入一个用户
			findMember = new MemberInfo();
			findMember.setMemberId(member.getUserId());
			findMember.setType(ConstantPool.GROUP_FILTER_NONE);
			findMember.setFavorability(0);
			findMember.setMemberNickname(member.getNickname());
			findMember.setTouchTicket(0);
			findMember.setRecruitTicket(0);
			findMember.setCreateTime(new Date());
			findMember.setUpdateTime(new Date());
			memberService.save(findMember);
			variablePool.getMemberInfoMap().put(findMember.getMemberId(), findMember);
			log.info("新增角色信息name:{} id:{}", member.getNickname(), member.getUserId());
			// variablePoolService.refreshMemberInfo();
			// findMember = variablePool.getMemberInfoMap().get(member.getUserId());
		}
		return findMember;
	}



	/**
	 * 正则关键字遍历
	 * @param content
	 */
	private boolean regularKeyMatch(String content, boolean isReferenced, MemberInfo member,
									Group group, int messageId, Member originMember) {
		List<KeyValueDto> regexKeyList = variablePool.getRegexKeyList();
		for (KeyValueDto valueDto : regexKeyList) {
			// 如果满足正则匹配规则
			if (Pattern.matches(valueDto.getKey(), content)) {
				/**
				 * 触发频次统计
				 */
				acceptMessage(member);
				// log.info("消息“{}”满足正则“{}”匹配", content, valueDto.getKey());

				/**
				 * 过滤规则匹配
				 */
				if (filterRuleMatch(member, group, valueDto, isReferenced, originMember)) {
					return false;
				}

				/**
				 * 常规过滤已经完成
				 * 直接进入特殊类型处理
				 */
				if (valueDto.getSpecial().equals(1)) {
					// 需要特殊处理
					if (valueDto.getId() == 5) {
						// 公开招募
						specialService.openRecruitment(group, member, valueDto, content);
						return true;
					} else if (valueDto.getId() == 6) {
						// 模拟寻访
						specialService.simulationRecruit(group, member, valueDto, content);
						return true;
					} else if (valueDto.getId() == 27) {
						// 设置中转目标
						specialService.setTransitTarget(valueDto, content);
						return true;
					} else if (valueDto.getId() == 28) {
						// 骰数
						specialService.randomNumber(group, member, valueDto, content);
						return true;
					} else if (valueDto.getId() == 42) {
						// 签到
						specialService.signIn(group, member, valueDto, content);
						return true;
					} else if (valueDto.getId() == 35) {
						// 查询信赖值
						specialService.getFavorability(group, member, valueDto, content);
						return true;
					} else if (valueDto.getId() == 44) {
						// 查询库存
						specialService.getRepository(group, member, valueDto, content);
						return true;
					} else if (valueDto.getId() == 26) {
						specialService.lolicon(group, member, valueDto, content);
						return true;
					} else if (valueDto.getId() == 260) {
						specialService.yugiohSearch(group, member, valueDto, content);
						return true;
					} else if (valueDto.getId() == 314) {
						specialService.chatPrompt(group, member, valueDto, content, originMember);
						return true;
					} else if (valueDto.getId() == 1000) {
						specialService.gptChat(group, member, valueDto, content, messageId);
						return true;
					} else if (valueDto.getId() == 315) {
						specialService.innerRefresh(group, member, valueDto, content);
					}
				}
				return regexProcessDeal(member, group, valueDto);

			}
		}
		return true;
	}

	public boolean regexProcessDeal(MemberInfo member, Group group, KeyValueDto valueDto) {
		/**
		 * 进入好感度处理
		 */
		if (pool.ADD_FAVORABILITY_LIST.contains(valueDto.getId())) {
			if (!favorabilityDeal(group, member)) {
				// 摸摸券不足时，回返回false
				return false;
			}
		}

		/**
		 * 好感度过滤
		 */
		List<ValueInfo> favalList = favorabilityFilter(member, valueDto);

		/**
		 * 过滤规则匹配
		 * 返回过滤后的value集合
		 */
		favalList = valueFilter(member, favalList);

		// 如果好感度回复列表为空，也不满足特殊处理，则直接返回
		if (CollectionUtils.isEmpty(favalList)) {
			return false;
		}

		/**
		 * 构造并发送消息
		 */
		sendMessage(member, group, favalList);

		return true;
	}

	/**
	 * 触发频次统计
	 * @param member
	 */
	private void acceptMessage(MemberInfo member) {
		String key = ConstantPool.TRIGGER_TIME_KEY + member.getMemberId();
		if (redisUtils.get(key) != null) {
			int val = (Integer) redisUtils.get(key);
			/**
			 * 超过10次调用，就新增一点信赖值
			 */
			if (val > pool.TRIGGER_TIME) {
				member.setFavorability(PathUtil.updateFavorability(member.getFavorability(), 1));
				// 重新置空
				redisUtils.set(key, 0);
			} else {
				redisUtils.set(key, val + 1);
			}
		} else {
			redisUtils.set(key, 1);
		}
	}

	/**
	 * 更新用户好感度
	 * 需要摸摸券足够的情况下
	 * @param member
	 */
	private boolean favorabilityDeal(Group group, MemberInfo member) {
		if (member.getTouchTicket() < 1) {
			MessageUtil.sendTextMessage(group, "摸摸券不足哦~ 目前剩余" + member.getTouchTicket() +
					"张，每日首次签到可领取摸摸券，改天再来吧！");
			return false;
		}

		/**
		 * 更新变量池中的属性，每次更新一点
		 */
		member.setTouchTicket(member.getTouchTicket() - 1);
		member.setFavorability(PathUtil.updateFavorability(member.getFavorability(), 1));
		return true;
	}


	/**
	 * 常规关键字匹配
	 * 1. key匹配
	 * 2. @匹配
	 * 3. 过滤规则匹配
	 * 4. 好感度匹配
	 * 以上规则匹配全部通过，即构造消息并发送
	 * @param content
	 */
	private boolean commonKeyMatch(String content, boolean isReferenced, MemberInfo member, Group group, int messageId, Member originMember) {
		Map<String, KeyValueDto> keyMap = variablePool.getKeyMap();

		// 1. key匹配
		KeyValueDto valueDto = keyMap.get(content);

		if (valueDto == null) {
			// 没匹配上，直接返回
			return false;
		}

		/**
		 * 触发频次统计
		 */
		acceptMessage(member);
		// log.info("消息“{}”满足常规匹配", content);

		/**
		 * 过滤规则匹配
		 */
		if (filterRuleMatch(member, group, valueDto, isReferenced, originMember)) {
			return false;
		}

		/**
		 * 常规过滤已经完成
		 * 直接进入特殊类型处理
		 */
		if (valueDto.getSpecial().equals(1)) {
			// 需要特殊处理
			if (valueDto.getId() == 4) {
				// jrrp
				specialService.jrrp(group, member.getMemberId());
			} else if (valueDto.getId() == 34) {
				// 查询黑名单
				specialService.blackList(group, member, valueDto, content);
			} else if (valueDto.getId() == 313) {
				specialService.favorability(group, member, valueDto, content);
			}

			return true;
		}

		/**
		 * 好感度过滤
		 */
		List<ValueInfo> favalList = favorabilityFilter(member, valueDto);

		/**
		 * 过滤规则匹配
		 * 返回过滤后的value集合
		 */
		favalList = valueFilter(member, favalList);

		// 如果过滤后的回复列表为空，也不满足特殊处理，则直接返回
		if (CollectionUtils.isEmpty(favalList)) {
			return false;
		}

		/**
		 * 构造并发送消息
		 */
		sendMessage(member, group, favalList);

		return true;
	}

	/**
	 * 构造并发送消息
	 * @param member
	 * @param group
	 * @param favalList
	 */
	private void sendMessage(MemberInfo member, Group group, List<ValueInfo> favalList) {
		ValueInfo result = PathUtil.randomList(favalList);
		log.info("回复value:“{}”", result.getValue());
		MessageChain messageChain = new MessageChain();

		if (result.getNeedReference().equals(1)) {
			// 如果需要艾特
			messageChain.at(member.getMemberId());
		}

		if (result.getType().equals(0)) {
			// 文本类型
			MessageUtil.buildTextMessage(messageChain, result.getValue());
		} else if (result.getType().equals(1)) {
			// 图片类型
			MessageUtil.buildImageMessage(messageChain, result.getValue());
		} else if (result.getType().equals(2)) {
			// 音频类型
			MessageUtil.buildVideoMessage(messageChain, result.getValue());
		} else if (result.getType().equals(10)) {
			// 混合类型
			String jsonValue = result.getValue();
			MessageChain chainOne = new MessageChain(), chainTwo = new MessageChain(), chainThree = new MessageChain();
			MessageChain atMember = new MessageChain();
			List<Map<String, String>> valueMapList = (List<Map<String, String>>) JSON.parse(jsonValue);
			valueMapList.forEach(valueMap -> {
				if (valueMap.get(ConstantPool.MULTIPART_BELONG).equals(ConstantPool.MULTIPART_ONE)) {
					buildMessage(chainOne, valueMap);
				} else if (valueMap.get(ConstantPool.MULTIPART_BELONG).equals(ConstantPool.MULTIPART_TWO)) {
					buildMessage(chainTwo, valueMap);
				} else if (valueMap.get(ConstantPool.MULTIPART_BELONG).equals(ConstantPool.MULTIPART_THREE)) {
					buildMessage(chainThree, valueMap);
				}
			});

			if (result.getNeedReference().equals(1)) {
				// 如果需要艾特
				atMember.at(member.getMemberId());
			}

			// 混合类型的，可以发最多3条消息
			sendMultMessage(group, chainOne, chainTwo, chainThree, atMember);
		}

		// 非混合类型的，发送一条消息
		if (!result.getType().equals(ConstantPool.MULTIPART_TYPE)) {
			group.sendMessage(messageChain);
		}
	}

	/**
	 * 发送多条消息
	 * 如果消息体不为空，则发送
	 */
	private void sendMultMessage(Group group, MessageChain ... messageChains) {
		for (MessageChain messageChain : messageChains) {
			if (messageChain.size() > 0) {
				group.sendMessage(messageChain);
			}
		}
	}

	/**
	 * 好感度过滤
	 * @param member
	 * @param valueDto
	 * @return
	 */
	private List<ValueInfo> favorabilityFilter(MemberInfo member, KeyValueDto valueDto) {
		/**
		 * 4. 进行好感度过滤
		 */
		int favorability = member.getFavorability();
		List<ValueInfo> valueList = valueDto.getValueList();
		// 获取满足好感度的回复列表
		return valueList.stream().
				filter(value -> favorability >= value.getMinFavorability() && favorability <= value.getMaxFavorability()).collect(
				Collectors.toList());
	}

	/**
	 * value的过滤
	 * @param member
	 * @param favalList
	 */
	private List<ValueInfo> valueFilter(MemberInfo member, List<ValueInfo> favalList) {
		if (CollectionUtils.isEmpty(favalList)) {
			return null;
		}

		List<ValueInfo> tmpCollect = favalList.stream().filter(value -> {
			String valueFilter = value.getFilter();
			// 如果过滤规则为空，直接通过
			if (StringUtils.isBlank(valueFilter)) {
				return true;
			}

			JSONObject jsonObject = JSON.parseObject(valueFilter);
			JSONArray memberInclude = jsonObject.getJSONArray(ConstantPool.MEMBER_INCLUDE);
			JSONArray memberExclude = jsonObject.getJSONArray(ConstantPool.MEMBER_EXCLUDE);

			if (memberInclude != null) {
				Set<Long> collect = new HashSet<>(memberInclude.toJavaList(Long.class));
				if (!collect.contains(member.getMemberId())) {
					return false;
				}
			} else if (memberExclude != null) {
				Set<Long> collect = new HashSet<>(memberExclude.toJavaList(Long.class));
				if (collect.contains(member.getMemberId())) {
					return false;
				}
			}

			return true;
		}).collect(Collectors.toList());

		return tmpCollect;
	}

	/**
	 * 艾特，过滤规则匹配
	 * @param member
	 * @param group
	 * @param valueDto
	 * @return
	 */
	private boolean filterRuleMatch(MemberInfo member, Group group, KeyValueDto valueDto, boolean isReferenced
			, Member originMember) {

		/**
		 * 操作者权限过滤
		 */
		int nowRole = this.getNowRole(originMember);
		if (nowRole < Integer.valueOf(valueDto.getAuthRange())) {
			log.info("权限不足，用户{}无法使用指令{}", member.getMemberId(), valueDto.getKey());
			return true;
		}

		/**
		 *
		 * 2. 判断是否需要被艾特
		 * key需要艾特 & 内容包含艾特 ： true
		 * key需要艾特 & 内容不包含艾特 ：false
		 * key不需要艾特 & 内容包含艾特 ：false
		 * key不需要艾特 & 内容不包含艾特 ：true
		 */
		if (!valueDto.getReferenced().equals(ConstantPool.NEED_REFERENCED_BOTH)
				&& valueDto.getReferenced().equals(ConstantPool.NEED_REFERENCED_YES) != isReferenced) {
			return true;
		}

		String filter = valueDto.getFilter();
		JSONObject jsonObject = JSON.parseObject(filter);
		JSONArray groupInclude = jsonObject.getJSONArray(ConstantPool.GROUP_INCLUDE);
		JSONArray groupExclude = jsonObject.getJSONArray(ConstantPool.GROUP_EXCLUDE);
		JSONArray memberInclude = jsonObject.getJSONArray(ConstantPool.MEMBER_INCLUDE);
		JSONArray memberExclude = jsonObject.getJSONArray(ConstantPool.MEMBER_EXCLUDE);
		JSONArray memberWhiteList = jsonObject.getJSONArray(ConstantPool.MEMBER_WHITE_LIST);
		JSONArray memberBlackList = jsonObject.getJSONArray(ConstantPool.MEMBER_BLACK_LIST);

		/**
		 * 第一层过滤：qq号白名单过滤（非硬性，即使不满足也会放行给后续判断）
		 * 需要 memberWhiteList 或 memberBlackList（可都存在）
		 * 1. 如果当前key里白名单包含当前qq，直接放行。如果不包含，交给后续群号过滤
		 * 2. 如果当前key里黑名单包含当前qq，直接拦截。如果不包含，交给后续群号过滤
		 */
		if (memberWhiteList != null) {
			Set<Long> collect = new HashSet<>(memberWhiteList.toJavaList(Long.class));
			// 如果白名单里包含当前qq，直接放行，如果不包含，则交给后续判断
			if (collect.contains(member.getMemberId())) {
				return false;
			}
		}
		if (memberBlackList != null) {
			// 如果黑名单里包含当前qq，直接拦截，如果不包含，则交给后续判断
			Set<Long> collect = new HashSet<>(memberBlackList.toJavaList(Long.class));
			if (collect.contains(member.getMemberId())) {
				return true;
			}
		}


		/**
		 * 第二层过滤：qq号硬性过滤
		 * 需要 memberInclude 或 memberExclude 存在任一
		 * 1. 如果当前memberInclude包含当前qq，直接放行。如果不包含，直接拦截
		 * 2. 如果当前memberExclude包含当前qq，直接拦截。如果不包含，直接放行
		 */
		if (memberInclude != null) {
			Set<Long> collect = new HashSet<>(memberInclude.toJavaList(Long.class));
			return !collect.contains(member.getMemberId());
		} else if (memberExclude != null) {
			Set<Long> collect = new HashSet<>(memberExclude.toJavaList(Long.class));
			return collect.contains(member.getMemberId());
		}

		/**
		 * 第三层过滤：群号硬性过滤
		 * 需要 groupInclude 或 groupExclude 存在任一
		 * 1. 如果当前groupInclude包含当前群号，直接放行。如果不包含，直接拦截
		 * 2. 如果当前groupExclude包含当前群号，直接拦截。如果不包含，直接放行
		 */
		if (groupInclude != null) {
			Set<Long> collect = new HashSet<>(groupInclude.toJavaList(Long.class));
			return !collect.contains(group.getGroupId());
		} else if (groupExclude != null) {
			Set<Long> collect = new HashSet<>(groupExclude.toJavaList(Long.class));
			return collect.contains(group.getGroupId());
		}

		// 如果无过滤规则，放行
		return false;
	}

	/**
	 * 获取当前用户的权限等级
	 * @param originMember
	 * @return
	 */
	private int getNowRole(Member originMember) {
		if (pool.MASTER_QQ.contains(originMember.getUserId())) {
			return ConstantPool.MEMBER_AUTH_SUPER_ADMIN;
		} else if (originMember.getRole().equals(ConstantPool.GROUP_OWNER)) {
			return ConstantPool.MEMBER_AUTH_GROUP_OWNER;
		} else if (originMember.getRole().equals(ConstantPool.GROUP_ADMIN)) {
			return ConstantPool.MEMBER_AUTH_GROUP_ADMIN;
		} else {
			return ConstantPool.MEMBER_AUTH_NORMAL;
		}
	}

	/**
	 * 构造Text&Image信息
	 * @param messageChain
	 * @param valueMap
	 */
	private void buildMessage(MessageChain messageChain, Map<String, String> valueMap) {
		if (valueMap.get(ConstantPool.MULTIPART_KEY).equals(ConstantPool.MULTIPART_TEXT)) {
			MessageUtil.buildTextMessage(messageChain, valueMap.get(ConstantPool.MULTIPART_VALUE));
		} else if (valueMap.get(ConstantPool.MULTIPART_KEY).equals(ConstantPool.MULTIPART_IMAGE)) {
			MessageUtil.buildImageMessage(messageChain, valueMap.get(ConstantPool.MULTIPART_VALUE));
		}
	}

	/**
	 * 判断群聊和角色状态，是否处于生效状态
	 * @param groupId 群号
	 * @param userId qq号
	 * @return
	 */
	private boolean statusCheck(long groupId, long userId) {
		if (redisUtils.get(ConstantPool.GROUP_TURN_KEY + groupId) != null) {
			return false;
		}

		Set<Long> failureGroupSet = variablePool.getFailureGroupSet();
		Set<Long> publicFailureMemberSet = variablePool.getPublicFailureMemberSet();

		return !failureGroupSet.contains(groupId) && !publicFailureMemberSet.contains(userId);
	}

	/**
     * 熔断检查：
     * 1. 群聊1分钟内发送100条消息，该群聊熔断10分钟
     *
     * @param groupId 群号
     */
    private boolean fusingCheck(long groupId) {
        String key = ConstantPool.GROUP_FUSING_STATISTIC_KEY + groupId;
        String fusingKey = ConstantPool.GROUP_FUSING_KEY + groupId;

        // 判断该群有无熔断标志
        if (redisUtils.get(fusingKey) != null) {
            return false;
        }

        // 将当前时间戳加入队列中
        long currentTime = System.currentTimeMillis();
        redisUtils.rightPush(key, String.valueOf(currentTime));
        long listSize = redisUtils.getListSize(key);

        if (listSize > ConstantPool.MAX_LIST_SIZE) {
            // 并发异常情况下，超过链表长度超标，修剪链表，保证取到的leftPop取出来的值 为第一百条消息
            redisUtils.ltrim(key, 0L, ConstantPool.MAX_LIST_SIZE);
        } else if (listSize < ConstantPool.MAX_LIST_SIZE) {
            // 链表未达到阈值，直接发送
            return true;
        }

        String leftMill = redisUtils.leftPop(key);
        if (currentTime - Long.parseLong(leftMill) < ConstantPool.LIMIT_SECONED_TIME) {
            // 添加标志，熔断10分钟（这里不一定要设置成GROUP_TURN_OFF，只要有值就行）
            redisUtils.set(fusingKey, ConstantPool.GROUP_TURN_OFF, ConstantPool.MELTING_TIME);
            // 将该群的队列移除
            redisUtils.del(key);
            log.info("群聊{}触发熔断", groupId);
            return false;
        }
        return true;
    }

	/**
	 * 发送全局通知
	 * @param value 通知内容
	 * @param groups 群聊列表，用英文逗号隔开，如（123123,123456,123789）。如为所有生效群聊，则该值为""
	 * @return
	 */
	@Override
	public void notice(String value, String groups, boolean atAll) {
		if (StringUtils.isBlank(value)) {
			throw new ServiceException("消息为空，不可发送！");
		}

		// 填充发送目标
		List<Long> groupIds = new ArrayList<>();
		if (StringUtils.isBlank(groups)) {
			QueryWrapper wrapper = new QueryWrapper();
			wrapper.eq("status", "1");
			List<GroupInfo> validGroups = groupService.list(wrapper);
			groupIds.addAll(validGroups.stream().map(GroupInfo::getGroupId).collect(Collectors.toList()));
		} else {
			List<Long> ids = Arrays.asList(groups.split(",")).stream().map(Long::parseLong).collect(Collectors.toList());
			groupIds.addAll(ids);
		}

		log.info("发送公告，groupId: {}", groupIds);

		// 发送消息
		Bot bot = BotFactory.getBots().get(0);
		MessageChain messageChain = new MessageChain();
		messageChain.text(value);
		if (atAll) {
			messageChain.atAll();
		}
		for (Long groupId : groupIds) {
			try {
				Group group = new Group(groupId, "消息发送", bot);
				group.sendMessage(messageChain);
				Thread.sleep(2000);
			} catch (Exception e) {
				log.error("群聊:{}发送通知失败", groupId);
			}
		}
	}

}

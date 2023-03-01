package top.lsyweb.qqbot.service.client;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zhuangxv.bot.core.BotFactory;
import com.zhuangxv.bot.core.Group;
import com.zhuangxv.bot.core.Member;
import com.zhuangxv.bot.message.MessageChain;
import com.zhuangxv.bot.message.support.ImageMessage;
import com.zhuangxv.bot.message.support.ReplyMessage;
import com.zhuangxv.bot.message.support.TextMessage;
import com.zhuangxv.bot.scheduled.FlushCacheScheduled;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.geometry.Positions;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import top.lsyweb.qqbot.config.RemoteOcr;
import top.lsyweb.qqbot.controller.oms.HealthController;
import top.lsyweb.qqbot.dto.AgentDto;
import top.lsyweb.qqbot.dto.AgentPair;
import top.lsyweb.qqbot.dto.KeyValueDto;
import top.lsyweb.qqbot.dto.VariablePool;
import top.lsyweb.qqbot.entity.AgentInfo;
import top.lsyweb.qqbot.entity.MemberInfo;
import top.lsyweb.qqbot.entity.ValueInfo;
import top.lsyweb.qqbot.exception.ServiceException;
import top.lsyweb.qqbot.service.MemberService;
import top.lsyweb.qqbot.util.*;
import top.lsyweb.qqbot.util.config.SystemConfigPool;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

/**
 * @Auther: Erekilu
 * @Date: 2022-01-24
 */
@Slf4j
@Service
public class SpecialServiceImpl implements SpecialService
{
	private VariablePool variablePool;
	private RedisUtils redisUtils;
	private RemoteOcr remoteOcr;

	private MemberService memberService;
	private VariablePoolService variablePoolService;
	private SystemConfigPool pool;
	private HealthController healthController;
	private FlushCacheScheduled flushCacheScheduled;

	private Random random;
	private RestTemplate restTemplate;

	@Autowired
	public SpecialServiceImpl(VariablePool variablePool, RedisUtils redisUtils, RemoteOcr remoteOcr,
							  MemberService memberService, VariablePoolService variablePoolService,
							  SystemConfigPool pool, RestTemplate restTemplate,
							  HealthController healthController, FlushCacheScheduled flushCacheScheduled) {
		this.variablePool = variablePool;
		this.redisUtils = redisUtils;
		this.remoteOcr = remoteOcr;
		this.memberService = memberService;
		this.variablePoolService = variablePoolService;
		// this.groupMessageService = groupMessageService;
		this.pool = pool;
		this.restTemplate = restTemplate;
		this.random = new Random();
		this.healthController = healthController;
		this.flushCacheScheduled = flushCacheScheduled;
	}

	/**
	 * 群聊开闭
	 * @param group
	 * @param member
	 * @param msg
	 */
	public void groupTurn(Group group, Member member, String msg) {
		if (variablePool.getFailureGroupSet().contains(group.getGroupId())) {
			// 如果群聊被禁止了，无视群聊开闭
			return;
		}

		Matcher matcher = PathUtil.getRegex(ConstantPool.GROUP_TURN_REGEX, msg);
		String status = matcher.group(1);
		Integer time = StringUtils.isEmpty(matcher.group(2)) ?
				pool.BOT_OFF_DEFAULT_TIME : Integer.parseInt(matcher.group(2));

		if (status.equals(ConstantPool.GROUP_TURN_OFF) || status.equals(ConstantPool.GROUP_TRUN_OFF_ALIA)) {
			// 关闭机器人，默认十分钟
			redisUtils.set(ConstantPool.GROUP_TURN_KEY + group.getGroupId(), ConstantPool.GROUP_TURN_OFF, time * 60);
			// 回信息
			MessageUtil.sendTextMessage(group, "达不溜要睡" + time + "分钟的觉觉了，不要打扰我哦~");

			log.info("操作员{}({})关闭了群聊{}({}){}分钟", member.getNickname(), member.getUserId(), group.getGroupName()
					,group.getGroupId(), time);
		} else if (status.equals(ConstantPool.GROUP_TURN_ON) || status.equals(ConstantPool.GROUP_TURN_ON_ALIA)) {
			// 开启机器人
			redisUtils.del(ConstantPool.GROUP_TURN_KEY + group.getGroupId());
			// 回信息
			MessageUtil.sendTextMessage(group, "达不溜已经整装待发，准备开工啦！");

			log.info("操作员{}({})开启了群聊{}({})", member.getNickname(), member.getUserId(), group.getGroupName()
					,group.getGroupId());
		}
	}

	/**
	 * 刷新系统内缓存或刷新go-cqhttp缓存
	 * @param group
	 * @param member
	 * @param valueDto
	 * @param content
	 */
	public void innerRefresh(Group group, MemberInfo member, KeyValueDto valueDto, String content) {
		/**
		 * 刷新缓存和刷新群聊好友信息
		 */
		if (ConstantPool.KEY_REFRESH.equals(content)) {
			healthController.refresh();
			MessageUtil.sendTextMessage(group, "已刷新配置信息");
		} else if (ConstantPool.GROUP_REFRESH.equals(content)) {
			flushCacheScheduled.flush();
			MessageUtil.sendTextMessage(group, "已刷新群聊好友信息");
		}
	}

	/**
	 * 涩图API
	 * 使用自定义线程池
	 */
	@Async("executor")
	@Override
	public Boolean lolicon(Group group, MemberInfo member, KeyValueDto valueDto, String content) {

		/**
		 * 限流设置
		 */
		Integer limit = pool.SETU_LIMIT.getInteger(String.valueOf(group.getGroupId()));
		if (limit != null) {
			// 如果当前群聊配置了限流
			// key为群号和qq号组合
			String key = ConstantPool.SETU_LIMIT + group.getGroupId() + "_" + member.getMemberId();
			if (redisUtils.get(key) != null) {
				int val = (Integer) redisUtils.get(key);
				// 超过限制次数
				if (val > limit) {
					return false;
				}
				redisUtils.set(key, val + 1, redisUtils.expire(key));
			} else {
				// 截至0点的key
				redisUtils.set(key, 1, PathUtil.getReleaseTime());
			}
		}

		// 获取tag串
		Matcher matcher = PathUtil.getRegex(valueDto.getKey(), content);
		String tagString = matcher.group(2);
		// 如果为主人调用，可返回r18涩图
		boolean r18 = false;
		if (pool.MASTER_QQ.contains(member.getMemberId()) && tagString.contains("r18")) {
			r18 = true;
		}
		tagString = tagString.replace("r18", "");
		// 转化为tag数组
		String [] tags = tagString.split("&");

		// 如果tag为空，直接调用本地涩图
		if (tags.length == 1 && tags[0].equals("")) {
			localSetu(group, valueDto);
			return true;
		}

		// 缩写词转换
		JSONObject replaceMap = pool.SETU_CONVERT_MAP;
		for (int i = 0 ; i < tags.length ; i++) {
			String tag = tags[i];
			if (replaceMap.containsKey(tag)) {
				tags[i] = replaceMap.getString(tag);
			}
		}

		// 请求API
		try {
			StringBuilder url = new StringBuilder("https://api.lolicon.app/setu/v2?size=" + pool.SETU_QUALITY);
			for (String tag : tags) {
				url.append("&tag=").append(tag);
			}
			if (r18) {
				url.append("&r18=1");
			}
			String response = restTemplate.getForObject(url.toString(), String.class);
			log.info("调用lolicon涩图API相应：{}", response);

			// 解析API结果
			JSONObject jsonObject = JSONObject.parseObject(response);
			String isError = jsonObject.getString("error");
			if (StringUtils.isNotBlank(isError) || jsonObject.getJSONArray("data").size() == 0) {
				throw new ServiceException("未查询到任何满足要求的图片！");
			}

			// 获取图片链接
			JSONObject obj = jsonObject.getJSONArray("data").getJSONObject(0);
			int width = obj.getInteger("width");
			int height = obj.getInteger("height");
			String pivixUrl = obj.getJSONObject("urls").getString(pool.SETU_QUALITY);
			// 干扰
			setuDisturb(group, HttpsDownloadUtils.downloadFileProxy(pivixUrl), pivixUrl, pool.SETU_MAX_WIDTH_HEIGHT, pivixUrl);
		} catch (Exception e) {
			log.error("调用lolicon涩图API失败: {}", e);
			e.printStackTrace();
			// 如二次干扰都失败，调用本地图库
			localSetu(group, valueDto);
		}

		return true;
	}

	/**
	 * 调用本地图库并发送（二次干扰）
	 * @param group
	 * @param valueDto
	 */
	private void localSetu(Group group, KeyValueDto valueDto) {
		log.info("==============开始调用本地图库=============");
		ValueInfo result = PathUtil.randomList(valueDto.getValueList());

		try {
			BufferedImage bufferedImage = ImageIO.read(new File(PathUtil.getBasePath() + result.getValue()));
			// 图片干扰并发送
			setuDisturb(group, bufferedImage, result.getValue(), pool.SETU_MAX_WIDTH_HEIGHT,
					PathUtil.getOssPath() + PathUtil.getBasePath() + result.getValue());
		} catch (Exception e) {
			e.printStackTrace();
			log.error("本地图片设置干扰失败: {}", e.getMessage());
		}
	}

	/**
	 * 图片干扰并发送
	 * 首先顺时针旋转90°，如还未发送出去，则增加水印
	 * @param group
	 * @param source 图片源 InputStream BufferedImage
	 * @param sourceName 图片名
	 * @param defaultUrl 未干扰图片的链接
	 * @throws Exception
	 */
	private void setuDisturb(Group group, Object source, String sourceName, List<Integer> maxWidthHeight, String defaultUrl) throws Exception {
		if (pool.SETU_DISTURB == 1) {
			int maxWidth = random.nextInt(maxWidthHeight.get(1) - maxWidthHeight.get(0)) + maxWidthHeight.get(0);
			int maxHeight = random.nextInt(maxWidthHeight.get(3) - maxWidthHeight.get(2)) + maxWidthHeight.get(2);
			String tempFilePath = PathUtil.getBasePath() + "image_disturb/" +  PathUtil.getUuidName(sourceName.substring(sourceName.lastIndexOf(".")).toLowerCase());
			try {
				// 顺时针旋转90°，保存到临时文件中
				if (source instanceof InputStream) {
					Thumbnails.of((InputStream) source)
							.rotate(90)
							.size(maxWidth, maxHeight)
							.outputQuality(0.9).toFile(new File(tempFilePath));
				} else if (source instanceof BufferedImage) {
					Thumbnails.of((BufferedImage) source)
							.rotate(90)
							.size(maxWidth, maxHeight)
							.outputQuality(0.9).toFile(new File(tempFilePath));
				}
				group.sendMessage(new ImageMessage(PathUtil.getOssPath() + tempFilePath));
			} catch (Exception e) {
				log.error("图片旋转后发送失败，开始在旋转的基础上加水印：{}", e);
				String newTmpFile = PathUtil.getBasePath() + "image_disturb/" +  PathUtil.getUuidName(sourceName.substring(sourceName.lastIndexOf(".")).toLowerCase());
				Thumbnails.of(new File(tempFilePath))
						.watermark(Positions.BOTTOM_RIGHT, ImageIO.read(new File(PathUtil.getBasePath() + "image_static/W.png")), 0.4f)
						.outputQuality(0.9)
						.size(maxHeight, maxWidth)
						.toFile(new File(newTmpFile));
				group.sendMessage(new ImageMessage(PathUtil.getOssPath() + newTmpFile));
			}
		} else if (pool.SETU_DISTURB == 0) {
			group.sendMessage(new ImageMessage(defaultUrl));
		}
	}

	/**
	 * 签到
	 * 每天签到一次
	 * 每次获得3张摸摸券和一张招募券。
	 * @param group
	 * @param member
	 * @param valueDto
	 */
	@Override
	public void signIn(Group group, MemberInfo member, KeyValueDto valueDto, String content) {
		// 检测是否已签到
		String key = ConstantPool.SIGN_IN_KEY + member.getMemberId();
		if (redisUtils.get(key) != null) {
			// 已签到，回复消息
			MessageUtil.sendTextMessage(group, "刀客塔~今天已经签到了哦，明天再来吧！", member.getMemberId(), MessageUtil.PRE);
			return;
		} else {
			// 未签到，redis添加记录
			redisUtils.set(key, 1, PathUtil.getReleaseTime());
		}

		// 给库存添加3张摸摸券、50张招募券
		int touchNum = pool.SIGN_IN_TOUCH_NUM;
		int recruitNum = pool.SIGN_IN_RECRUIT_NUM;
		member.setTouchTicket(member.getTouchTicket() + touchNum);
		member.setRecruitTicket(member.getRecruitTicket() + recruitNum);

		// 发送消息
		StringBuilder sb = new StringBuilder();
		sb.append("刀客塔，签到成功！已为您的账号添加").append(touchNum).append("张摸摸券，").append(recruitNum).append("张寻访凭证。");
		sb.append(PathUtil.getLine());
		sb.append("目前拥有：" + member.getTouchTicket() + "张摸摸券，" + member.getRecruitTicket() + "张寻访凭证。");
		MessageUtil.sendTextMessage(group, sb.toString(), member.getMemberId(), MessageUtil.POST);
	}


	/**
	 * 撤回消息
	 */
	@Override
	public boolean deleteMsg(MessageChain messageChain, Group group, String msg) {
		ReplyMessage message = (ReplyMessage) messageChain.get(0);
		if (msg.contains("撤回")) {
			log.info("撤回消息：{}", message.getId());
			group.deleteMsg(message.getId());
			return true;
		}
		return false;
	}

	/**
	 * 查询信赖值
	 */
	@Override
	public void getFavorability(Group group, MemberInfo member, KeyValueDto valueDto, String content) {
		// 发送消息
		MessageUtil.sendTextMessage(group, "信赖值：" + member.getFavorability(), member.getMemberId(), MessageUtil.PRE);
	}

	/**
	 * 查询库存
	 * @param group
	 * @param member
	 * @param valueDto
	 * @param content
	 */
	@Override
	public void getRepository(Group group, MemberInfo member, KeyValueDto valueDto, String content) {
		MessageUtil.sendTextMessage(group, "Dr." + member.getMemberNickname() + "，您目前持有" + member.getTouchTicket() +
				"张摸摸券，" + member.getRecruitTicket() + "张寻访凭证。", member.getMemberId(), MessageUtil.PRE);
	}

	/**
	 * 中转消息
	 * @param messageChain
	 */
	@Override
	public void messageTransit(String msg, MessageChain messageChain, int messageId) {
		// 排除非文字类型和transit_target
		if (msg.startsWith(ConstantPool.TRANSIT_KEY)) {
			return;
		}

		// 从redis获取目标群号
		Integer groupId = (Integer) redisUtils.get(ConstantPool.TRANSIT_KEY);
		if (groupId != null) {
			log.info("调用中转服务，目标群号（{}）", groupId);
			// 构造群聊发送消息
			Group target = new Group(groupId, "中转群聊", BotFactory.getBots().get(0));
			target.sendMessage(messageChain);
		}
	}

	/**
	 * 设置中转目标
	 * @param valueDto
	 * @param content
	 */
	@Override
	public void setTransitTarget(KeyValueDto valueDto, String content) {
		/**
		 * 参数检查，匹配群号关键字
		 */
		Matcher matcher = PathUtil.getRegex(valueDto.getKey(), content);
		String groupId = matcher.group(1);

		redisUtils.set(ConstantPool.TRANSIT_KEY, Integer.parseInt(groupId));
		log.info("设置中转目标：{}", groupId);
	}


	/**
	 * 查询本群黑名单
	 * @param group
	 * @param member
	 * @param valueDto
	 * @param content
	 */
	@Override
	public void blackList(Group group, MemberInfo member, KeyValueDto valueDto, String content) {
		QueryWrapper<MemberInfo> queryWrapper = new QueryWrapper<>();
		// 群聊过滤
		queryWrapper.eq("type", 1);
		List<MemberInfo> blackList = memberService.list(queryWrapper);

		StringBuilder sb = new StringBuilder();
		sb.append("共有").append(blackList.size()).append("位用户处于黑名单中：");
		sb.append(PathUtil.getLine()).append(PathUtil.getLine());

		for (int i = 0 ; i < blackList.size() ; i++) {
			MemberInfo memberInfo = blackList.get(i);
			sb.append("用户名：").append(memberInfo.getMemberNickname()).append("，账号：").append(memberInfo.getMemberId());
			sb.append("，过滤原因：").append(memberInfo.getReason());
			if (i != blackList.size() - 1) {
				sb.append(PathUtil.getLine()).append(PathUtil.getLine());
			}
		}

		MessageUtil.sendTextMessage(group, sb.toString());
	}

	/**
	 * 查询今日人品
	 * @param memberId 角色id
	 */
	@Override
	public void jrrp(Group group, Long memberId) {
		Map<Long, String> jrrpMap = variablePool.getJrrpMap();

		String jrrp = jrrpMap.get(memberId);
		if (jrrp != null) {
			// 直接输出
			MessageUtil.sendTextMessage(group, jrrp, memberId, MessageUtil.PRE);
		} else {
			// 生成一个0 ~ 100的随机数
			int resultRp = random.nextInt(101);
			// 后置拼接
			String result = "今日人品：" + String.valueOf(resultRp) + "（";
			String post = null;
			if (resultRp >= 90) {
				post = "哇！人品爆炸~~快去开包！";
			} else if (resultRp >= 80) {
				post = "今天运气不错哦，刀客塔";
			} else if (resultRp >= 60) {
				post = "很好很好~";
			} else if (resultRp >= 40) {
				post = "刀客塔~今天脸很黑呀";
			} else if (resultRp >= 20) {
				post = "水逆退散，快跑！";
			} else {
				post = "危！";
			}
			result += post + "）";

			// 保存在jrrpMap中
			variablePool.getJrrpMap().put(memberId, result);
			// 输出
			MessageUtil.sendTextMessage(group, result, memberId, MessageUtil.PRE);
		}
	}

	/**
	 * ocr公开招募
	 * @param group
	 * @param member
	 * @param valueDto
	 * @param url
	 */
	@Override
	public void ocrOpenRecruitment(Group group, MemberInfo member, KeyValueDto valueDto, String url) {
		/**
		 * 1. 裁剪图像，保留中间词条部分
		 * 2. 调用ocr，获取5个词条
		 */
		try {
			log.info("开始进行公开招募OCR");
			BufferedImage bufImage = ImageIO.read(HttpsDownloadUtils.downloadFile(url));
			// 获取原图像的高 宽
			int height = bufImage.getHeight(), width = bufImage.getWidth();

			// 获取尺寸配置
			JSONObject jsonObj = pool.OPEN_RECRUITMENT_SIZE_MAP;
			List<Double> sizeList = jsonObj.getJSONArray(String.valueOf(valueDto.getId())).toJavaList(Double.class);
			// 裁剪公招图像
			bufImage = bufImage.getSubimage((int)(width * sizeList.get(0)), (int)(height * sizeList.get(1)),
											(int)(width * sizeList.get(2)), (int)(height * sizeList.get(3)));

			// 输出到本地
			String filePath = ConstantPool.OCR_TEMP_PATH + PathUtil.getUuidName(".jpg");
			File outputFile = new File(PathUtil.getBasePath() + filePath);
			ImageIO.write(bufImage, "jpg", outputFile);

			// 拼接ocr访问路径
			String ocrPath = PathUtil.getBasePath() + filePath;

			// ocr识别
			List<String> resultList = remoteOcr.ocr(ocrPath);
			// 判断ocr结果是否合法
			if (resultList == null || resultList.size() < 5) {
				// 如果识别到的不是5个词条，说明识别有问题，直接返回结果
				log.warn("公开招募ocr识别结果小于5条。");
				return;
			}

			log.info("公开招募词条：{}", String.join(" ", resultList));

			/**
			 * 1. 拼接字符串
			 * 2. 进行易错词处理
			 * 3. 调用常规公开招募
			 */
			// 获取替换词Map
			JSONObject replaceMap = pool.OPEN_RECRUITMENT_CONVERT_MAP;
			StringBuilder openStr = new StringBuilder("公开招募");
			for (String s : resultList) {
				if (replaceMap.containsKey(s)) {
					s = (String)replaceMap.get(s);
				}
				openStr.append(" ").append(s);
			}

			// 调用公开招募
			for (KeyValueDto keyValueDto : variablePool.getRegexKeyList()) {
				// 公开招募正则
				if (keyValueDto.getId() == 5) {
					this.openRecruitment(group, member, keyValueDto, openStr.toString());
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			log.error("公开招募ocr处理异常");
		}
	}

	/**
	 * ================公开招募================
	 * pre: 限流&参数检查
	 * 1. 获取满足公开招募干员池，封装成DTO List<AgentDto> agentDtoList，tags为Set类型
	 * 2. 获取tag组合排列数组 List<List<String>> tagsList
	 * 3. 遍历tagsList，对于每个tags，词条移除-高级资深干员、资深干员，并打上标记
	 * 4. 如果有高级资深干员，则排除非6星干员，如果有资深干员，则排除非5星干员
	 * 5. 如果包含tag，将其加入结果集
	 * 6. 按规则排序
	 * 7. 输出结果
	 */
	@Override
	public void openRecruitment(Group group, MemberInfo member, KeyValueDto valueDto, String content) {
		/**
		 * 限流
		 * 每个QQ号10分钟只能公开招募4次
		 */
		String key = ConstantPool.OPEN_RECRUITMENT_KEY + member.getMemberId();
		if (redisUtils.get(key) != null) {
			int val = (Integer) redisUtils.get(key);
			// 超过4次
			if (val > pool.OPEN_RECRUITMENT_LIMIT) {
				return;
			}
			redisUtils.set(key, val + 1, redisUtils.expire(key));
		} else {
			// 10分钟的key
			redisUtils.set(key, 1, pool.OPEN_RECRUITMENT_SECOND);
		}

		/**
		 * 获取公开招募干员池
		 */
		List<AgentDto> agentDtoList = variablePool.getAgentDtoList();

		/**
		 * 获取tag排列组合数组
		 */
		Matcher matcher = PathUtil.getRegex(valueDto.getKey(), content);
		List<List<String>> tagsList = new ArrayList<>();

		List<String> inputTags = new ArrayList<>();
		for (int i = 1 ; i <= matcher.groupCount() ; i++) {
			if (StringUtils.isNotBlank(matcher.group(i))) {
				inputTags.add(matcher.group(i));
			}
		}

		// 词条去重
		Set<String> tagsSet = new HashSet<>(inputTags);
		List<String> postTags = new ArrayList<>(tagsSet);

		int tagLen = postTags.size();
		for (int i = 0 ; i < tagLen ; i++) {
			List<String> tmpList = new ArrayList<>();
			tmpList.add(postTags.get(i));
			for (int j = i + 1 ; j < tagLen; j++) {
				List<String> tmpListB = new ArrayList<>(tmpList);
				tmpListB.add(postTags.get(j));
				for (int k = j + 1 ; k < tagLen ; k++) {
					List<String> tmpListC = new ArrayList<>(tmpListB);
					tmpListC.add(postTags.get(k));
					tagsList.add(tmpListC);
				}
				tagsList.add(tmpListB);
			}
			tagsList.add(tmpList);
		}

		/**
		 * 遍历tagsList，对于每个tags，词条移除-高级资深干员、资深干员，并打上标记
		 * 遍历公开招募干员池
		 * 如果有高级资深干员，则排除非6星干员，如果有资深干员，则排除非5星干员
		 * 如果agentDtoList[x].getTags().containsAll(tags)，将其加入结果集
		 */
		List<AgentPair> result = new ArrayList<>();
		for (List<String> tags : tagsList) {
			boolean isSuper = tags.remove(ConstantPool.PRO_SENIOR_AGENT);
			boolean isPro = tags.remove(ConstantPool.SENIOR_AGENT);

			AgentPair agentPair = new AgentPair();
			agentPair.setTags(tags);
			for (AgentDto agentDto : agentDtoList) {
				// 高级资深，不为六星的干员
				if (isSuper && agentDto.getLevel() != 6) {
					continue;
				}
				// 资深，不为五星的干员
				if (isPro && agentDto.getLevel() != 5) {
					continue;
				}
				// 干员六星，没有高资
				if (!isSuper && agentDto.getLevel() == 6) {
					continue;
				}

				if (agentDto.getTags().containsAll(tags)) {
					// 添加到结果集
					agentPair.getAgentDtos().add(agentDto);
					// 更新最低星级
					if (agentDto.getLevel() != 2 && agentPair.getMinLevel() > agentDto.getLevel()) {
						agentPair.setMinLevel(agentDto.getLevel());
					}
				}
			}

			if (isSuper) {
				agentPair.getTags().add(ConstantPool.PRO_SENIOR_AGENT);
			}
			if (isPro) {
				agentPair.getTags().add(ConstantPool.SENIOR_AGENT);
			}

			if (agentPair.getAgentDtos().size() != 0) {
				result.add(agentPair);
			}
		}

		/**
		 * 按某种规则排序
		 */
		result.sort(((o1, o2) -> {
			if (!o1.getMinLevel().equals(o2.getMinLevel())) {
				return o2.getMinLevel() - o1.getMinLevel();
			}
			return o1.getAgentDtos().size() - o2.getAgentDtos().size();
		}));

		/**
		 * 拼接到输出字符串
		 */
		StringBuilder resultStr = new StringBuilder();
		for (int i = 0 ; i < result.size() ; i++) {
			AgentPair agentPair = result.get(i);
			// 拼接tag标签
			resultStr.append(String.join(" ", agentPair.getTags()));
			resultStr.append(": ");
			// 拼接干员列表
			resultStr.append(agentPair.getAgentDtos().stream().map(AgentDto::getName).collect(
					Collectors.joining(", ")));

			// 换行符
			resultStr.append(PathUtil.getLine());
			if (i != result.size() - 1) {
				resultStr.append(PathUtil.getLine());
			}
		}

		if (result.size() == 0) {
			resultStr.append("达不溜找不到符合条件的干员www，请检查词条输入是否正确。例如“近卫干员”而不是“近卫”");
		}

		/**
		 * 发送消息
		 */
		MessageUtil.sendTextMessage(group, resultStr.toString(), member.getMemberId(), MessageUtil.POST);
	}

	/**
	 * 骰数
	 */
	@Override
	public void randomNumber(Group group, MemberInfo member, KeyValueDto valueDto, String content) {
		/**
		 * 参数检查，匹配骰数范围
		 */
		Matcher matcher = PathUtil.getRegex(valueDto.getKey(), content);
		String numberStr = matcher.group(1);

		// 长度限制
		if (numberStr.length() >= 10) {
			return;
		}

		int number = 6;
		if (StringUtils.isNotBlank(numberStr)) {
			number = Integer.parseInt(numberStr);
			// 范围限制
			if (number <= 0) {
				return;
			}
		}

		int result = random.nextInt(number) + 1;

		/**
		 * 发送消息
		 */
		MessageUtil.sendTextMessage(group, String.valueOf(result), member.getMemberId(), MessageUtil.PRE);
	}

	/**
	 * 模拟招募
	 */
	@Override
	public void simulationRecruit(Group group, MemberInfo member, KeyValueDto valueDto, String content) {
		/**
		 * 参数检查，匹配两个关键字
		 */
		Matcher matcher = PathUtil.getRegex(valueDto.getKey(), content);
		String number = matcher.group(1), type = matcher.group(2); //  .*(十连|单抽)\s*([^\s]*)


		// 寻访次数数量替换
		JSONObject convertMap = pool.RECRUIT_CONVERT_MAP;
		int count = 0;
		if (convertMap.containsKey(number)) {
			// 满足替换规则
			count = convertMap.getInteger(number);
		} else {
			// 不满足替换规则，判断是否为数字
			if (StringUtils.isEmpty(number) || !StringUtils.isNumeric(number)) {
				// 为空、或不为数字
				return;
			}

			count = Integer.valueOf(number);
			// 次数限制
			if (count <= 0 || count > 300) {
				MessageUtil.sendTextMessage(group, "寻访次数要在300以内哦！", member.getMemberId(), MessageUtil.POST);
				return;
			}
		}

		if (member.getRecruitTicket() < count) {
			MessageUtil.sendTextMessage(group, "寻访凭证不足！目前剩余" + member.getRecruitTicket()
					+ "张，每日首次签到可领取寻访凭证哦~", member.getMemberId(), MessageUtil.PRE);
			return;
		}

		List<AgentInfo> resultList = new ArrayList<>();
		// 寻访凭证减少count张
		member.setRecruitTicket(member.getRecruitTicket() - count);
		// 调用count次单抽
		for (int i = 0 ; i < count ; i++) {
			resultList.add(oneRecruit(type));
		}

		// 封装结果（干员-次数）
		Map<AgentInfo, Long> agents = resultList.stream().collect(
				Collectors.groupingBy(Function.identity(), Collectors.counting()));

		/**
		 * 输出结果
		 */
		StringBuilder result = new StringBuilder();
		Set<AgentInfo> agentInfoSet = agents.keySet();
		for (AgentInfo agentInfo : agentInfoSet) {
			if (agentInfo.getLevel() < 5) {
				continue;
			}

			result.append("（");
			for (int j = 0 ; j < agentInfo.getLevel() ; j++) {
				result.append("★");
			}
			for (int j = 0 ; j < 6 - agentInfo.getLevel() ; j++) {
				result.append("☆");
			}
			result.append("）").append(agentInfo.getName());

			Long agentCount = agents.get(agentInfo);
			if (agentCount > 1L) {
				result.append(" × ").append(agentCount);
			}

			result.append(PathUtil.getLine());
		}
		if (result.length() == 0) {
			result.append("好可怜，你没有抽到任何一个五星以上的干员。");
		} else {
			result.insert(0, "你一共寻访了" + count + "次，五星以上干员为：\n");
		}


		/**
		 * 发送消息
		 */
		MessageUtil.sendTextMessage(group, result.toString(), member.getMemberId(), MessageUtil.POST);
	}

	/**
	 * 游戏王查卡
	 * @param group
	 * @param member
	 * @param valueDto
	 * @param content
	 */
	public void yugiohSearch(Group group, MemberInfo member, KeyValueDto valueDto, String content) {
		Matcher matcher = PathUtil.getRegex(valueDto.getKey(), content);
		// 查询关键字
		String type = matcher.group(1), key = matcher.group(2);

		JSONObject ulrMap = pool.YUGIOH_URL_PREFIX;
		// key替换
		try {
			// 先调用云端key替换
			key = restTemplate.getForObject(ulrMap.getString("nickNameTransfer").replace("{}", key), String.class);
		} catch (Exception e) {
			log.error("调用云端昵称失败", e);
		}

		// 再调用本地key替换
		if (pool.YUGIOH_NAME_MAP.containsKey(key)) {
			key = pool.YUGIOH_NAME_MAP.getString(key);
		}

		String response = restTemplate.getForObject(ulrMap.getString("cardDetail").replace("{}", key), String.class);
		// log.info("调用yugioh-API相应：{}", response);

		// 解析API结果
		JSONArray array = JSONObject.parseObject(response).getJSONArray("result");
		if (array == null || array.size() == 0) {
			return;
		}

		JSONObject cardObject = null;
		for (int i = 0; i < array.size(); i++) {
			if (!array.getJSONObject(i).getString("id").equals("0")) {
				// 如果id不为0，则表示这张卡有效
				cardObject = array.getJSONObject(i);
				break;
			}
		}
		if (cardObject == null) {
			return;
		}

		String id = cardObject.getString("id"), name = cardObject.getString("cn_name"), ocg_name = cardObject.getString("cnocg_n");
		JSONObject textObject = cardObject.getJSONObject("text");
		String types = textObject.getString("types"), desc = textObject.getString("desc");

		// card-detail card-picture card-original-picture
		if (type.equalsIgnoreCase("cp")) {
			group.sendMessage(new ImageMessage(ulrMap.getString("cardPicture").replace("{}", id)));
		} else if (type.equalsIgnoreCase("co")) {
			group.sendMessage(new ImageMessage(ulrMap.getString("cardPictureWithoutBorder").replace("{}", id)));
		} else {
			MessageChain messageChain = new MessageChain();
			messageChain.add(new ImageMessage(ulrMap.getString("cardPicture").replace("{}", id)));
			// card detail才会发送描述，不然只发送卡图
			messageChain.add(new TextMessage(name + " / " + ocg_name + "\r\n"));
			messageChain.add(new TextMessage(types + "\r\n\r\n"));
			messageChain.add(new TextMessage(desc));
			group.sendMessage(messageChain);
		}
	}

	/**
	 * AI回复
	 * @param group
	 * @param member
	 * @param valueDto
	 * @param content
	 */
	public void gptChat(Group group, MemberInfo member, KeyValueDto valueDto, String content, int messageId) {
		Matcher matcher = PathUtil.getRegex(valueDto.getKey(), content);
		// 问题正文
		final String prompt = matcher.group(1);
		int presetIndex = redisUtils.getIntOrDefault(ConstantPool.PERSONAL_SET_KEY + group.getGroupId(), 0);
		String tmpPreset = Optional.ofNullable(redisUtils.get(ConstantPool.GROUP_PROMPT_KEY + group.getGroupId()))
						 .map(Object::toString)
						 .orElse(pool.AI_CHAT_PRESET.getJSONObject(presetIndex).getString("c"));
		String preset = tmpPreset.replace("\"", "\\\"") + "现在我要问你第一个问题：" + prompt + "。";
		preset = preset.replace("\r\n", "");
		log.info("preset: {}", preset);

		// 获取当前群聊的keys
		JSONArray keysArray = pool.AI_CHAT_KEYS.getJSONArray(String.valueOf(group.getGroupId()));
		JSONObject chatConfig = pool.AI_CHAT_CONFIG;
		if (keysArray == null) {
			// 如果当前群聊的keys为空，判断是否为白名单群聊
			if (chatConfig.getJSONArray("whitelist_groups").contains((int)group.getGroupId()) || pool.MASTER_QQ.contains(member.getMemberId())) {
				// 白名单群聊可以使用默认keys
				keysArray = pool.AI_CHAT_KEYS.getJSONArray("default");
			} else {
				int memberUsedTime = redisUtils.getIntOrDefault(ConstantPool.MEMBER_FREE_USED + member.getMemberId(), 0);
				int totalUsedTime = redisUtils.getIntOrDefault(ConstantPool.TOTAL_FREE_USED, 0);
				int freeLimit = chatConfig.getInteger("freePerDay"), memberFreePerDay = chatConfig.getInteger("memberFreePerDay");

				if (totalUsedTime >= freeLimit || memberUsedTime > memberFreePerDay) {
					return;
				}

				if (memberUsedTime == memberFreePerDay) {
					String str = String.format("今日你的预设外的AI回复次数(%d/%d)已用完，"
													   + "如需继续使用请加入W内测群使用，或者联系群主开启", memberFreePerDay, memberFreePerDay);
					MessageUtil.sendTextMessage(group, str, member.getMemberId(), MessageUtil.PRE);
					redisUtils.set(ConstantPool.MEMBER_FREE_USED + member.getMemberId(), memberUsedTime + 1);
					return;
				}

				// 进入试用环节
				chatConfig = pool.AI_CHAT_FREE_CONFIG;
				preset = chatConfig.getString("prompt_preset") + prompt + "。";
				keysArray = pool.AI_CHAT_KEYS.getJSONArray("default");
				// key截至到0点
				redisUtils.set(ConstantPool.TOTAL_FREE_USED, totalUsedTime + 1,
							   redisUtils.expire(ConstantPool.TOTAL_FREE_USED));
				redisUtils.set(ConstantPool.MEMBER_FREE_USED + member.getMemberId(), memberUsedTime + 1,
							   redisUtils.expire(ConstantPool.MEMBER_FREE_USED + member.getMemberId()));
//				log.info("群聊：{}无可用key");
//				MessageUtil.sendTextMessage(group, "群聊已无可用key，NLP功能将无法使用，请联系机器人管理员添加key");
//				return;
			}
		}


		Set<String> invalidKeys = new HashSet<>();
		for (Object o : keysArray) {
			String key = (String) o;

			// Set headers
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.setBearerAuth(key);

			// Set request body
			String requestBody = String.format("{\"prompt\":\"%s\",\"temperature\":%s,\"max_tokens\":%s,\"model\":\"%s\",\"stop\":\"%s\"}"
					, preset, chatConfig.getString("temperature"), chatConfig.getString("max_tokens")
					, chatConfig.getString("model"), chatConfig.getString("stop"));
			HttpEntity<String> request = new HttpEntity<>(requestBody, headers);

			JSONObject response = null;
			try {
				response = restTemplate.postForObject(pool.AI_CHAT_CONFIG.getString("api_url"), request, JSONObject.class);
				log.info("AI-response: {}", response);
				String responseText = response.getJSONArray("choices").getJSONObject(0).getString("text");
				int promptTokens = response.getJSONObject("usage").getInteger("prompt_tokens");

				if (responseText.contains("\n\n")) {
					responseText = responseText.substring(responseText.indexOf("\n\n") + 2);
				}

				if (!responseText.isEmpty()) {
					MessageUtil.sendTextMessage(group, responseText, messageId);
				}

				// 在Redis中更新该群的prompt
				if (promptTokens >= pool.AI_CHAT_CONFIG.getInteger("prompt_token_limit")) {
					log.info("群聊：{} prompt超过设定上限，将清空prompt", group.getGroupId());
					MessageUtil.sendTextMessage(group, "记忆数量超过上限，W已清除在本群的记忆");
					redisUtils.del(ConstantPool.GROUP_PROMPT_KEY + group.getGroupId());
				} else if (redisUtils.get(ConstantPool.GROUP_PROMPT_TURN + group.getGroupId()) != null) {
					String resultPrompt = tmpPreset + prompt + "。";
					log.info("resultPrompt: {}", resultPrompt);
					redisUtils.set(ConstantPool.GROUP_PROMPT_KEY + group.getGroupId(), resultPrompt);
				}
			} catch (HttpClientErrorException e) {
				log.info("AI-CHAT回复异常：{}", e.getMessage());
				// 判断是额度不足的类型
				if (e.getRawStatusCode() == 429 && e.getMessage().contains("insufficient_quota")) {
					invalidKeys.add(key);
					log.info("Key：{}已耗尽额度，自动切换到下一个Key", key);
					continue;
				}
			} catch (Exception e) {
				log.error("发生错误，将删除该群聊的Prompt：{}", group.getGroupId());
				redisUtils.del(ConstantPool.GROUP_PROMPT_KEY + group.getGroupId());
			}
			break;
		}

		for (String invalidKey : invalidKeys) {
			pool.AI_CHAT_KEYS.getJSONArray("default").remove(invalidKey);
			keysArray.remove(invalidKey);
		}
	}

	/**
	 * AI prompt操作
	 * @param group
	 * @param member
	 * @param valueDto
	 * @param content
	 */
	public void chatPrompt(Group group, MemberInfo member, KeyValueDto valueDto, String content, Member originMember) {
		if (!originMember.getRole().equals(ConstantPool.GROUP_OWNER) &&
				!originMember.getRole().equals(ConstantPool.GROUP_ADMIN)) {
			MessageUtil.sendTextMessage(group, "只有群主或管理员有权限操作记忆");
			return;
		}

		Matcher matcher = PathUtil.getRegex(valueDto.getKey(), content);
		String type = matcher.group(1), cont = matcher.group(2);
		if ("开启".equals(type)) {
			redisUtils.set(ConstantPool.GROUP_PROMPT_TURN + group.getGroupId(), "");
			MessageUtil.sendTextMessage(group, "W已开启上下文记忆功能");
		} else if ("关闭".equals(type)) {
			redisUtils.del(ConstantPool.GROUP_PROMPT_TURN + group.getGroupId());
			MessageUtil.sendTextMessage(group, "W已关闭上下文记忆功能");
		} else if ("重置".equals(type)) {
			int personalType = StringUtils.isNumeric(cont) && !cont.isEmpty() ? Integer.parseInt(cont) : 0;
			// 如果超过了预设下标，采取默认设置
			personalType = personalType >= pool.AI_CHAT_PRESET.size() ? 0 : personalType;

			redisUtils.del(ConstantPool.GROUP_PROMPT_KEY + group.getGroupId());
			redisUtils.set(ConstantPool.PERSONAL_SET_KEY + group.getGroupId(), personalType);
			MessageUtil.sendTextMessage(group, "W已将人格重置为 => " +
					pool.AI_CHAT_PRESET.getJSONObject(personalType).getString("n"));
		} else if ("查看".equals(type)) {
			Object prompt = redisUtils.get(ConstantPool.GROUP_PROMPT_KEY + group.getGroupId());
			MessageUtil.sendTextMessage(group, prompt == null ? "W在本群还没有记忆哦" : prompt.toString());
		} else {
			if (cont != null) {
				redisUtils.set(ConstantPool.GROUP_PROMPT_KEY + group.getGroupId(), cont.trim().replaceAll("[\r\n]", ""));
			}
			MessageUtil.sendTextMessage(group, "记忆更新完毕！");
		}
	}

	/**
	 * 一键摸摸
	 * @param group
	 * @param member
	 * @param valueDto
	 * @param content
	 */
	public void favorability(Group group, MemberInfo member, KeyValueDto valueDto, String content) {
		int original = member.getFavorability();
		if (original == ConstantPool.MAX_FAVORABILITY) {
			MessageUtil.sendTextMessage(group, "你的信赖值已满，不需要消耗摸摸券！", member.getMemberId(), MessageUtil.POST);
			return;
		}

		int maxNeed = ConstantPool.MAX_FAVORABILITY - member.getFavorability();
		int real = member.getTouchTicket() > maxNeed ? maxNeed : member.getTouchTicket();

		member.setFavorability(PathUtil.updateFavorability(member.getFavorability(), real));
		member.setTouchTicket(member.getTouchTicket() - real);

		String reply = "已消耗" + real + "张摸摸券，将你的信赖值由" + original + "提升至" + (original + real);
		MessageUtil.sendTextMessage(group, reply, member.getMemberId(), MessageUtil.POST);
	}

	/**
	 * 返回一个干员
	 * @param type 池子类型（普通 限定）
	 * @return
	 */
	private AgentInfo oneRecruit(String type) {
		/**
		 * 1. 摇号，确定星级
		 * 2. 摇号，是否进入up池
		 */
		Random priRandom = new Random();
		int levelNum = priRandom.nextInt(10000);
		int level = 0;
		if (levelNum < 200) {
			level = 6;
		} else if (levelNum < 1000) {
			level = 5;
		} else if (levelNum < 6000) {
			level = 4;
		} else {
			level = 3;
		}

		List<AgentInfo> preAgentList = null;
		if (type != null && pool.RECRUIT_ACTIVITY_NAME.contains(type)) {
			if (level == 6) {
				// 6星：50/70概率进入up池
				preAgentList = decideAgentList(variablePool.getLimitativeUp(), variablePool.getLimitative(), 6, pool.RECRUIT_ACTIVITY_PERCENT, 100);
			} else if (level == 5) {
				// 5星：50概率进入up池
				preAgentList = decideAgentList(variablePool.getLimitativeUp(), variablePool.getLimitative(), 5, 50, 100);
			} else {
				// 3、4星：直接取普通池
				preAgentList = variablePool.getLimitative().get(level);
			}
		} else {
			// 默认为标准寻访
			type = "标准寻访";

			if (level == 6 || level == 5) {
				// 5、6星：50概率进入up池
				preAgentList = decideAgentList(variablePool.getNormalUp(), variablePool.getNormal(), level, 50, 100);
			} else {
				// 3、4星：直接取普通池
				preAgentList = variablePool.getNormal().get(level);
			}
		}

		return PathUtil.randomList(preAgentList);
	}

	/**
	 * 确定待选干员清单
	 * @param level 干员星级
	 * @param limit 随机数限定范围
	 * @param size 随机数大小
	 * @return
	 */
	private List<AgentInfo> decideAgentList(Map<Integer, List<AgentInfo>> upPool, Map<Integer, List<AgentInfo>> pool, int level, int limit, int size) {
		int upNum = random.nextInt(size);
		if (upNum < limit) {
			// 进入up池
			return upPool.get(level);
		} else {
			// 进入非up池
			return pool.get(level);
		}
	}
}

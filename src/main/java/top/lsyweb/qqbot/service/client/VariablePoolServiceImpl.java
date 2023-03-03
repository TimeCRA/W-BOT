package top.lsyweb.qqbot.service.client;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import top.lsyweb.qqbot.dto.AgentDto;
import top.lsyweb.qqbot.dto.ChatPresetDto;
import top.lsyweb.qqbot.dto.KeyValueDto;
import top.lsyweb.qqbot.dto.VariablePool;
import top.lsyweb.qqbot.entity.*;
import top.lsyweb.qqbot.service.*;
import top.lsyweb.qqbot.util.PathUtil;
import top.lsyweb.qqbot.util.config.SystemConfigPool;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

@Slf4j
@Service
public class VariablePoolServiceImpl implements VariablePoolService
{
	private VariablePool variablePool;

	private GroupService groupService;
	private MemberService memberService;
	private KeyService keyService;
	private ValueService valueService;
	private AgentService agentService;
	private SystemConfigService systemConfigService;
	private ChatPresetService chatPresetService;
	private SystemConfigPool pool;



	@Autowired
	public VariablePoolServiceImpl(VariablePool variablePool, GroupService groupService, MemberService memberService,
								   KeyService keyService, ValueService valueService, AgentService agentService,
								   SystemConfigService systemConfigService, ChatPresetService chatPresetService, SystemConfigPool pool) {
		this.variablePool = variablePool;
		this.groupService = groupService;
		this.memberService = memberService;
		this.keyService = keyService;
		this.valueService = valueService;
		this.agentService = agentService;
		this.systemConfigService = systemConfigService;
		this.chatPresetService = chatPresetService;
		this.pool = pool;
	}

	/**
	 * 刷新所有变量
	 */
	@Override
	public void refreshAll() {
		refreshMemberInfo();
		refreshFailureGroupSet();
		refreshFailureMemberSet();
		refreshKeyValue();
		refreshJrrp();
		refreshAgentInfo();
		refreshRecruit();
		refreshChatPreset();
	}

	/**
	 * 刷新AI聊天预设信息
	 */
	private void refreshChatPreset() {
		List<ChatPreset> chatPresets = chatPresetService.list();
		Map<Integer, ChatPresetDto> chatPresetMap = chatPresets.stream().collect(
				Collectors.toConcurrentMap(ChatPreset::getPresetId, ChatPresetDto::new));
		variablePool.setChatPresetMap(chatPresetMap);
		log.info("刷新“AI预设人格”完成");
	}

	/**
	 * 刷新角色信息
	 */
	@Override
	public void refreshMemberInfo() {
		List<MemberInfo> memberInfoList = memberService.list();
		Map<Long, MemberInfo> memberInfoMap = memberInfoList.stream().collect(Collectors.toConcurrentMap(MemberInfo::getMemberId, o -> o));
		variablePool.setMemberInfoMap(memberInfoMap);
		log.info("刷新”角色信息映射“完成");
	}

	/**
	 * 角色信息定时同步到数据库
	 * 每隔5分钟执行一次
	 */
	@Scheduled(cron="0 */5 * * * ?")
	@Override
	public void syncMemberInfo() {
		Collection<MemberInfo> values = variablePool.getMemberInfoMap().values();
		// 批量更新角色数据
		memberService.updateBatchById(values);
		log.info("持久化角色数据完成");
	}

	/**
	 * 刷新 过滤群聊组
	 */
	@Override
	public void refreshFailureGroupSet() {
		QueryWrapper<GroupInfo> queryWrapper = new QueryWrapper<>();
		// 群聊状态为失效
		queryWrapper.eq("status", 0);
		List<GroupInfo> groupList = groupService.list(queryWrapper);
		Set<Long> collect = groupList.stream().map(GroupInfo::getGroupId).collect(Collectors.toSet());
		variablePool.setFailureGroupSet(collect);
		log.info("刷新“过滤群聊组”完成");
	}

	/**
	 * 刷新 过滤角色组（包含群聊和私聊）
	 */
	@Override
	public void refreshFailureMemberSet() {
		QueryWrapper<MemberInfo> queryWrapper = new QueryWrapper<>();
		// 过滤状态不为"不过滤"
		queryWrapper.ne("type", 0);
		List<MemberInfo> memberList = memberService.list(queryWrapper);
		Set<Long> publicSet = memberList.stream().filter(member -> (member.getType().equals(1) || member.getType().equals(3))).map(
				MemberInfo::getMemberId).collect(Collectors.toSet());
		Set<Long> privateSet = memberList.stream().filter(member -> (member.getType().equals(2) || member.getType().equals(3))).map(
				MemberInfo::getMemberId).collect(Collectors.toSet());
		variablePool.setPrivateFailureMemberSet(privateSet);
		variablePool.setPublicFailureMemberSet(publicSet);
		log.info("刷新“过滤角色组”完成");
	}

	/**
	 * 刷新 关键字列表（包含文本和正则）
	 */
	@Override
	public void refreshKeyValue() {
		QueryWrapper<KeyInfo> queryWrapper = new QueryWrapper<>();
		// 状态为生效的key
		queryWrapper.eq("status", 1);
		List<KeyInfo> keyList = keyService.list(queryWrapper);

		Map<String, KeyValueDto> keyMap = new HashMap<>(keyList.size());
		List<KeyValueDto> regexKeyList = new ArrayList<>(keyList.size());
		List<KeyValueDto> imageKeyList = new ArrayList<>(keyList.size());

		// 生成一个Value组Map
		Map<Integer, List<ValueInfo>> valueGroupMap = new HashMap<>();

		keyList.forEach(key -> {
			// 构造keyValueDto
			KeyValueDto keyValueDto = new KeyValueDto(key);
			if (key.getValueGroupId() == null || key.getValueGroupId().equals(0L)) {
				// 不属于回复组key
				QueryWrapper<ValueInfo> valueWrapper = new QueryWrapper<>();
				// 状态为生效的value
				valueWrapper.eq("status", 1);
				valueWrapper.eq("key_id", key.getId());
				List<ValueInfo> valueList = valueService.list(valueWrapper);
				keyValueDto.setValueList(valueList);
			} else {
				// 属于回复组key

				/**
				 * 1. 先在map中拿对应的回复组value
				 * 2. 如果没拿到，查询回复组value并写入map
				 */
				List<ValueInfo> valueGroups = valueGroupMap.get(key.getValueGroupId());
				if (valueGroups == null) {
					QueryWrapper<ValueInfo> valueWrapper = new QueryWrapper<>();
					// 状态为生效的value
					valueWrapper.eq("status", 1);
					valueWrapper.eq("value_group_id", key.getValueGroupId());
					List<ValueInfo> valueList = valueService.list(valueWrapper);
					valueGroupMap.put(key.getValueGroupId(), valueList);
					keyValueDto.setValueList(valueList);
				} else {
					keyValueDto.setValueList(valueGroups);
				}
			}


			if (keyValueDto.getType().equals(1)) {
				// 文本类型
				keyMap.put(keyValueDto.getKey(), keyValueDto);
			} else if (keyValueDto.getType().equals(2)) {
				// 正则类型
				regexKeyList.add(keyValueDto);
			} else if (keyValueDto.getType().equals(3)) {
				// 图像哈希码
				imageKeyList.add(keyValueDto);
			}
		});

		variablePool.setKeyMap(keyMap);
		variablePool.setRegexKeyList(regexKeyList);
		variablePool.setImageKeyList(imageKeyList);
		log.info("刷新“关键字列表”完成");
	}

	/**
	 * 定时刷新jrrp池（线程安全池）
	 * 每天04:00执行
	 */
	@Scheduled(cron="0 0 4 * * ?")
	@Override
	public void refreshJrrp() {
		Map<Long, String> oldMap = variablePool.getJrrpMap();
		variablePool.setJrrpMap(new ConcurrentHashMap<>(oldMap == null ? 16 : oldMap.size()));
		log.info("刷新“jrrp池”完成");
	}

	/**
	 * 定时刷新图片扰乱临时文件夹
	 * 每天04:00执行
	 */
	@Scheduled(cron="0 0 4 * * ?")
	@Override
	public void refreshImageDisturb() {
		File folder = new File(PathUtil.getBasePath() + "image_disturb/");
		try {
			FileUtils.deleteDirectory(folder);
			folder.mkdirs();
			log.info("刷新“ImageDisturb”完成");
		} catch (Exception e) {
			log.error("删除distrub文件夹失败", e.getMessage());
		}
	}

	/**
	 * 每天下午7点执行，按频次统计图片
	 */
//	@Scheduled(cron="0 0 19 * * ?")
//	public void statisticImageCode() {
//		// 默认采集20条信息
//		int maxSize = pool.STATISTICAL_IMAGE_LIMIT;
//
//		/**
//		 * 保存到文件中
//		 */
//		String path = PathUtil.getBasePath() + "image_statistic/";
//		SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd");
//		String format = sdf.format(new Date());
//		String outputPath = path + format;
//
//		// 处理统计信息
//		Map<Long, Map<Long, ImageCodeDto>> imageCodeMap = variablePool.getImageCodeMap();
//		imageCodeMap.keySet().forEach(groupId -> {
//
//			Map<Long, ImageCodeDto> dtoMap = imageCodeMap.get(groupId);
//			List<ImageCodeDto> collect = dtoMap.values().stream()
//											   .sorted((o1, o2) -> o2.getCount() - o1.getCount())
//											   .limit(maxSize).collect(Collectors.toList());
//
//			for (ImageCodeDto imageCodeDto : collect) {
//				try {
//					File file = new File(outputPath + "/" + groupId + "__" + imageCodeDto.getCount() + ".jpg");
//					if (!file.exists()) {
//						file.mkdirs();
//					}
//					BufferedImage read = ImageIO.read(new URL(imageCodeDto.getUrl()));
//					ImageIO.write(read, "jpg", file);
//				} catch (Exception e) {
//					e.printStackTrace();
//					log.error("处理统计信息异常，url:{}", imageCodeDto.getUrl());
//				}
//			}
//		});
//
//		// 清除统计信息
//		variablePool.setImageCodeMap(new HashMap<>());
//		log.info("统计图像频次信息完成");
//	}

	/**
	 * 刷新公开招募干员池
	 */
	@Override
	public void refreshAgentInfo() {
		QueryWrapper<AgentInfo> queryWrapper = new QueryWrapper<>();
		// 支持公开招募
		queryWrapper.eq("recruitment", 1);
		List<AgentInfo> agentList = agentService.list(queryWrapper);
		List<AgentDto> agentDtos = new ArrayList<>();
		agentList.forEach(agent -> agentDtos.add(new AgentDto(agent)));
		agentDtos.sort((o1, o2) -> o2.getLevel() - o1.getLevel());
		variablePool.setAgentDtoList(agentDtos);
		log.info("刷新“公开招募干员池”完成");
	}

	/**
	 * 刷新 寻访池（标准池和活动池）
	 */
	@Override
	public void refreshRecruit() {
		List<AgentInfo> allAgent = agentService.list();

		Map<Integer, List<AgentInfo>> normalUp = new HashMap<>();
		Map<Integer, List<AgentInfo>> normal = new HashMap<>();
		Map<Integer, List<AgentInfo>> limitativeUp = new HashMap<>();
		Map<Integer, List<AgentInfo>> limitative = new HashMap<>();

		// 初始化map池
		initialMap(normalUp, normal, limitativeUp, limitative);

		for (AgentInfo agentInfo : allAgent) {
			String name = agentInfo.getName();
			Integer level = agentInfo.getLevel();

			// 非限定干员
			if (agentInfo.getLimitative() == 0) {
				normal.get(level).add(agentInfo);
				limitative.get(agentInfo.getLevel()).add(agentInfo);
			}

			// 限定UP池
			if (pool.LIMITIVE_SIX_UP_NAMES.contains(name)) {
				// 限定六星
				limitativeUp.get(level).add(agentInfo);
			} else if (pool.LIMITIVE_RUN_SIX_UP_NAMES.contains(name)) {
				// 陪跑六星
				limitativeUp.get(level).add(agentInfo);
				limitative.get(level).remove(agentInfo);
				normal.get(level).remove(agentInfo);
			} else if (pool.LIMITIVE_WEIGHT_SIX_UP_NAMES.contains(name)) {
				// 五倍权值复刻六星
				List<AgentInfo> fiveStar = limitative.get(6);
				fiveStar.add(agentInfo);
				fiveStar.add(agentInfo);
				fiveStar.add(agentInfo);
				fiveStar.add(agentInfo);
				fiveStar.add(agentInfo);
			} else if (pool.LIMITIVE_FIVE_UP_NAMES.contains(name)) {
				// 五星
				limitativeUp.get(level).add(agentInfo);
				limitative.get(level).remove(agentInfo);
			}

			// 常规活动池
			if (pool.ACTIVITY_NEW_UP_NAMES.contains(name)) {
				// 新UP干员
				limitativeUp.get(level).add(agentInfo);
				limitative.get(level).remove(agentInfo);
				normal.get(level).remove(agentInfo);
			} else if (pool.ACTIVITY_OLD_UP_NAMES.contains(name)) {
				// 老UP干员
				limitativeUp.get(level).add(agentInfo);
				limitative.get(level).remove(agentInfo);
			}

			// 标准UP池
			if (pool.NORMAL_SIX_UP_NAMES.contains(name) || pool.NORMAL_FIVE_UP_NAMES.contains(name)) {
				// UP干员
				normalUp.get(level).add(agentInfo);
				normal.get(level).remove(agentInfo);
			}
		}
		variablePool.setLimitativeUp(limitativeUp);
		variablePool.setLimitative(limitative);
		variablePool.setNormalUp(normalUp);
		variablePool.setNormal(normal);

		log.info("刷新“寻访池”完成");
	}

	/**
	 * 刷新配置常量池
	 */
	@Override
	public void refreshSystemConfig() {
		List<SystemConfig> list = systemConfigService.list();
		Map<String, SystemConfig> collect = list.stream().collect(
				Collectors.toMap(SystemConfig::getKey, config -> config));
		variablePool.setSystemConfig(collect);
		log.info("刷新配置常量池完成");
	}

	/**
	 * 初始化寻访干员池
	 * @param mapList
	 */
	private void initialMap(Map<Integer, List<AgentInfo>> ... mapList) {
		for (Map<Integer, List<AgentInfo>> map : mapList) {
			map.put(1, new ArrayList<>());
			map.put(2, new ArrayList<>());
			map.put(3, new ArrayList<>());
			map.put(4, new ArrayList<>());
			map.put(5, new ArrayList<>());
			map.put(6, new ArrayList<>());
		}
	}

}

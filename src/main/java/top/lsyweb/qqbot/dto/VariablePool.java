package top.lsyweb.qqbot.dto;

import lombok.Data;
import org.springframework.stereotype.Component;
import top.lsyweb.qqbot.entity.AgentInfo;
import top.lsyweb.qqbot.entity.ChatPreset;
import top.lsyweb.qqbot.entity.MemberInfo;
import top.lsyweb.qqbot.entity.SystemConfig;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
@Data
public class VariablePool
{
	// 过滤群聊组，键为群号
	private Set<Long> failureGroupSet;

	// 角色信息映射
	private Map<Long, MemberInfo> memberInfoMap;

	// 群聊过滤角色组，键为qq号
	private Set<Long> publicFailureMemberSet;

	// 私聊过滤角色组，键为qq号
	private Set<Long> privateFailureMemberSet;

	// 文本关键字组，键为关键字，值为组合对象
	private Map<String, KeyValueDto> keyMap;

	// 正则关键字列表
	private List<KeyValueDto> regexKeyList;

	// 图片识别列表
	private List<KeyValueDto> imageKeyList;

	// 今日人品池
	private Map<Long, String> jrrpMap;

	// 公开招募干员池
	private List<AgentDto> agentDtoList;

	// 标准UP池
	private Map<Integer, List<AgentInfo>> normalUp;

	// 标准非UP池
	private Map<Integer, List<AgentInfo>> normal;

	// 活动UP池
	private Map<Integer, List<AgentInfo>> limitativeUp;

	// 活动非UP池
	private Map<Integer, List<AgentInfo>> limitative;

	/**
	 * AI预设消息
	 */
	private Map<Integer, ChatPresetDto> chatPresetMap;

	// 图片统计池 (Map<群号, Map<图片CODE, 次数>>)
	// private Map<Long, Map<Long, ImageCodeDto>> imageCodeMap = new HashMap<>();

	// 常量池
	private Map<String, SystemConfig> systemConfig;
}

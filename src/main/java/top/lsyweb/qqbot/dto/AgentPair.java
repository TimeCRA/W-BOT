package top.lsyweb.qqbot.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @Auther: Erekilu
 * @Date: 2022-01-26
 */
@Data
public class AgentPair
{
	/**
	 * 标签列表
	 */
	private List<String> tags;

	/**
	 * 干员列表
	 */
	private List<AgentDto> agentDtos = new ArrayList<>();

	/**
	 * 最低星级
	 */
	private Integer minLevel = 6;
}

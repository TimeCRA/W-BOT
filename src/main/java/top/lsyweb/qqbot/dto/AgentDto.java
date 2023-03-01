package top.lsyweb.qqbot.dto;

import com.alibaba.fastjson.JSON;
import lombok.Data;
import lombok.NoArgsConstructor;
import top.lsyweb.qqbot.entity.AgentInfo;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @Auther: Erekilu
 * @Date: 2022-01-26
 */
@Data
@NoArgsConstructor
public class AgentDto
{
	/**
	 * 干员名
	 */
	private String name;

	/**
	 * 干员星级（1-6）
	 */
	private Integer level;

	/**
	 * 词条JSON数组
	 * 格式：["高级资深干员","削弱","远程位"]
	 */
	private Set<String> tags;

	public AgentDto(AgentInfo agentInfo) {
		this.name = agentInfo.getName();
		this.level = agentInfo.getLevel();
		List<String> tagList = (List<String>) JSON.parse(agentInfo.getTags());
		this.tags = new HashSet<>(tagList);
	}
}

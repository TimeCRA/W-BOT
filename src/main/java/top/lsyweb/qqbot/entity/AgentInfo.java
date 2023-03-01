package top.lsyweb.qqbot.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @Auther: Erekilu
 * @Date: 2022-01-25
 */
@Data
@NoArgsConstructor
@TableName("tbl_agent_info")
public class AgentInfo
{
	@TableId
	private Integer id;

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
	private String tags;

	/**
	 * 是否为异格干员（0-否，n-上级干员id）
	 */
	private Integer isomer;

	/**
	 * 是否支持公开招募（0-否，1-是）
	 */
	private Integer recruitment;

	/**
	 * 是否为限定干员（0-否，1-是，2-活动干员，3-公招限定干员）
	 */
	private Integer limitative;

	private Date createTime;

	private Date updateTime;
}

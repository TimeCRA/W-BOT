package top.lsyweb.qqbot.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@TableName("tbl_system_config")
public class SystemConfig
{
	@TableId
	private Integer id;

	@TableField(value = "`key`")
	private String key;

	@TableField(value = "`value`")
	private String value;

	/**
	 * 默认值，在value为空的情况下会使用该值
	 */
	private String defaultValue;

	/**
	 * 配置描述
	 */
	@TableField(value = "`desc`")
	private String desc;

	private Date createTime;

	private Date updateTime;

	public SystemConfig(String key, String value, String desc) {
		this.key = key;
		this.value = value;
		this.defaultValue = value;
		this.desc = desc;
		this.createTime = new Date();
		this.updateTime = new Date();
	}
}

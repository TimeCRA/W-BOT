package top.lsyweb.qqbot.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 回复组
 */
@Data
@NoArgsConstructor
@TableName("tbl_value_group")
public class ValueGroup
{
	@TableId
	private Integer id;

	/**
	 * 回复组简述
	 */
	@TableField(value = "`desc`")
	private String desc;

	@JsonFormat(pattern="yyyy-MM-dd", timezone = "GMT+8")
	private Date createTime;
	@JsonFormat(pattern="yyyy-MM-dd", timezone = "GMT+8")
	private Date updateTime;

	public ValueGroup(String desc) {
		this.desc = desc;
		this.createTime = new Date();
		this.updateTime = new Date();
	}
}

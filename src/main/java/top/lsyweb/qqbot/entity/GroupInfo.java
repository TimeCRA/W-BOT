package top.lsyweb.qqbot.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 入驻群聊
 */
@Data
@TableName("tbl_group_info")
public class GroupInfo
{
	@TableId
	private Integer id;
	/**
	 * 群号
	 */
	private Long groupId;
	/**
	 * 群名称
	 */
	private String groupName;
	/**
	 * 群聊状态（0-失效，1-生效）
	 */
	private Integer status;
	/**
	 * 上一次群聊状态（0-失效，1-生效）
	 */
	private Integer hisStatus;

	@JsonFormat(pattern="yyyy-MM-dd", timezone = "GMT+8")
	private Date createTime;
	@JsonFormat(pattern="yyyy-MM-dd", timezone = "GMT+8")
	private Date updateTime;
}

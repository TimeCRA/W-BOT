package top.lsyweb.qqbot.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * QQ用户表
 */
@Data
@TableName("tbl_member_info")
public class MemberInfo
{
	@TableId
	private Integer id;
	/**
	 * QQ号
	 */
	private Long memberId;
	/**
	 * 角色昵称
	 */
	private String memberNickname;
	/**
	 * 过滤消息（0-不过滤，1-群聊过滤，2-私聊过滤，3-全部过滤）
	 */
	private Integer type;
	/**
	 * 过滤原因
	 */
	private String reason;
	/**
	 * 好感度（-100至200）
	 */
	private Integer favorability;
	/**
	 * 摸摸券数量
	 */
	private Integer touchTicket;
	/**
	 * 寻访券数量
	 */
	private Integer recruitTicket;

	@JsonFormat(pattern="yyyy-MM-dd", timezone = "GMT+8")
	private Date createTime;
	@JsonFormat(pattern="yyyy-MM-dd", timezone = "GMT+8")
	private Date updateTime;
}

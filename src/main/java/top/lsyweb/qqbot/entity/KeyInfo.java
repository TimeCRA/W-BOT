package top.lsyweb.qqbot.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 关键字配置信息
 */
@Data
@NoArgsConstructor
@TableName("tbl_key_info")
public class KeyInfo
{
	@TableId
	private Integer id;
	/**
	 * 关键字，为正则表达式或字符串
	 */
	@TableField(value = "`key`")
	private String key;
	/**
	 * 类型（1-纯文本，2-正则表达式，3-图像哈希）
	 */
	private Integer type;
	/**
	 * 过滤规则，格式：
	 * {
	 *     "include":[701927987,3102369809,1231213,2342309798]
	 * }
	 * 或
	 * {
	 *     "exclude":[123213123,123123345,6734656]
	 * }
	 * include表示包含的群号，exclude表示排除的群号。这两者只会单独出现
	 */
	@TableField(value = "`filter`")
	private String filter;
	/**
	 * 关键词状态（0-失效，1-生效）
	 */
	private Integer status;
	/**
	 * 是否需要被艾特（0-不需要，1-需要，2-都可）
	 */
	private Integer referenced;
	/**
	 * 是否需要特殊处理（0-不需要，1-需要）
	 */
	private Integer special;
	/**
	 * 图像相似度精度
	 */
	@TableField(value = "`precision`")
	private Integer precision;
	/**
	 * 图片识别结果
	 */
	private String ocrContent;
	/**
	 * 回复组编号
	 */
	private Integer valueGroupId;
	/**
	 * 权限限制
	 */
	private String authRange;

	@JsonFormat(pattern="yyyy-MM-dd", timezone = "GMT+8")
	private Date createTime;
	@JsonFormat(pattern="yyyy-MM-dd", timezone = "GMT+8")
	private Date updateTime;

	public KeyInfo(KeyInfo keyInfo) {
		this.id = keyInfo.id;
		this.key = keyInfo.key;
		this.type = keyInfo.type;
		this.filter = keyInfo.filter;
		this.status = keyInfo.status;
		this.referenced = keyInfo.referenced;
		this.special = keyInfo.special;
		this.precision = keyInfo.precision;
		this.ocrContent = keyInfo.ocrContent;
		this.valueGroupId = keyInfo.valueGroupId;
		this.authRange = keyInfo.authRange;
		this.createTime = keyInfo.createTime;
		this.updateTime = keyInfo.updateTime;
	}
}

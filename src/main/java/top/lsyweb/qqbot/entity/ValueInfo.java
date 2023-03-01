package top.lsyweb.qqbot.entity;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;
import top.lsyweb.qqbot.dto.ValueDto;

import java.util.Date;

/**
 * 关键字对应值配置信息
 */
@Data
@NoArgsConstructor
@TableName("tbl_value_info")
public class ValueInfo
{
	@TableId
	private Integer id;
	/**
	 * 值（包含文本，影像路径，以及混合信息）
	 *
	 * 混合类型格式：
	 * [
	 *     {"type":"text", "value":"点击环境变量，选择高级，按一下步骤操作：", "group":"1"},
	 *     {"type":"image", "value":"http://10.32.112.233/image-base/1.jpg", "group":"1"},
	 *     {"type":"text", "value":"最后把勾打上，点击提交就行", "group":"2"}
	 * ]
	 * type文本表示类型，value为对应类型的内容，group为所属消息簇，如果group有多个值，则会发送多条信息。
	 * 里如果示例json串，会在qq发两条消息，第一条是文字跟图片，第二条就是一行文字
	 */
	@TableField(value = "`value`")
	private String value;
	/**
	 * 值类型（0-文本，1-图片，2-音频，3-视频，10-混合类型）
	 */
	private Integer type;
	/**
	 * 回复简述（用于图片/音频/视频/混合类型）
	 */
	@TableField(value = "`desc`")
	private String desc;
	/**
	 * 关联tbl_key_info的id
	 */
	private Integer keyId;
	/**
	 * 角色名，方便定位到是哪位角色的资源
	 */
	@TableField(value = "`character`")
	private String character;
	/**
	 * 最低触发好感度（如无好感度限制，则为-100）
	 */
	private Integer minFavorability;
	/**
	 * 最高触发好感度（如无好感度限制，则为100）
	 */
	private Integer maxFavorability;
	/**
	 * 状态（0-失效，1-生效）
	 */
	private Integer status;
	/**
	 * 是否需要艾特对方（0-否，1-是）
	 */
	private Integer needReference;
	/**
	 * 过滤规则（同KeyInfo）
	 */
	@TableField(value = "`filter`")
	private String filter;
	/**
	 * 回复组编号
	 */
	private Integer valueGroupId;

	@JsonFormat(pattern="yyyy-MM-dd", timezone = "GMT+8")
	private Date createTime;
	@JsonFormat(pattern="yyyy-MM-dd", timezone = "GMT+8")
	private Date updateTime;

	public ValueInfo(ValueDto valueDto) {
		this((ValueInfo) valueDto);
		if (valueDto.getType().equals(10)) {
			Object valueList = JSON.toJSON(valueDto.getValueList());
			this.value = JSON.toJSONString(valueList);
		}
	}

	public ValueInfo(ValueInfo valueInfo) {
		this.id = valueInfo.id;
		this.value = valueInfo.value;
		this.type = valueInfo.type;
		this.desc = valueInfo.desc;
		this.keyId = valueInfo.keyId;
		this.character = valueInfo.character;
		this.minFavorability = valueInfo.minFavorability;
		this.maxFavorability = valueInfo.maxFavorability;
		this.status = valueInfo.status;
		this.needReference = valueInfo.needReference;
		this.filter = valueInfo.filter;
		this.valueGroupId = valueInfo.valueGroupId;
		this.createTime = valueInfo.createTime;
		this.updateTime = valueInfo.updateTime;
	}
}

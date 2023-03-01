package top.lsyweb.qqbot.dto;

import top.lsyweb.qqbot.entity.KeyInfo;
import top.lsyweb.qqbot.entity.ValueInfo;

import java.util.Date;
import java.util.List;

public class KeyValueDto extends KeyInfo
{
	/**
	 * 值列表
	 */
	private List<ValueInfo> valueList;

	public List<ValueInfo> getValueList() {
		return valueList;
	}

	public void setValueList(List<ValueInfo> valueList) {
		this.valueList = valueList;
	}

	public KeyValueDto(KeyInfo keyInfo) {
		super(keyInfo);
	}
}

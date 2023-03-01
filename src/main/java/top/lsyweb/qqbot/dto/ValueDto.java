package top.lsyweb.qqbot.dto;

import com.alibaba.fastjson.JSON;
import top.lsyweb.qqbot.entity.ValueInfo;
import top.lsyweb.qqbot.util.PathUtil;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ValueDto extends ValueInfo
{
	List<Map<String, Object>> valueList;

	public ValueDto() {
	}

	private ValueDto(ValueInfo valueInfo) {
		super(valueInfo);
	}

	public List<Map<String, Object>> getValueList() {
		return valueList;
	}

	public void setValueList(List<Map<String, Object>> valueList) {
		this.valueList = valueList;
	}

	/**
	 * 1. 将json字符串转化为对象
	 * 2. 拼接图片前缀
	 * @param valueInfoList
	 * @return
	 */
	public static List<ValueDto> parseValueDtoList(List<ValueInfo> valueInfoList) {
		List<ValueDto> result = valueInfoList.stream().map(valueInfo -> {
			ValueDto valueDto = new ValueDto(valueInfo);

			if (valueDto.getType().equals(10)) {
				// 如果是混合类型，转json串里的东西
				valueDto.setValueList((List<Map<String, Object>>) JSON.parse(valueInfo.getValue()));
				valueDto.setValue(null);
				valueDto.getValueList().forEach(valueMap -> {
					if (valueMap.get("type").equals("image")) {
						valueMap.put("value", PathUtil.getBasePath() + valueMap.get("value"));
					}
				});
			} else if (!valueDto.getType().equals(0)) {
				// 如果是图片、音频、视频
				valueDto.setValue(PathUtil.getBasePath() + valueDto.getValue());
			}
			return valueDto;
		}).collect(Collectors.toList());
		return result;
	}
}

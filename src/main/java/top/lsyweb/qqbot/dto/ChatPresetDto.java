package top.lsyweb.qqbot.dto;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import top.lsyweb.qqbot.entity.ChatPreset;

/**
 * @Auther: Erekilu
 * @Date: 2023-03-03
 */
public class ChatPresetDto extends ChatPreset
{
	private final JSONArray messageArray;


	public ChatPresetDto() {
		messageArray = new JSONArray();
	}

	public ChatPresetDto(ChatPreset chatPreset) {
		super(chatPreset);
		this.messageArray = JSON.parseArray(chatPreset.getMessages());
	}

	public JSONArray getMessageArray() {
		return messageArray;
	}
}

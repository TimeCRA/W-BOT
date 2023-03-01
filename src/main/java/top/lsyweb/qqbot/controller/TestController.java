package top.lsyweb.qqbot.controller;

import com.zhuangxv.bot.core.Group;
import com.zhuangxv.bot.core.Member;
import com.zhuangxv.bot.message.MessageChain;
import com.zhuangxv.bot.message.support.ImageMessage;
import com.zhuangxv.bot.message.support.RecordMessage;
import com.zhuangxv.bot.message.support.UnknownMessage;
import com.zhuangxv.bot.message.support.VideoMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;

import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Controller
public class TestController
{
	private ConcurrentHashMap<Long, String> concurrentHashMap = new ConcurrentHashMap();


	public MessageChain buildJson() {
		MessageChain messages = new MessageChain();
		UnknownMessage unknownMessage = new UnknownMessage();
		messages.add(unknownMessage);
		unknownMessage.setJson("{\"type\":\"tts\",\"data\":{\"text\":\"你好，我是w\"}}");
		return messages;
	}

	public MessageChain buildRecord() {
		MessageChain messages = new MessageChain();
		RecordMessage recordMessage = new RecordMessage("https://downsc.chinaz.net/Files/DownLoad/sound1/202112/15162.mp3");
		// recordMessage.setUrl("https://downsc.chinaz.net/Files/DownLoad/sound1/202112/15162.mp3");
		messages.add(recordMessage);
		return messages;
	}

	public MessageChain jrrp(String msg, Member member) {
		MessageChain messages = new MessageChain();
		if ("jrrp".equals(msg)) {
			messages.at(member.getUserId());
			messages.text(concurrentHashMap.getOrDefault(member.getUserId(), jrrp(member.getUserId())));
		}
		return messages;
	}

	public MessageChain buildVideo() {
		MessageChain messages = new MessageChain();
		VideoMessage videoMessage = new VideoMessage();
		// recordMessage.setUrl("https://downsc.chinaz.net/Files/DownLoad/sound1/202112/15162.mp3");
		videoMessage.setFile("https://downsc.chinaz.net/Files/DownLoad/sound1/202112/15162.mp3");
		messages.add(videoMessage);
		return messages;
	}

	public String jrrp(Long userId) {
		Random random = new Random();
		int rp = random.nextInt(101);
		String post = null;
		if (rp < 20) {
			post = "，建议重开";
		} else if (rp < 40) {
			post = "，建议玩穴居人积积德";
		} else if (rp < 60) {
			post = "，下一把穴居人必配自闭法术卡组";
		} else if (rp < 80) {
			post = "，穴居人亲了你一口";
		} else {
			post = "，下一包必出金穴居人";
		}
		String result = "今日人品：" + rp + post;
		concurrentHashMap.put(userId, result);
		return result;
	}
}

package top.lsyweb.qqbot.util;

import com.zhuangxv.bot.core.Group;
import com.zhuangxv.bot.message.MessageChain;
import com.zhuangxv.bot.message.support.ImageMessage;
import com.zhuangxv.bot.message.support.TextMessage;
import com.zhuangxv.bot.message.support.VideoMessage;
import jdk.nashorn.internal.runtime.logging.Logger;
import lombok.extern.log4j.Log4j;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;

/**
 * @Auther: Erekilu
 * @Date: 2022-01-24
 */
@Slf4j
public class MessageUtil
{
	public static final int PRE = -1;
	public static final int POST = 1;

	/**
	 * 音频消息
	 * @param messageChain
	 * @param value
	 */
	public static void buildVideoMessage(MessageChain messageChain, String value) {
		messageChain.add(new VideoMessage(PathUtil.getOssPath() + PathUtil.getBasePath() + value));
	}

	/**
	 * 图片消息
	 * @param messageChain
	 * @param value
	 */
	public static void buildImageMessage(MessageChain messageChain, String value) {
		messageChain.add(new ImageMessage(PathUtil.getOssPath() + PathUtil.getBasePath() + value));
	}

	/**
	 * 文本消息
	 * @param messageChain
	 * @param value
	 */
	public static void buildTextMessage(MessageChain messageChain, String value) {
		messageChain.add(new TextMessage(value));
	}

	/**
	 * 发送艾特文本消息
	 * @param group
	 * @param content
	 * @param userId at用户qq号
	 * @param where -1代表前置@，1代表后置@
	 */
	public static void sendTextMessage(Group group, String content, Long userId, int where) {
		MessageChain messages = new MessageChain();
		if (where == PRE) {
			messages.at(userId);
			// 前置艾特需要换行
			messages.text(PathUtil.getLine());
		}
		buildTextMessage(messages, content);
		if (where == POST) {
			messages.at(userId);
		}
		group.sendMessage(messages);
		log.info("发送消息：{}", content);
	}

	/**
	 * 发送回复消息
	 * @param group
	 * @param content
	 * @param messageId
	 */
	public static void sendTextMessage(Group group, String content, int messageId) {
		MessageChain messages = new MessageChain();
		messages.reply(messageId);
		buildTextMessage(messages, content);
		group.sendMessage(messages);
		log.info("发送消息：{}", content);
	}

	public static void sendTextMessage(Group group, String content) {
		sendTextMessage(group, content, null, 0);
	}
}

package top.lsyweb.qqbot.controller;

import com.zhuangxv.bot.annotation.GroupMessageHandler;
import com.zhuangxv.bot.core.Group;
import com.zhuangxv.bot.core.Member;
import com.zhuangxv.bot.message.MessageChain;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import top.lsyweb.qqbot.dto.ResultResponse;
import top.lsyweb.qqbot.service.client.GroupMessageService;

import java.util.Map;

@Slf4j
@Controller
public class GroupMessageController
{
	@Autowired
	private GroupMessageService groupMessageService;

	/**
	 * 群聊信息总入口
	 * @param msg          消息的字符串形式
	 * @param group        群组对象
	 * @param member       群成员对象
	 * @param messageChain 消息链
	 * @param messageId    消息id
	 */
	@GroupMessageHandler
	public void accept(String msg, Group group, Member member, MessageChain messageChain, int messageId) {
		groupMessageService.accept(msg, group, member, messageChain, messageId);
	}


	/**
	 * 发送全局通知
	 * @param value 通知内容
	 * @param groups 群聊列表，用英文逗号隔开，如（123123,123456,123789）。如为所有生效群聊，则该值为""
	 * @return
	 */
	@PostMapping("/notice")
	@ResponseBody
	public ResultResponse notice(@RequestBody Map<String, String> param) {
		groupMessageService.notice(param.get("value"), param.get("groups"), Boolean.valueOf(param.get("atAll")));
		return ResultResponse.success();
	}
}

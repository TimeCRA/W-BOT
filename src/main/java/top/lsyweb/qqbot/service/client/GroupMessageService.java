package top.lsyweb.qqbot.service.client;

import com.zhuangxv.bot.core.Group;
import com.zhuangxv.bot.core.Member;
import com.zhuangxv.bot.message.MessageChain;

public interface GroupMessageService
{
	public void accept(String msg, Group group, Member member, MessageChain messageChain, int messageId);

	public void notice(String value, String groups, boolean atAll);
}

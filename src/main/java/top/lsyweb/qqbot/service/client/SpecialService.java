package top.lsyweb.qqbot.service.client;

import com.zhuangxv.bot.core.Group;
import com.zhuangxv.bot.core.Member;
import com.zhuangxv.bot.message.MessageChain;
import top.lsyweb.qqbot.dto.KeyValueDto;
import top.lsyweb.qqbot.entity.MemberInfo;

/**
 * @Auther: Erekilu
 * @Date: 2022-01-24
 */
public interface SpecialService
{
	/**
	 * 签到
	 * @param group
	 * @param member
	 * @param valueDto
	 * @param content
	 */
	void signIn(Group group, MemberInfo member, KeyValueDto valueDto, String content);

	/**
	 * 查询库存
	 * @param group
	 * @param member
	 * @param valueDto
	 * @param content
	 */
	void getRepository(Group group, MemberInfo member, KeyValueDto valueDto, String content);

	/**
	 * 查询本群黑名单
	 * @param group
	 * @param member
	 * @param valueDto
	 * @param content
	 */
	void blackList(Group group, MemberInfo member, KeyValueDto valueDto, String content);

	/**
	 * 查询今日人品
	 * @param memberId 角色id
	 */
	void jrrp(Group group, Long memberId);

	/**
	 * 公开招募
	 * @param group
	 * @param member
	 * @param valueDto
	 * @param content
	 */
	void openRecruitment(Group group, MemberInfo member, KeyValueDto valueDto, String content);

	/**
	 * ocr公开招募
	 * @param group
	 * @param member
	 * @param valueDto
	 * @param url
	 */
	void ocrOpenRecruitment(Group group, MemberInfo member, KeyValueDto valueDto, String url);

	/**
	 * 模拟招募
	 */
	void simulationRecruit(Group group, MemberInfo member, KeyValueDto valueDto, String content);

	/**
	 * 设置中转目标
	 * @param valueDto
	 * @param content
	 */
	void setTransitTarget(KeyValueDto valueDto, String content);

	/**
	 * 中转消息
	 * @param messageChain
	 */
	void messageTransit(String msg, MessageChain messageChain, int messageId);

	/**
	 * 骰数
	 */
	void randomNumber(Group group, MemberInfo member, KeyValueDto valueDto, String content);

	/**
	 * 查询信赖值
	 */
	void getFavorability(Group group, MemberInfo member, KeyValueDto valueDto, String content);

	/**
	 * 涩图API
	 */
	Boolean lolicon(Group group, MemberInfo member, KeyValueDto valueDto, String content);

	/**
	 * 撤回消息
	 */
	boolean deleteMsg(MessageChain messageChain, Group group, String msg);

	/**
	 * 游戏王查卡
	 * @param group
	 * @param member
	 * @param valueDto
	 * @param content
	 */
	void yugiohSearch(Group group, MemberInfo member, KeyValueDto valueDto, String content);

	/**
	 * AI回复
	 * @param group
	 * @param member
	 * @param valueDto
	 * @param content
	 */
	void gptChat(Group group, MemberInfo member, KeyValueDto valueDto, String content, int messageId);

	/**
	 * 一键摸摸
	 * @param group
	 * @param member
	 * @param valueDto
	 * @param content
	 */
	void favorability(Group group, MemberInfo member, KeyValueDto valueDto, String content);

	/**
	 * AI prompt操作
	 * @param group
	 * @param member
	 * @param valueDto
	 * @param content
	 */
	void chatPrompt(Group group, MemberInfo member, KeyValueDto valueDto, String content, Member originMember);

	/**
	 * 刷新系统内缓存或刷新go-cqhttp缓存
	 * @param group
	 * @param member
	 * @param valueDto
	 * @param content
	 */
	void innerRefresh(Group group, MemberInfo member, KeyValueDto valueDto, String content);

	/**
	 * 群聊开闭
	 * @param group
	 * @param member
	 * @param msg
	 */
	void groupTurn(Group group, Member member, String msg);
}

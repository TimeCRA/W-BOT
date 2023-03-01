package top.lsyweb.qqbot.service.client;

public interface VariablePoolService
{
	/**
	 * 刷新 所有变量池数据
	 */
	void refreshAll();

	/**
	 * 刷新角色信息
	 */
	void refreshMemberInfo();

	/**
	 * 角色信息定时同步到数据库
	 */
	void syncMemberInfo();

	/**
	 * 刷新 过滤群聊组
	 */
	void refreshFailureGroupSet();

	/**
	 * 刷新 过滤角色组（包含群聊和私聊）
	 */
	void refreshFailureMemberSet();

	/**
	 * 刷新 关键字列表（包含文本和正则）
	 */
	void refreshKeyValue();

	/**
	 * 定时刷新jrrp池（线程安全池）
	 * 每天04:00执行
	 */
	void refreshJrrp();

	/**
	 * 定时刷新图片扰乱临时文件夹
	 * 每天04:00执行
	 */
	void refreshImageDisturb();

	/**
	 * 刷新公开招募干员池
	 */
	void refreshAgentInfo();

	/**
	 * 刷新 寻访池（标准池和活动池）
	 */
	void refreshRecruit();

	/**
	 * 刷新配置常量池
	 */
	void refreshSystemConfig();
}

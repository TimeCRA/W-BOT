package top.lsyweb.qqbot.util;

public interface ConstantPool
{
	//==============================图像处理相关=============================//
	/**
	 * ocr识别的嵌套内容相关
	 */
	String WORD_RESULTS = "words_result";
	String WORD = "words";

	/**
	 * ocr图片识别临时表
	 */
	String OCR_TEMP_PATH = "ocr_temp/";

	/**
	 * 涩图临时存放路径
	 */
	String SETU = "setu/";

	/**
	 * 涩图限流(天为单位，细粒到 群聊+用户)
	 */
	String SETU_LIMIT = "setu_limit_";

	//==============================签到相关=============================//
	/**
	 * 控制签到频次redis Key
	 */
	String SIGN_IN_KEY = "sign_in_";

	//==============================公开招募相关=============================//

	/**
	 * 公开招募限流
	 */
	String OPEN_RECRUITMENT_KEY = "open_recruitment_";

	String SENIOR_AGENT = "资深干员";
	String PRO_SENIOR_AGENT = "高级资深干员";

	String AGENT_SEARCH_ONE = "单抽";
	String AGENT_SEARCH_TEN = "十连";

	//==============================中转处理相关=============================//

	/**
	 * 中转目标key
	 */
	String TRANSIT_KEY = "transit_target";

	//==============================回复/消息处理相关=============================//

	/**
	 * 群聊开闭正则
	 */
	String GROUP_TURN_REGEX = "(?:w|W|达不溜)\\s?(上班|下班)\\s?(\\d*)";

	/**
	 * 发言人角色：群主/管理员
	 */
	String GROUP_OWNER = "owner";
	String GROUP_ADMIN = "admin";

	/**
	 * 角色类型，0-普通用户，1-群内管理员，2-群内群主，3-机器人管理员
	 */
	Integer MEMBER_AUTH_NORMAL = 0;
	Integer MEMBER_AUTH_GROUP_ADMIN = 1;
	Integer MEMBER_AUTH_GROUP_OWNER = 2;
	Integer MEMBER_AUTH_SUPER_ADMIN = 3;

	/**
	 * 过滤类型：
	 * 0：无过滤
	 * 1：群聊过滤
	 * 2：私聊过滤
	 * 3：全部过滤
	 */
	int GROUP_FILTER_NONE = 0;
	int GROUP_FILTER_PUBLIC = 1;
	int GROUP_FILTER_PRIVATE = 2;
	int GROUP_FILTER_ALL = 3;

	/**
	 * 接收消息时，是否需要被AT
	 * 0-不需要，1-需要，2-都可
	 */
	int NEED_REFERENCED_NO = 0;
	int NEED_REFERENCED_YES = 1;
	int NEED_REFERENCED_BOTH = 2;

	/**
	 * 群号、qq号过滤规则
	 */
	String GROUP_INCLUDE = "groupInclude";
	String GROUP_EXCLUDE = "groupExclude";
	String MEMBER_INCLUDE = "memberInclude";
	String MEMBER_EXCLUDE = "memberExclude";
	String MEMBER_WHITE_LIST = "memberWhiteList";
	String MEMBER_BLACK_LIST = "memberBlackList";


	/**
	 * 混合消息类型标识
	 */
	int MULTIPART_TYPE = 10;
	/**
	 * 混合消息类型
	 */
	String MULTIPART_KEY = "type";
	String MULTIPART_VALUE = "value";
	String MULTIPART_TEXT = "text";
	String MULTIPART_IMAGE = "image";
	/**
	 * 混合类型回复所属哪一条回复
	 * 目前支持3条分隔开的回复
	 */
	String MULTIPART_BELONG = "group";
	String MULTIPART_ONE = "1";
	String MULTIPART_TWO = "2";
	String MULTIPART_THREE = "3";


	//==============================好感度相关=============================//
	/**
	 * 最高/最低好感度
	 */
	int MAX_FAVORABILITY = 200;
	int MIN_FAVORABILITY = -100;

	/**
	 * 统计触发次数KEY
	 * 提升好感
	 */
	String TRIGGER_TIME_KEY = "trigger_time_";


	//==============================控制/熔断相关=============================//

	/**
	 * 群聊开关KEY
	 * 群聊开关正则
	 */
	String GROUP_TURN_KEY = "group_off_";
	// String GROUP_TURN_REGEX = "bot\\s*(on|off)\\s*(\\d*)";
	/**
	 * bot on/off判定
	 */
	String GROUP_TURN_OFF = "off";
	String GROUP_TRUN_OFF_ALIA = "下班";
	String GROUP_TURN_ON = "on";
	String GROUP_TURN_ON_ALIA = "上班";

	/**
	 * 刷新缓存指令
	 */
	String KEY_REFRESH = "/refresh";
	String GROUP_REFRESH = "/flush";

	/**
	 * 熔断检测队列key
	 */
	String GROUP_FUSING_STATISTIC_KEY = "group_";
	String GROUP_FUSING_KEY = "fusing_group_";

	/**
	 * 规定时间内最大的消息条数
	 */
	long MAX_LIST_SIZE = 100L;
	/**
	 * 群聊指定限制时间 ，单位 秒 /
	 */
	long LIMIT_SECONED_TIME = 60 * 1000L;
	/**
	 * 熔断时间
	 */
	long MELTING_TIME = 60 * 10L;

	//==============================AI相关=============================//
	/**
	 * 不同群聊的AI前缀 Redis-Key
	 */
	String GROUP_PROMPT_KEY = "group_prompt_";

	/**
	 * 是否开启上下文对话
	 */
	String GROUP_PROMPT_TURN = "group_prompt_turn_";

	/**
	 * 机器人设定类型 Redis-Key
	 */
	String PERSONAL_SET_KEY = "personal_set_key_";

	/**
	 * NLP每人每天的免费次数统计 Redis-Key
	 */
	String MEMBER_FREE_USED = "member_free_used_";

	/**
	 * NLP总共每天的免费次数统计 Redis-Key
	 */
	String TOTAL_FREE_USED = "total_free_used";
}

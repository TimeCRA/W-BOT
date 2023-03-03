package top.lsyweb.qqbot.util.config;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SystemConfigPool
{
	private SystemConfigs configs;

	/**
	 * 机器人称呼
	 */
	public List<String> NAMES;

	/**
	 * 机器人主人qq号 798166259
	 */
	public List<Long> MASTER_QQ;

	/**
	 * 关闭机器人默认时间(分) 10
	 */
	public int BOT_OFF_DEFAULT_TIME;

	/**
	 * 前x张图片参与统计 20
	 */
	public int STATISTICAL_IMAGE_LIMIT;

	/**
	 * 图像默认相似度 5
	 */
	public int DEFAULT_IMAGE_PRECISION;

	//==============================公开招募相关=============================//

	/**
	 * 公招模板keyId [45,46]
	 */
	public List<Integer> OPEN_RECRUITMENT_IDS;

	/**
	 * 公招尺寸映射
	 * {"45":[0.31,0.47,0.35,0.22],"46":[0.26,0.45,0.44,0.26]}
	 */
	public JSONObject OPEN_RECRUITMENT_SIZE_MAP;

	/**
	 * 公招OCR易错词替换
	 * {"高级资深王员":"高级资深干员","高级资深干贵":"高级资深干员"}
	 */
	public JSONObject OPEN_RECRUITMENT_CONVERT_MAP;

	/**
	 * 公开招募次数限制 4
	 */
	public int OPEN_RECRUITMENT_LIMIT;

	/**
	 * 公开招募时间限制(秒) 600
	 * 结合上面就是，在600s内只能调用4次公开招募
	 */
	public int OPEN_RECRUITMENT_SECOND;

	//==============================涩图相关=============================//

	/**
	 * 涩图词条转换map
	 */
	public JSONObject SETU_CONVERT_MAP;

	/**
	 * 图源配置
	 */
	public int SETU_IMAGE_SOURCE;

	/**
	 * 调用路径配置
	 */
	public String SETU_QUALITY;

	/**
	 * 每小时限流
	 */
	public JSONObject SETU_LIMIT;

	/**
	 * 远端涩图干扰方式（0-不干扰，1-左旋90°，2-加水印）
	 */
	public int SETU_DISTURB;

	/**
	 * 图片最大宽高
	 */
	public List<Integer> SETU_MAX_WIDTH_HEIGHT;

	//==============================寻访相关=============================//

	/**
	 * 寻访活动池名称（包含限定） ["____"]
	 */
	public List<String> RECRUIT_ACTIVITY_NAME;

	/**
	 * 六星池UP概率 50
	 * 如果是限定池，这里调整为70
	 */
	public int RECRUIT_ACTIVITY_PERCENT;

	/**
	 * 活动池新UP干员（不包含限定）[]
	 */
	public List<String> ACTIVITY_NEW_UP_NAMES;

	/**
	 * 活动池老UP干员（不包含限定）[]
	 */
	public List<String> ACTIVITY_OLD_UP_NAMES;

	/**
	 * 六星标准池UP干员（不包含限定）["泥岩","森蚺"]
	 */
	public List<String> NORMAL_SIX_UP_NAMES;

	/**
	 * 五星标准池UP干员（不包含限定）["桑葚","断崖","槐琥"]
	 */
	public List<String> NORMAL_FIVE_UP_NAMES;

	/**
	 * 六星限定池限定UP干员 []
	 */
	public List<String> LIMITIVE_SIX_UP_NAMES;

	/**
	 * 六星限定池陪跑UP干员 []
	 */
	public List<String> LIMITIVE_RUN_SIX_UP_NAMES;

	/**
	 * 六星限定池5倍倍率干员 []
	 */
	public List<String> LIMITIVE_WEIGHT_SIX_UP_NAMES;

	/**
	 * 五星限定池UP干员 []
	 */
	public List<String> LIMITIVE_FIVE_UP_NAMES;

	/**
	 * 寻访数量替换词
	 */
	public JSONObject RECRUIT_CONVERT_MAP;

	//==============================好感度相关=============================//

	/**
	 * 能触发直接增加好感度的操作key_id [36]
	 */
	public List<Integer> ADD_FAVORABILITY_LIST;

	/**
	 * 触发增加好感度的最低生效次数 10
	 */
	public int TRIGGER_TIME;

	/**
	 * 每日签到赠送的摸摸券数量 3
	 */
	public int SIGN_IN_TOUCH_NUM;

	/**
	 * 每日签到赠送的寻访凭证数量 50
	 */
	public int SIGN_IN_RECRUIT_NUM;

	//==============================游戏王相关=============================//

	/**
	 * 游戏王卡图前缀
	 */
	public JSONObject YUGIOH_URL_PREFIX;

	/**
	 * 游戏王昵称替换
	 */
	public JSONObject YUGIOH_NAME_MAP;

	//==============================AI相关=============================//

	/**
	 * AI-CHAT相关设置
	 * @param configs
	 */
	public JSONObject AI_CHAT_CONFIG;

	/**
	 * AI-CHAT API-KEY
	 * 每个群对应一个KEY数组
	 */
	public JSONObject AI_CHAT_KEYS;

	/**
	 * AI-CHAT免费版设置
	 */
	public JSONObject AI_CHAT_FREE_CONFIG;

	@Autowired
	public SystemConfigPool(SystemConfigs configs) {
		this.configs = configs;
	}

	/**
	 * 从数据库中获取所有配置
	 */
	public void refresh() {
		this.NAMES = configs.getList("names", String.class);
		this.MASTER_QQ = configs.getList("master_qq", Long.class);
		this.BOT_OFF_DEFAULT_TIME = configs.getInt("bot_off_default_time");
		this.STATISTICAL_IMAGE_LIMIT = configs.getInt("statistical_image_limit");
		this.DEFAULT_IMAGE_PRECISION = configs.getInt("default_image_precision");

		this.OPEN_RECRUITMENT_IDS = configs.getList("open_recruitment_ids", Integer.class);
		this.OPEN_RECRUITMENT_SIZE_MAP = configs.getJsonObject("open_recruitment_size_map");
		this.OPEN_RECRUITMENT_CONVERT_MAP = configs.getJsonObject("open_recruitment_convert_map");
		this.OPEN_RECRUITMENT_LIMIT = configs.getInt("open_recruitment_limit");
		this.OPEN_RECRUITMENT_SECOND = configs.getInt("open_recruitment_second");

		this.SETU_CONVERT_MAP = configs.getJsonObject("setu_convert_map");
		this.SETU_IMAGE_SOURCE = configs.getInt("setu_image_soruce");
		this.SETU_QUALITY = configs.getString("setu_quality");
		this.SETU_LIMIT = configs.getJsonObject("setu_limit");
		this.SETU_DISTURB = configs.getInt("setu_disturb");
		this.SETU_MAX_WIDTH_HEIGHT = configs.getList("setu_max_width_height", Integer.class);

		this.RECRUIT_ACTIVITY_NAME = configs.getList("recruit_activity_name", String.class);
		this.RECRUIT_ACTIVITY_PERCENT = configs.getInt("recruit_activity_percent");
		this.ACTIVITY_NEW_UP_NAMES = configs.getList("activity_new_up_names", String.class);
		this.ACTIVITY_OLD_UP_NAMES = configs.getList("activity_old_up_names", String.class);
		this.NORMAL_SIX_UP_NAMES = configs.getList("normal_six_up_names", String.class);
		this.NORMAL_FIVE_UP_NAMES = configs.getList("normal_five_up_names", String.class);
		this.LIMITIVE_SIX_UP_NAMES = configs.getList("limitive_six_up_names", String.class);
		this.LIMITIVE_RUN_SIX_UP_NAMES = configs.getList("limitive_run_six_up_names", String.class);
		this.LIMITIVE_WEIGHT_SIX_UP_NAMES = configs.getList("limitive_weight_six_up_names", String.class);
		this.LIMITIVE_FIVE_UP_NAMES = configs.getList("limitive_five_up_names", String.class);
		this.RECRUIT_CONVERT_MAP = configs.getJsonObject("recruit_convert_map");

		this.ADD_FAVORABILITY_LIST = configs.getList("add_favorability_list", Integer.class);
		this.TRIGGER_TIME = configs.getInt("trigger_time");
		this.SIGN_IN_TOUCH_NUM = configs.getInt("sign_in_touch_num");
		this.SIGN_IN_RECRUIT_NUM = configs.getInt("sign_in_recruit_num");

		this.YUGIOH_URL_PREFIX = configs.getJsonObject("yugioh_url_prefix");
		this.YUGIOH_NAME_MAP = configs.getJsonObject("yugioh_name_map");

		this.AI_CHAT_CONFIG = configs.getJsonObject("ai_chat_config");
		this.AI_CHAT_KEYS = configs.getJsonObject("ai_chat_keys");
		this.AI_CHAT_FREE_CONFIG = configs.getJsonObject("ai_chat_free_config");
	}
}

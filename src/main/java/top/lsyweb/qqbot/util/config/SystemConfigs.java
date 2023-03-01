package top.lsyweb.qqbot.util.config;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import top.lsyweb.qqbot.dto.VariablePool;
import top.lsyweb.qqbot.entity.SystemConfig;

import java.util.List;
import java.util.Map;

@Component
public class SystemConfigs
{
	private VariablePool variablePool;
	private Map<String, SystemConfig> configs;

	@Autowired
	public SystemConfigs(VariablePool variablePool) {
		this.variablePool = variablePool;
	}

	/**
	 * 获取字符串类型的值
	 * 若不存在值，则使用默认值
	 * @param key
	 * @return
	 */
	public String getString(String key) {
		if (configs != variablePool.getSystemConfig()) {
			configs = variablePool.getSystemConfig();
		}

		SystemConfig systemConfig = configs.get(key);
		if (systemConfig == null) {
			return null;
		}

		return StringUtils.isBlank(systemConfig.getValue()) ? systemConfig.getDefaultValue() : systemConfig.getValue();
	}

	public int getInt(String key) {
		return Integer.parseInt(getString(key));
	}

	public long getLong(String key) {
		return Long.parseLong(getString(key));
	}

	public double getDouble(String key) {
		return Double.parseDouble(getString(key));
	}

	public <T> JSONArray getJSONArray(String key) {
		String json = getString(key);
		return JSON.parseArray(json);
	}

	public <T> List<T> getList(String key, Class<T> clazz) {
		String json = getString(key);
		JSONArray jsonArray = JSON.parseArray(json);
		return jsonArray.toJavaList(clazz);
	}

	public JSONObject getJsonObject(String key) {
		return JSONObject.parseObject(getString(key));
	}

}

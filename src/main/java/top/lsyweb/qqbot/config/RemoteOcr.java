package top.lsyweb.qqbot.config;

import com.baidu.aip.ocr.AipOcr;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Component;
import top.lsyweb.qqbot.util.ConstantPool;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class RemoteOcr
{
	//设置APPID/AK/SK
	public static final String APP_ID = "25653061";
	public static final String API_KEY = "jMLq1VOUGG7b9ikb5TTRhP3H";
	public static final String SECRET_KEY = "TAupyzgT1iB30A0hReNi3ujsW3zRnswm";

	private AipOcr client;

	public RemoteOcr() {
		// 初始化一个AipOcr
		client = new AipOcr(APP_ID, API_KEY, SECRET_KEY);

		// 可选：设置网络连接参数
		client.setConnectionTimeoutInMillis(2000);
		client.setSocketTimeoutInMillis(60000);
	}



	/**
	 * 远程ocr识别
	 * @param url 远程图片地址
	 * @return 识别结果数组
	 */
	public List<String> ocr(String url) {
		// 传入可选参数调用接口
		HashMap<String, String> options = new HashMap<String, String>();

		log.info("ocr-url:{}", url);
		// 通用文字识别
		JSONObject res = client.general(url, options);
		List<Map<String, String>> wordsResult = null;
		JSONArray jsonArray = null;
		try {
			jsonArray = res.getJSONArray(ConstantPool.WORD_RESULTS);
		} catch (JSONException e) {
			log.error("ocr识别结果转json失败");
			e.printStackTrace();
		}

		if (jsonArray == null || jsonArray.length() == 0) {
			return null;
		}

		List<String> result = new ArrayList<>();
		try {
			for (int i = 0 ; i < jsonArray.length() ; i++) {
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				result.add(jsonObject.getString(ConstantPool.WORD));
			}
		} catch (Exception e) {
			log.error("ocr识别结果结果转List失败");
		}

		return result;
	}

	/**
	 * 远程ocr识别
	 * @param url 远程图片地址
	 * @return 识别结果数组
	 */
	public String unionOcr(String url) {
		List<String> result = this.ocr(url);
		return StringUtils.join(result, "");
	}
}

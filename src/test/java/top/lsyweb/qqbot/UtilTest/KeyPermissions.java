package top.lsyweb.qqbot.UtilTest;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import top.lsyweb.qqbot.entity.KeyInfo;
import top.lsyweb.qqbot.service.KeyService;

import java.util.Arrays;
import java.util.List;

/**
 * 关键词权限配置
 */
@Slf4j
@SpringBootTest
public class KeyPermissions
{
	@Autowired
	KeyService keyService;

	// 指定群聊
	Integer group = 647340514;
	// 禁止或只开启哪些keyId
	List<Integer> keyIdArray = Arrays.asList(9, 9, 17, 17, 260, 260); // 游戏王只查卡+今日人品 open
//	List<Integer> keyIdArray = Arrays.asList(17, 17, 260, 304); // 游戏王+表情组 open
//	List<Integer> keyIdArray = Arrays.asList(5,6,26,27,312,313); // 游戏王+聊天 ban
//	List<Integer> keyIdArray = Arrays.asList(5,6,26,27,36,36,42,42,312,313); // 游戏王+聊天+禁止签到相关 ban
	// 类型 ban 或 open
	String type = "open";

	boolean containsRange(Integer id)  throws Exception {
		if (keyIdArray.size() % 2 != 0) {
			throw new Exception("keyIdArray长度必须为偶数");
		}

		for (int i = 0 ; i < keyIdArray.size() ; i += 2) {
			if (id >= keyIdArray.get(i) && id <= keyIdArray.get(i + 1)) {
				return true;
			}
		}
		return false;
	}

	@Test
	void groupBanKeys() throws Exception {
		List<KeyInfo> allKey = keyService.list();
		if (keyIdArray.size() > 0) {
			for (KeyInfo keyInfo : allKey) {
				boolean containsKey = containsRange(keyInfo.getId());
				if (type.equals("ban") ? containsKey : !containsKey) {
					/**
					 * 这里是要禁止的key才能进入的
					 * 1. 如果不存在groupInclude或groupExclude，新建groupExclude并加入当前群聊
					 * 2. 如果存在groupExclude，在其中加入当前群聊
					 * 3. 如果存在groupInclude，剔除当前群聊
					 */
					JSONObject filter = JSONObject.parseObject(keyInfo.getFilter());

					// 未测试
					JSONArray groupInclude = filter.getJSONArray("groupInclude");
					if (groupInclude != null) {
						if (groupInclude.contains(group)) {
							// 如果这个将被ban的key设置了groupInclude，就只需要在include数组里排除这个群聊即可
							groupInclude.remove(group);
							if (groupInclude.size() == 0) {
								filter.remove("groupInclude");
							} else {
								filter.put("groupInclude", groupInclude);
							}
							keyInfo.setFilter(filter.toJSONString());
							keyService.updateById(keyInfo);
						}
						continue;
					}

					JSONArray groupExcludes = filter.getJSONArray("groupExclude");
					if (groupExcludes == null) {
						groupExcludes = new JSONArray();
					}
					// exclude里如果已经包含了需要ban的群，那就不需要做处理了
					if (!groupExcludes.contains(group)) {
						groupExcludes.add(group);
						filter.put("groupExclude", groupExcludes);
						keyInfo.setFilter(filter.toJSONString());
						keyService.updateById(keyInfo);
						continue;
					}
				} else {
					/**
					 * 这里是要开启的key才能进入的
					 * 1. 如果不存在groupInclude或groupExclude，无需操作
					 * 2. 如果存在groupExclude，从中剔除当前群聊
					 * 3. 如果存在groupInclude，从中加入当前群聊
					 */
					JSONObject filter = JSONObject.parseObject(keyInfo.getFilter());
					JSONArray groupExcludes = filter.getJSONArray("groupExclude");
					if (groupExcludes != null) {
						if (groupExcludes.contains(group)) {
							groupExcludes.remove(group);
							if (groupExcludes.size() == 0) {
								filter.remove("groupExclude");
							} else {
								filter.put("groupExclude", groupExcludes);
							}
							filter.put("groupExclude", groupExcludes);
							keyInfo.setFilter(filter.toJSONString());
							keyService.updateById(keyInfo);
						}
						continue;
					}

					JSONArray groupInclude = filter.getJSONArray("groupInclude");
					if (groupInclude != null && !groupInclude.contains(group)) {
						groupInclude.add(group);
						filter.put("groupInclude", groupInclude);
						keyInfo.setFilter(filter.toJSONString());
						keyService.updateById(keyInfo);
						continue;
					}
				}
			}
		}
	}

}

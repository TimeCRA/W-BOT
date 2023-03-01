package top.lsyweb.qqbot.UtilTest;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import top.lsyweb.qqbot.entity.GroupInfo;
import top.lsyweb.qqbot.entity.KeyInfo;
import top.lsyweb.qqbot.entity.ValueInfo;
import top.lsyweb.qqbot.service.GroupService;
import top.lsyweb.qqbot.service.KeyService;
import top.lsyweb.qqbot.service.ValueService;

import java.util.List;

/**
 * 群聊开闭
 */
@Slf4j
@SpringBootTest
public class GroupSwitch
{
	@Autowired
	GroupService groupService;
	@Autowired
	KeyService keyService;
	@Autowired
	ValueService valueService;

	/**
	 * 关闭所有群聊
	 */
	@Test
	void closeAll() {
		List<GroupInfo> groupList = groupService.list();
		for (GroupInfo groupInfo : groupList) {
			if (groupInfo.getStatus() == 1) {
				groupInfo.setHisStatus(groupInfo.getStatus());
				groupInfo.setStatus(0);
			}
		}
		groupService.saveOrUpdateBatch(groupList);
	}

	/**
	 * 根据历史状态唤醒群聊
	 */
	@Test
	void notifyGroup() {
		List<GroupInfo> groupList = groupService.list();
		for (GroupInfo groupInfo : groupList) {
			if (groupInfo.getHisStatus() == 1) {
				groupInfo.setStatus(groupInfo.getHisStatus());
			}
		}
		groupService.saveOrUpdateBatch(groupList);
	}

	@Test
	void groupChangeId() {
		groupSwitch("815354461", "528993686");
	}

	@Transactional(rollbackFor = Exception.class)
	void groupSwitch(String pre, String now) {
		List<KeyInfo> keyList = keyService.list();

		for (KeyInfo keyInfo : keyList) {
			if (keyInfo.getFilter() == null) {
				continue;
			}
			keyInfo.setFilter(keyInfo.getFilter().replace(pre, now));
		}

		keyService.saveOrUpdateBatch(keyList);
	}
}

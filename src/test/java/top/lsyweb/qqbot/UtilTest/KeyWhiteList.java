package top.lsyweb.qqbot.UtilTest;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import top.lsyweb.qqbot.entity.KeyInfo;
import top.lsyweb.qqbot.service.KeyService;

import java.util.List;

/**
 * key白名单、黑名单配置
 */
@Slf4j
@SpringBootTest
public class KeyWhiteList {
    @Autowired
    KeyService keyService;

    @Test
    void memberAddWhiteList() {
        Integer memberId = 798166259;

        List<KeyInfo> allKey = keyService.list();
        for (KeyInfo keyInfo : allKey) {
            JSONObject filter = JSONObject.parseObject(keyInfo.getFilter());

            JSONArray memberWhiteList = filter.getJSONArray("memberWhiteList");
            if (memberWhiteList == null) {
                memberWhiteList = new JSONArray();
                filter.put("memberWhiteList", memberWhiteList);
            }
            // exclude里如果已经包含了需要ban的群，那就不需要做处理了
            if (!memberWhiteList.contains(memberId)) {
                memberWhiteList.add(memberId);
                keyInfo.setFilter(filter.toJSONString());
                keyService.updateById(keyInfo);
            }
        }
    }
}

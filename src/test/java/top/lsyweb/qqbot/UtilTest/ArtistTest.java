package top.lsyweb.qqbot.UtilTest;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import top.lsyweb.qqbot.entity.ValueInfo;
import top.lsyweb.qqbot.service.ValueService;

import java.io.File;
import java.util.Date;

/**
 * 批量生成图片回复
 */
@Slf4j
@SpringBootTest
public class ArtistTest {
    @Autowired
    private ValueService valueService;

    @Test
    void main() throws Exception {
        insert(867, 935, 26, "C:\\Users\\lhd72\\Pictures\\QQ-BOT\\同步图片", "image/"); // 涩图
        //insert(1, 31, 244, "C:\\Users\\lhd72\\Pictures\\QQ-BOT\\龙图", "image/dragon/"); // 龙图
    }


    void insert(int start, int end, int keyId, String basePath, String prefixPath) throws Exception {
        File file = new File(basePath);
        File[] files = file.listFiles();
        for (File f : files) {
            if (!f.isDirectory()) {
                String debug = f.getName();
                String prefix = f.getName().substring(0, f.getName().lastIndexOf("."));
                int num = Integer.parseInt(prefix);
                if (num <= end && num >= start) {
                    ValueInfo value = new ValueInfo();
                    value.setValue(prefixPath + f.getName());
                    value.setKeyId(keyId);
                    value.setType(1); // 图片
                    value.setCreateTime(new Date());
                    value.setUpdateTime(new Date());
                    valueService.save(value);
                }
            }
        }

    }
}

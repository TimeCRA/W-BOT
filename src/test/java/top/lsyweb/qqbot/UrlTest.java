package top.lsyweb.qqbot;

import com.zhuangxv.bot.core.Bot;
import com.zhuangxv.bot.core.BotFactory;
import com.zhuangxv.bot.core.Group;
import com.zhuangxv.bot.message.support.ImageMessage;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import top.lsyweb.qqbot.util.ConstantPool;
import top.lsyweb.qqbot.util.HttpsDownloadUtils;
import top.lsyweb.qqbot.util.PathUtil;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.util.regex.Matcher;

@Slf4j
@SpringBootTest
public class UrlTest {

    @Test
    void xoo() {
        Bot bot = BotFactory.getBots().get(0);
        ImageMessage message = new ImageMessage("https://pixiv.runrab.workers.dev/img-master/img/2019/08/05/17/56/56/76091581_p0_master1200.jpg");
        Group group = new Group(320955766, "消息发送", bot);
        group.sendMessage(message);
    }





    @Test
    void foo() throws Exception {
        InputStream inputStream = HttpsDownloadUtils.downloadFile("https://pixiv.runrab.workers.dev/img-master/img/2019/08/05/17/56/56/76091581_p0_master1200.jpg");
        BufferedImage read = ImageIO.read(inputStream);
        // 输出到本地
        String filePath = ConstantPool.SETU + PathUtil.getUuidName(".jpg");
        File outputFile = new File(PathUtil.getBasePath() + filePath);
        ImageIO.write(read, "jpg", outputFile);

        // 拼接ocr访问路径
        String ocrPath = PathUtil.getBasePath() + filePath;

        Bot bot = BotFactory.getBots().get(0);
        ImageMessage message = new ImageMessage(ocrPath);
        Group group = new Group(320955766, "消息发送", bot);
        group.sendMessage(message);
    }

    @Test
    void tag() {
        Matcher matcher = PathUtil.getRegex("(来点|来份|来张)?(.*)的?(涩图|色图)", "来张涩图");
        if (matcher.matches()) {
            for (int i = 1 ; i <= matcher.groupCount() ; i++) {
                System.out.println(matcher.group(i));
            }
        }
    }


}

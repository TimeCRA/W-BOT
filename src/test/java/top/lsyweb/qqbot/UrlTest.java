package top.lsyweb.qqbot;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zhuangxv.bot.core.Bot;
import com.zhuangxv.bot.core.BotFactory;
import com.zhuangxv.bot.core.Group;
import com.zhuangxv.bot.message.support.ImageMessage;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import top.lsyweb.qqbot.util.ConstantPool;
import top.lsyweb.qqbot.util.HttpsDownloadUtils;
import top.lsyweb.qqbot.util.PathUtil;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.regex.Matcher;

@Slf4j
@SpringBootTest
public class UrlTest {

    @Autowired
    private RestTemplate restTemplate;

    @Test
    void xoo() {
//        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
//        InetSocketAddress address = new InetSocketAddress("127.0.0.1", 7891);
//        Proxy proxy = new Proxy(Proxy.Type.HTTP, address);
//        factory.setProxy(proxy);
//        RestTemplate restTemplate = new RestTemplate(factory);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth("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");

        // Set request body
        JSONObject objects = new JSONObject();
        objects.put("role", "user");
        objects.put("content", "你好！");
        JSONArray array = new JSONArray();
        array.add(objects);
        JSONObject requestObject = new JSONObject();
        requestObject.put("messages", array);
        requestObject.put("temperature", 0.7);
        requestObject.put("max_tokens", 1024);
        requestObject.put("model", "gpt-3.5-turbo");
        HttpEntity<String> request = new HttpEntity<>(requestObject.toJSONString(), headers);

        JSONObject jsonObject = restTemplate.postForObject("https://api.openai.com/v1/chat/completions", request,
                                                           JSONObject.class);
        System.out.println(jsonObject);
    }



}

package top.lsyweb.qqbot;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import top.lsyweb.qqbot.config.RemoteOcr;
import top.lsyweb.qqbot.entity.MemberInfo;
import top.lsyweb.qqbot.service.MemberService;
import top.lsyweb.qqbot.util.ConstantPool;
import top.lsyweb.qqbot.util.ImageSimilarUtil;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@SpringBootTest
class QqbotApplicationTests
{
	@Autowired
	private RemoteOcr remoteOcr;
	@Autowired
	private MemberService memberService;

	@Test
	void fooooo() {
		MemberInfo findMember;
		// 如果没找到用户对象，则写入一个用户
		findMember = new MemberInfo();
		findMember.setMemberId(111111L);
		findMember.setType(ConstantPool.GROUP_FILTER_NONE);
		findMember.setFavorability(0);
		findMember.setMemberNickname("hello test");
		findMember.setCreateTime(new Date());
		findMember.setUpdateTime(new Date());
		memberService.save(findMember);

		System.out.println(findMember);
	}


	@Test
	void testRedisLink() {
		long code = ImageSimilarUtil.perceptualLocalHashSimilarity("C:\\Users\\lhd72\\Pictures\\QQ-BOT\\Hash\\换头\\8.gif");
		System.out.println(code);
	}

	@Test
	void ocr() {
		String url = "https://gchat.qpic.cn/gchatpic_new/798166259/3810955766-2197405181-C955E08BE0496DC4D3724DB792D3077F/0?term=2";
		List<String> resultList = remoteOcr.ocr(url);
		System.out.println(resultList);
	}

	@Test
	void fooo() throws Exception {
		File file = new File("C:\\Users\\lisiyang\\Pictures\\5.png");
		BufferedImage read = ImageIO.read(new File("C:\\Users\\lisiyang\\Pictures\\1.jpg"));
		ImageIO.write(read, "png", file);
	}

	@Test
	void fo() {
		System.out.println(foo());
	}

	public static boolean foo() {
		Map<String, Object> jsonObject = (Map<String, Object>) JSON.parseObject("{\"memberExclude\": [798166259]}");
		Object memberInclude = jsonObject.get("memberInclude");
		Object memberExclude = jsonObject.get("memberExclude");

		if (memberInclude != null) {
			Set<Long> collect = ((List<Long>) memberInclude).stream().collect(Collectors.toSet());
			if (!collect.contains(798166259L)) {
				return false;
			}
		} else if (memberExclude != null) {
			Set<Long> collect = ((List<Long>) memberExclude).stream().collect(Collectors.toSet());
			if (collect.contains(798166259L)) {
				return false;
			}
		}

		return true;
	}


}

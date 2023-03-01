package top.lsyweb.qqbot.UtilTest;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import top.lsyweb.qqbot.entity.KeyInfo;
import top.lsyweb.qqbot.entity.ValueGroup;
import top.lsyweb.qqbot.entity.ValueInfo;
import top.lsyweb.qqbot.service.KeyService;
import top.lsyweb.qqbot.service.ValueGroupService;
import top.lsyweb.qqbot.service.ValueService;
import top.lsyweb.qqbot.util.ImageSimilarUtil;
import top.lsyweb.qqbot.util.config.SystemConfigPool;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@SpringBootTest
public class ValueGroupTest
{
	@Autowired
	private ValueGroupService valueGroupService;
	@Autowired
	private ValueService valueService;
	@Autowired
	private KeyService keyService;
	@Autowired
	private SystemConfigPool pool;

	@Data
	@Builder
	private static class ValueGroupDto {
		// 回复组描述
		String desc;
		// 本地根路径
		String basePath;
		// 远程跟路径
		String remotePath;
		// 起始名
		int startCount;
		// 终止名
		int imageCOunt;
		// png图像名
		List<Integer> pngLis;
		// gif图像名
		List<Integer> gifList;
		// 回复组文本key
		List<String> textList;
		// 回复组正则key
		List<String> regexList;
	}

	@Test
	void main() throws Exception {
		ValueGroupDto paoMei = ValueGroupDto.builder()
											  .desc("炮妹")
											  .basePath("C:\\Users\\Erekilu\\Pictures\\炮妹\\")
											  .remotePath("image/paomei/")
											  .startCount(1) // need change
											  .imageCOunt(15) // need change
											  .pngLis(Arrays.asList())
											  .gifList(Arrays.asList())
											  .textList(Arrays.asList())
											  .regexList(Arrays.asList())
											  .build();

		ValueGroupDto greyBlue = ValueGroupDto.builder()
				.desc("灰流丽和小蓝")
				.basePath("C:\\Users\\lhd72\\Pictures\\QQ-BOT\\Hash\\灰流丽\\")
				.remotePath("image/grey_blue/")
				.startCount(1) // need change
				.imageCOunt(14) // need change
				.pngLis(Arrays.asList())
				.gifList(Arrays.asList(8,14))
				.textList(Arrays.asList())
				.regexList(Arrays.asList())
				.build();

		ValueGroupDto ff0 = ValueGroupDto.builder()
										 .desc("华法琳沙雕系列")
										 .basePath("C:\\Users\\lhd72\\Pictures\\QQ-BOT\\Hash\\华法琳\\")
										 .remotePath("image/ff0/")
										 .startCount(37) // need change
										 .imageCOunt(37) // need change
										 .pngLis(Arrays.asList())
										 .gifList(Arrays.asList(37))
										 .textList(Arrays.asList())
										 .regexList(Arrays.asList())
										 .build();

		ValueGroupDto ifIHave = ValueGroupDto.builder()
										 .desc("我要是专xx会是这个吊样")
										 .basePath("C:\\Users\\lhd72\\Pictures\\QQ-BOT\\Hash\\xxxx\\")
										 .remotePath("image/if_i_have_xx/")
										 .startCount(-1) // need change
										 .imageCOunt(-1) // need change
										 .pngLis(Arrays.asList())
										 .gifList(Arrays.asList())
										 .textList(Arrays.asList())
										 .regexList(Arrays.asList())
										 .build();

		ValueGroupDto pingpong = ValueGroupDto.builder()
											 .desc("锡兰乒乓球")
											 .basePath("C:\\Users\\lhd72\\Pictures\\QQ-BOT\\Hash\\乒乓球\\")
											 .remotePath("image/pingpong/")
											 .startCount(44) // need change
											 .imageCOunt(50) // need change
											 .pngLis(Arrays.asList())
											 .gifList(Arrays.asList())
											 .textList(Arrays.asList())
											 .regexList(Arrays.asList())
											 .build();

		ValueGroupDto changeHead = ValueGroupDto.builder()
											  .desc("换头")
											  .basePath("C:\\Users\\lhd72\\Pictures\\QQ-BOT\\Hash\\换头\\")
											  .remotePath("image/change_head/")
											  .startCount(48) // need change
											  .imageCOunt(48) // need change
											  .pngLis(Arrays.asList())
											  .gifList(Arrays.asList())
											  .textList(Arrays.asList())
											  .regexList(Arrays.asList())
											  .build();

		ValueGroupDto smelly = ValueGroupDto.builder()
											  .desc("哼哼啊啊啊啊")
											  .basePath("C:\\Users\\lhd72\\Pictures\\QQ-BOT\\Hash\\xxxx\\")
											  .remotePath("image/smelly/")
											  .startCount(-1) // need change
											  .imageCOunt(-1) // need change
											  .pngLis(Arrays.asList())
											  .gifList(Arrays.asList())
											  .textList(Arrays.asList())
											  .regexList(Arrays.asList())
											  .build();

		ValueGroupDto law = ValueGroupDto.builder()
											  .desc("缺德与犯法")
											  .basePath("C:\\Users\\lhd72\\Pictures\\QQ-BOT\\Hash\\缺德与犯法\\")
											  .remotePath("image/law/")
											  .startCount(5) // need change
											  .imageCOunt(5) // need change
											  .pngLis(Arrays.asList())
											  .gifList(Arrays.asList())
											  .textList(Arrays.asList())
											  .regexList(Arrays.asList())
											  .build();

		ValueGroupDto clever = ValueGroupDto.builder()
											  .desc("乖巧")
											  .basePath("C:\\Users\\lhd72\\Pictures\\QQ-BOT\\Hash\\乖巧\\")
											  .remotePath("image/clever/")
											  .startCount(5) // need change
											  .imageCOunt(6) // need change
											  .pngLis(Arrays.asList())
											  .gifList(Arrays.asList())
											  .textList(Arrays.asList())
											  .regexList(Arrays.asList())
											  .build();

		ValueGroupDto dog = ValueGroupDto.builder()
											.desc("刻俄柏")
											.basePath("C:\\Users\\lhd72\\Pictures\\QQ-BOT\\Hash\\刻俄柏\\")
											.remotePath("image/dog/")
											.startCount(1) // need change
											.imageCOunt(11) // need change
											.pngLis(Arrays.asList())
											.gifList(Arrays.asList())
											.textList(Arrays.asList())
											.regexList(Arrays.asList())
											.build();

		saveOrUpdate(paoMei);
	}

	void saveOrUpdate(ValueGroupDto valueGroupDto) {
		// 回复组描述
		String desc = valueGroupDto.getDesc();
		// 回复组文本key
		List<String> textList = valueGroupDto.getTextList();
		// 回复组正则key
		List<String> regexList = valueGroupDto.getRegexList();
		// 回复组图片地址
		List<String> localPathList = new ArrayList<>();
		String basePath = valueGroupDto.getBasePath();
		List<Integer> pngList = valueGroupDto.getPngLis();
		List<Integer> gifList = valueGroupDto.getGifList();

		int startCount = valueGroupDto.getStartCount(), imageCount = valueGroupDto.getImageCOunt();
		for (int i = startCount ; i <= imageCount ; i++) {
			String tmpPath = basePath + i;
			if (pngList.contains(i)) {
				tmpPath += ".png";
			} else if (gifList.contains(i)) {
				tmpPath += ".gif";
			} else {
				tmpPath += ".jpg";
			}
			localPathList.add(tmpPath);
		}

		// 回复组远程图片地址
		int count = 0;
		List<String> remotePathList = new ArrayList<>();
		String remotePath = valueGroupDto.getRemotePath();
		for (int i = startCount ; i <= imageCount ; i++) {
			String tmpPath = remotePath + i;
			if (pngList.contains(i)) {
				tmpPath += ".png";
			} else if (gifList.contains(i)) {
				tmpPath += ".gif";
			} else {
				tmpPath += ".jpg";
			}
			remotePathList.add(tmpPath);
		}

		/**
		 * 1. 创建回复组信息
		 * 2. 处理文本key
		 * 3. 处理正则key
		 */
		QueryWrapper<ValueGroup> wrapper = new QueryWrapper<>();
		wrapper.eq("`desc`", desc);
		ValueGroup valueGroup = valueGroupService.getOne(wrapper);
		if (valueGroup == null) {
			valueGroup = new ValueGroup(desc);
			valueGroupService.save(valueGroup);
		}

		// 获取回复组id
		int valueGroupId = valueGroup.getId();

		// 定义防重Set
		Set<Long> codeSet = new HashSet<>();
		QueryWrapper<KeyInfo> keyWrapper = new QueryWrapper<>();
		keyWrapper.eq("`value_group_id`", valueGroup.getId()).eq("`type`", 3);
		List<KeyInfo> imageKeyTmp = keyService.list(keyWrapper);
		codeSet.addAll(imageKeyTmp.stream().map(o -> Long.parseLong(o.getKey())).collect(Collectors.toList()));

		// 添加文本key
		for (String s : textList) {
			// ValueInfo添加信息
			ValueInfo valueInfo = new ValueInfo();
			valueInfo.setType(0);
			valueInfo.setValue(s);
			valueInfo.setDesc(desc);
			valueInfo.setKeyId(-1);
			valueInfo.setValueGroupId(valueGroupId);

			valueInfo.setCreateTime(new Date());
			valueInfo.setUpdateTime(new Date());
			valueService.save(valueInfo);

			// keyInfo添加信息
			KeyInfo keyInfo = new KeyInfo();
			keyInfo.setType(1);
			keyInfo.setKey(s);
			keyInfo.setOcrContent(desc);
			keyInfo.setValueGroupId(valueGroupId);

			keyInfo.setCreateTime(new Date());
			keyInfo.setUpdateTime(new Date());
			keyService.save(keyInfo);
		}

		// 添加正则类型
		for (String s : regexList) {
			// keyInfo添加信息
			KeyInfo keyInfo = new KeyInfo();
			keyInfo.setType(2);
			keyInfo.setKey(s);
			keyInfo.setOcrContent(desc);
			keyInfo.setValueGroupId(valueGroupId);

			keyInfo.setCreateTime(new Date());
			keyInfo.setUpdateTime(new Date());
			keyService.save(keyInfo);
		}

		// 添加图片类型
		for (String path : localPathList) {
			// ValueInfo添加信息
			ValueInfo valueInfo = new ValueInfo();
			valueInfo.setType(1);
			valueInfo.setValue(remotePathList.get(count++));
			valueInfo.setDesc(desc);
			valueInfo.setKeyId(-1);
			valueInfo.setValueGroupId(valueGroupId);

			valueInfo.setCreateTime(new Date());
			valueInfo.setUpdateTime(new Date());
			valueService.save(valueInfo);

			// 获取图片hash码
			long code = ImageSimilarUtil.perceptualLocalHashSimilarity(path);

			// keyInfo添加信息
			if (!codeSet.contains(code)) {
				KeyInfo keyInfo = new KeyInfo();
				keyInfo.setType(3);
				keyInfo.setOcrContent(desc);
				keyInfo.setKey(String.valueOf(code));
				keyInfo.setValueGroupId(valueGroupId);
				// 从配置中取默认相似度
				keyInfo.setPrecision(pool.DEFAULT_IMAGE_PRECISION);

				keyInfo.setCreateTime(new Date());
				keyInfo.setUpdateTime(new Date());
				keyService.save(keyInfo);
				codeSet.add(code);
			}
		}
	}
}

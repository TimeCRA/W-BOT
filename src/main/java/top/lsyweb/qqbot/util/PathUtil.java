package top.lsyweb.qqbot.util;

import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.util.Calendar;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 应该就叫Util的，本来想着给各个功能分类，但功能实在不多，所以就写到一个工具类里了
 * 包括了获取基路径，生成uuid，图片输出到本地，页号与行号的转化功能
 * @Auther: Erekilu
 * @Date: 2020-03-08
 */
@Component
public class PathUtil
{
	public static final Random random = new Random();
	@Value("${oss-path}")
	private String path;
	private static String ossPath;

	@PostConstruct
	public void getStaticPath() {
		ossPath = this.path;
	}


	/**
	 * 获取本地文件夹基路径
	 * @return
	 */
	public static String getBasePath()
	{
		String os = System.getProperty("os.name");
		String basePath;

		// 判断系统
		if (os.toLowerCase().startsWith("win"))
		{
			basePath = "E:/qqbot_resource/";
		}
		else
		{
			basePath = "/qqbot_resource/";
		}
		// return basePath;
		return "/qqbot_resource/";
	}

	/**
	 * 获取文件服务器路径
	 */
	public static String getOssPath() {
		return ossPath;
	}

	/**
	 * 根据系统返回字符串
	 * @return
	 */
	public static boolean isWin() {
		return System.getProperty("os.name").toLowerCase().startsWith("win");
	}

	/**
	 * 用一个uuid做文件名，生成完整的文件名。如：134435sd1f3adfadf5463.jpg
	 * @param sname 文件后缀名
	 * @return
	 */
	public static String getUuidName(String sname)
	{
		// 给图片命名
		String uuid = UUID.randomUUID().toString().replace("-", "");
		// 拼接成完整的文件名
		String fullName = uuid + sname;

		return fullName;
	}

	/**
	 * 将图片流输出到本地
	 * @param source 图片流
	 * @param target 本地路径
	 */
	public static void createThumbnail(InputStream source, String target)
	{
		try
		{
			// 获取图片宽，高
			BufferedImage sourceImage = ImageIO.read(source);
			// 等比缩放图片，最大高/宽为100
			Thumbnails.of(sourceImage)
					  .size(100, 100)
					  .toFile(new File(target));
		}
		catch (Exception e)
		{
			throw new RuntimeException("本地创建图片失败：" + e.toString());
		}
	}

	/**
	 * 从一个list里随机取一个元素出来
	 * @param list
	 * @return
	 */
	public static <T> T randomList(List<T> list) {
		if (CollectionUtils.isEmpty(list)) {
			return null;
		}

		return list.get(random.nextInt(list.size()));
	}

	/**
	 * 解析正则，拿到值
	 * @param key
	 * @param content
	 * @return
	 */
	public static Matcher getRegex(String key, String content) {
		Pattern pattern = Pattern.compile(key);
		Matcher matcher = pattern.matcher(content);
		matcher.find();
		return matcher;
	}

	/**
	 * 获取当前时间到0点的秒数
	 * @return
	 */
	public static long getReleaseTime() {
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DAY_OF_YEAR,1);
		calendar.set(Calendar.HOUR_OF_DAY,0);
		calendar.set(Calendar.SECOND,0);
		calendar.set(Calendar.MINUTE,0);
		calendar.set(Calendar.MILLISECOND,0);
		return (calendar.getTimeInMillis()-System.currentTimeMillis()) / 1000;
	}

	/**
	 * 更新好感度
	 * @param original 原始好感度
	 * @param change 变动值
	 * @return
	 */
	public static int updateFavorability(int original, int change) {
		if (change > 0) {
			original = original + change > ConstantPool.MAX_FAVORABILITY ? ConstantPool.MAX_FAVORABILITY : original + change;
		} else if (change < 0) {
			original = original + change < ConstantPool.MIN_FAVORABILITY ? ConstantPool.MIN_FAVORABILITY : original - change;
		}
		return original;
	}

	/**
	 * 获取当前系统换行符
	 * @return
	 */
	public static String getLine() {
		return System.getProperty("line.separator");
	}
}

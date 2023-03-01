package top.lsyweb.qqbot.util;

import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;

@Slf4j
public class ImageSimilarUtil
{
	/**
	 * 图像默认相似度
	 */
	public static final int DEFAULT_LIMIT = 3;


	/**
	 * 图片相似判断入口
	 *
	 * @return
	 */
	public static boolean isSimilar(long code1, long code2) {
		return isSimilar(code1, code2, DEFAULT_LIMIT);
	}

	/**
	 * 定义精度 图片相似判断入口
	 * @param code1
	 * @param code2
	 * @param limit
	 * @return
	 */
	public static boolean isSimilar(long code1, long code2, int limit) {
		// 将两个图片码异或，找到不同位个数
		int diffCount = hammingWeight(code1 ^ code2);
		return diffCount <= limit;
	}

	/**
	 * 判断这个数二进制有多少位1
	 * @param n
	 * @return
	 */
	public static int hammingWeight(long n) {
		int count = 0;
		while (n != 0)
		{
			count++;
			n = (n - 1) & n;
		}
		return count;
	}

	public static boolean perceptualHashSimilarity(String code1, String code2, int limit) {
		char[] ch1 = code1.toCharArray();
		char[] ch2 = code2.toCharArray();
		int diffCount = 0;
		for (int i = 0 ; i < 64 ; i++) {
			if (ch1[i] != ch2[i]) {
				diffCount++;
			}
		}
		return diffCount <= limit;
	}

	/**
	 * 根据图像url，获取哈希码
	 * @param url
	 * @return
	 */
	public static long perceptualHashSimilarity(String url) {
		try {
			return perceptualHashSimilarity(ImageIO.read(HttpsDownloadUtils.downloadFile(url)));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0L;
	}

	/**
	 * 根据本地path，获取哈希码
	 * @return
	 */
	public static long perceptualLocalHashSimilarity(String path) {
		try {
			return perceptualHashSimilarity(ImageIO.read(new File(path)));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0L;
	}

	public static long perceptualHashSimilarity(BufferedImage src) {
		int width = 8;
		int height = 8;
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_BGR);
		Graphics graphics = image.createGraphics();
		graphics.drawImage(src, 0, 0, 8, 8, null);
		int total = 0;
		for (int i = 0 ; i < height ; i++) {
			for (int j = 0 ; j < width ; j++) {
				int pixel = image.getRGB(j, i);
				int gray = gray(pixel);
				total = total + gray;
			}
		}
		long result = 0L;
		long index = 4611686018427387904L; //  1 << 62, long类型的第63位为1
		int grayAvg = total / (width * height);
		for (int i = 0 ; i < height ; i++) {
			for (int j = 0 ; j < width ; j++) {
				if (i == 0 && j == 0) {
					// 无符号，排除首位
					continue;
				}
				int pixel = image.getRGB(j, i);
				int gray = gray(pixel);
				if (gray >= grayAvg) {
					result += index;
				}

				index = index >> 1;
			}
		}
		return result;
	}

	private static int gray(int rgb) {
		//将最高位（24-31）的信息（alpha通道）存储到a变量
		int a = rgb & 0xff000000;
		//取出次高位（16-23）红色分量的信息
		int r = (rgb >> 16) & 0xff;
		//取出中位（8-15）绿色分量的信息
		int g = (rgb >> 8) & 0xff;
		//取出低位（0-7）蓝色分量的信息
		int b = rgb & 0xff;
		// NTSC luma，算出灰度值
		rgb = (r * 77 + g * 151 + b * 28) >> 8;
		//将灰度值送入各个颜色分量
		return a | (rgb << 16) | (rgb << 8) | rgb;
	}
}

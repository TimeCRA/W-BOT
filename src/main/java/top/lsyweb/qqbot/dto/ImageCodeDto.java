package top.lsyweb.qqbot.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImageCodeDto
{
	// 图片链接
	private String url;

	// 图片code
	private Long code;

	// 数量
	private Integer count;
}

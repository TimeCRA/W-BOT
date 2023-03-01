package top.lsyweb.qqbot.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.multipart.MultipartFile;
import top.lsyweb.qqbot.entity.ValueInfo;

public interface ValueService extends IService<ValueInfo>
{
	/**
	 * 返回文件路径
	 * @param file
	 * @return
	 */
	String mediaUpload(MultipartFile file);
}

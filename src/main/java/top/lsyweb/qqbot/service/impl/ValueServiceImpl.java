package top.lsyweb.qqbot.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import top.lsyweb.qqbot.entity.ValueInfo;
import top.lsyweb.qqbot.exception.ServiceException;
import top.lsyweb.qqbot.mapper.ValueMapper;
import top.lsyweb.qqbot.service.ValueService;
import top.lsyweb.qqbot.util.PathUtil;

import java.io.File;
import java.io.IOException;

@Service
public class ValueServiceImpl extends ServiceImpl<ValueMapper, ValueInfo> implements ValueService
{
	@Override
	public String mediaUpload(MultipartFile file) {

		// 获取上传文件名
		String fileName = file.getOriginalFilename();
		// 获取后缀名
		String sname = fileName.substring(fileName.lastIndexOf(".")).toLowerCase();
		// 获取完整的文件名
		String fullName = PathUtil.getUuidName(sname);
		// 相对路径
		boolean isImage = sname.equals("jpg") || sname.equals("jpeg") ||sname.equals("png") || sname.equals("gif");
		String resultPath = (isImage ? "image/" : "audio/") + fullName;
		// 输出路径
		String outputPath = PathUtil.getBasePath() + resultPath;

		try {
			// 写到本地
			file.transferTo(new File(outputPath));
		} catch (IOException e) {
			throw new ServiceException("文件保存失败");
		}

		return resultPath;
	}
}

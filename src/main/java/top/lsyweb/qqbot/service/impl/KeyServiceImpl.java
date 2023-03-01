package top.lsyweb.qqbot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import top.lsyweb.qqbot.entity.KeyInfo;
import top.lsyweb.qqbot.mapper.KeyMapper;
import top.lsyweb.qqbot.service.KeyService;

@Service
public class KeyServiceImpl extends ServiceImpl<KeyMapper, KeyInfo> implements KeyService
{
	@Override
	public Page<KeyInfo> queryList(int page, int size, Integer status, String query) {
		Page<KeyInfo> keyInfo = new Page<>(page, size);
		QueryWrapper<KeyInfo> queryWrapper = new QueryWrapper<>();
		if (StringUtils.isNotBlank(query)) {
			queryWrapper.like("`key`", query);
		}
		if (status != null) {
			queryWrapper.eq("status", status);
		}
		return this.page(keyInfo, queryWrapper);
	}
}

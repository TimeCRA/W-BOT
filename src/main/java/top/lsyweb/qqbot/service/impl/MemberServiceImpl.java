package top.lsyweb.qqbot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import top.lsyweb.qqbot.entity.MemberInfo;
import top.lsyweb.qqbot.mapper.MemberMapper;
import top.lsyweb.qqbot.service.MemberService;

@Service
public class MemberServiceImpl extends ServiceImpl<MemberMapper, MemberInfo> implements MemberService
{
	@Override
	public Page<MemberInfo> queryList(int page, int size, Integer status, String query) {
		Page<MemberInfo> pageInfo = new Page<>(page, size);
		QueryWrapper<MemberInfo> queryWrapper = new QueryWrapper<>();
		if (StringUtils.isNotBlank(query)) {
			queryWrapper.like("member_nickname", query).or()
						.eq("member_id", query);
		}
		if (status != null) {
			queryWrapper.eq("status", status);
		}
		return this.page(pageInfo, queryWrapper);
	}
}

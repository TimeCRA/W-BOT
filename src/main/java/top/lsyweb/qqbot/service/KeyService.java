package top.lsyweb.qqbot.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import top.lsyweb.qqbot.entity.KeyInfo;
import top.lsyweb.qqbot.entity.MemberInfo;

public interface KeyService extends IService<KeyInfo>
{
	Page<KeyInfo> queryList(int page, int size, Integer status, String query);
}

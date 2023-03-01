package top.lsyweb.qqbot.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import top.lsyweb.qqbot.entity.GroupInfo;
import top.lsyweb.qqbot.mapper.GroupMapper;
import top.lsyweb.qqbot.service.GroupService;

@Service
public class GroupServiceImpl extends ServiceImpl<GroupMapper, GroupInfo> implements GroupService
{
}

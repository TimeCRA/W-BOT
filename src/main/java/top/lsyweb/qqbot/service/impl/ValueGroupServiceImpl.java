package top.lsyweb.qqbot.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import top.lsyweb.qqbot.entity.ValueGroup;
import top.lsyweb.qqbot.mapper.ValueGroupMapper;
import top.lsyweb.qqbot.service.ValueGroupService;

@Service
public class ValueGroupServiceImpl extends ServiceImpl<ValueGroupMapper, ValueGroup> implements ValueGroupService
{
}

package top.lsyweb.qqbot.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import top.lsyweb.qqbot.mapper.SystemConfigMapper;
import top.lsyweb.qqbot.service.SystemConfigService;
import top.lsyweb.qqbot.entity.SystemConfig;

@Service
public class SystemConfigServiceImpl extends ServiceImpl<SystemConfigMapper, SystemConfig> implements SystemConfigService
{
}

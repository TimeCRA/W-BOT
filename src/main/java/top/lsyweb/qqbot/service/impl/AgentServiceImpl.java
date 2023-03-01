package top.lsyweb.qqbot.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.lsyweb.qqbot.entity.AgentInfo;
import top.lsyweb.qqbot.entity.GroupInfo;
import top.lsyweb.qqbot.mapper.AgentMapper;
import top.lsyweb.qqbot.mapper.GroupMapper;
import top.lsyweb.qqbot.service.AgentService;
import top.lsyweb.qqbot.service.GroupService;

/**
 * @Auther: Erekilu
 * @Date: 2022-01-26
 */
@Service
public class AgentServiceImpl extends ServiceImpl<AgentMapper, AgentInfo> implements AgentService
{
}

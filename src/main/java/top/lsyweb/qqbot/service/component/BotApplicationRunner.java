package top.lsyweb.qqbot.service.component;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import top.lsyweb.qqbot.service.client.VariablePoolService;
import top.lsyweb.qqbot.util.config.SystemConfigPool;

/**
 * 服务器启动时调用
 */
@Slf4j
@Component
public class BotApplicationRunner implements ApplicationRunner
{
	@Autowired
	private VariablePoolService variablePoolService;
	@Autowired
	private SystemConfigPool pool;

	@Override
	public void run(ApplicationArguments args) {
		log.info("项目启动，执行刷新操作");
		// 刷新系统变量
		variablePoolService.refreshSystemConfig();
		// 刷新配置池
		pool.refresh();
		// 刷新变量池
		variablePoolService.refreshAll();
	}
}

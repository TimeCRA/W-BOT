package top.lsyweb.qqbot.controller.oms;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.lsyweb.qqbot.dto.ResultResponse;
import top.lsyweb.qqbot.service.client.VariablePoolService;
import top.lsyweb.qqbot.util.config.SystemConfigPool;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

@Slf4j
@RequestMapping("/oms/system")
@RestController
public class HealthController
{
	@Autowired
	private VariablePoolService variablePoolService;
	@Autowired
	private SystemConfigPool pool;

	@PostMapping("/login")
	public ResultResponse login(@RequestBody Map<String, String> map) {
		String username = map.get("username");
		String password = map.get("password");

		String tempPassword = new SimpleDateFormat("HHmm").format(new Date());

		log.info("当前时分: {}", tempPassword);
		if ("admin".equals(username) && tempPassword.equals(password)) {
			return ResultResponse.success();
		}
		return ResultResponse.error("用户名或密码错误");
	}

	@PostMapping("/refresh")
	public ResultResponse refresh() {
		log.info("开始刷新内存数据");
		variablePoolService.refreshSystemConfig();
		pool.refresh();
		variablePoolService.refreshAll();
		return ResultResponse.success();
	}
}

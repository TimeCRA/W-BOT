package top.lsyweb.qqbot.controller.oms;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import top.lsyweb.qqbot.dto.ResultResponse;
import top.lsyweb.qqbot.entity.KeyInfo;
import top.lsyweb.qqbot.service.KeyService;
import top.lsyweb.qqbot.service.client.VariablePoolService;

import java.util.Date;

@RequestMapping("/oms/key")
@RestController
public class KeyController
{
	@Autowired
	private KeyService keyService;
	@Autowired
	private VariablePoolService variablePoolService;

	@GetMapping("/getList")
	public ResultResponse getKey(int page, int size, Integer status, String query) {
		return ResultResponse.success(keyService.queryList(page, size, status, query));
	}

	@PostMapping("/modify")
	public ResultResponse modifyKey(@RequestBody KeyInfo key) {
		key.setCreateTime(key.getCreateTime() != null ? key.getCreateTime() : new Date());
		key.setUpdateTime(new Date());
		keyService.saveOrUpdate(key);
		variablePoolService.refreshKeyValue();
		return ResultResponse.success();
	}

	@DeleteMapping("/remove")
	public ResultResponse removeKey(@RequestBody KeyInfo key) {
		keyService.removeById(key.getId());
		variablePoolService.refreshKeyValue();
		return ResultResponse.success();
	}
}

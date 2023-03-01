package top.lsyweb.qqbot.controller.oms;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import top.lsyweb.qqbot.dto.ResultResponse;
import top.lsyweb.qqbot.entity.GroupInfo;
import top.lsyweb.qqbot.service.GroupService;
import top.lsyweb.qqbot.service.client.VariablePoolService;

import java.util.Date;

@RequestMapping("/oms/group")
@RestController
public class GroupController
{
	@Autowired
	private GroupService groupService;
	@Autowired
	private VariablePoolService variablePoolService;

	@GetMapping("/getList")
	public ResultResponse getGroup(int page, int size) {
		Page<GroupInfo> pageInfo = new Page<>(page, size);
		return ResultResponse.success(groupService.page(pageInfo));
	}

	@PostMapping("/modify")
	public ResultResponse modifyGroup(@RequestBody GroupInfo group) {
		group.setCreateTime(group.getCreateTime() != null ? group.getCreateTime() : new Date());
		group.setUpdateTime(new Date());
		groupService.saveOrUpdate(group);
		variablePoolService.refreshFailureGroupSet();
		return ResultResponse.success();
	}

	@DeleteMapping("/remove")
	public ResultResponse removeGroup(@RequestBody GroupInfo group) {
		groupService.removeById(group.getId());
		variablePoolService.refreshFailureGroupSet();
		return ResultResponse.success();
	}
}

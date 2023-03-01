package top.lsyweb.qqbot.controller.oms;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.segments.MergeSegments;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import top.lsyweb.qqbot.dto.ResultResponse;
import top.lsyweb.qqbot.entity.GroupInfo;
import top.lsyweb.qqbot.entity.MemberInfo;
import top.lsyweb.qqbot.service.MemberService;
import top.lsyweb.qqbot.service.client.VariablePoolService;

import java.util.Date;

@RequestMapping("/oms/member")
@RestController
public class MemberController
{
	@Autowired
	private MemberService memberService;
	@Autowired
	private VariablePoolService variablePoolService;

	@GetMapping("/getList")
	public ResultResponse getMember(int page, int size, Integer status, String query) {
		return ResultResponse.success(memberService.queryList(page, size, status, query));
	}

	@PostMapping("/modify")
	public ResultResponse modifyMember(@RequestBody MemberInfo member) {
		member.setCreateTime(member.getCreateTime() != null ? member.getCreateTime() : new Date());
		member.setUpdateTime(new Date());
		memberService.saveOrUpdate(member);
		variablePoolService.refreshFailureMemberSet();
		variablePoolService.refreshMemberInfo();
		return ResultResponse.success();
	}

	@DeleteMapping("/remove")
	public ResultResponse removeMember(@RequestBody MemberInfo member) {
		memberService.removeById(member.getId());
		variablePoolService.refreshFailureMemberSet();
		variablePoolService.refreshMemberInfo();
		return ResultResponse.success();
	}
}

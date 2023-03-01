package top.lsyweb.qqbot.controller.oms;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import top.lsyweb.qqbot.dto.ResultResponse;
import top.lsyweb.qqbot.dto.ValueDto;
import top.lsyweb.qqbot.entity.ValueInfo;
import top.lsyweb.qqbot.service.ValueService;
import top.lsyweb.qqbot.service.client.VariablePoolService;

import java.util.Date;
import java.util.List;

@RequestMapping("/oms/value")
@RestController
public class ValueController
{
	@Autowired
	private ValueService valueService;
	@Autowired
	private VariablePoolService variablePoolService;

	@GetMapping("/getList")
	public ResultResponse getValue(int page, int size, int keyId) {
		Page<ValueInfo> valueInfo = new Page<>(page, size);
		QueryWrapper<ValueInfo> valueWrapper = new QueryWrapper<>();
		valueWrapper.eq("key_id", keyId);
		valueService.page(valueInfo, valueWrapper);
		List<ValueDto> valueDtoList = ValueDto.parseValueDtoList(valueInfo.getRecords());
		return ResultResponse.success(valueInfo, valueDtoList);
	}

	@GetMapping("/getOne")
	public ResultResponse getOne(int id)  {
		return ResultResponse.success(valueService.getById(id));
	}

	@PostMapping("/modify")
	public ResultResponse modifyValue(@RequestBody ValueDto valueDto) {
		ValueInfo valueInfo = new ValueInfo(valueDto);
		valueInfo.setCreateTime(valueInfo.getCreateTime() != null ? valueInfo.getCreateTime() : new Date());
		valueInfo.setUpdateTime(new Date());
		valueService.saveOrUpdate(valueInfo);
		variablePoolService.refreshKeyValue();
		return ResultResponse.success();
	}

	@DeleteMapping("/remove")
	public ResultResponse removeValue(@RequestBody ValueInfo valueInfo) {
		valueService.removeById(valueInfo.getId());
		variablePoolService.refreshKeyValue();
		return ResultResponse.success();
	}

	// @PostMapping("/mediaUpload")
	public ResultResponse mediaUpload(MultipartFile file) {
		return ResultResponse.success(valueService.mediaUpload(file));
	}
}

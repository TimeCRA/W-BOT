package top.lsyweb.qqbot.dto;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;

import java.util.List;

@Data
public class PageDto
{
	/**
	 * 总数
	 */
	private Long totalRows;

	/**
	 * 每页数量
	 */
	private Long rows;

	/**
	 * 当前页号
	 */
	private Long page;

	/**
	 * 总页数
	 */
	private Long totalPage;

	/**
	 * 所有记录
	 */
	private List records;

	PageDto(Page page) {
		this.totalRows = page.getTotal();
		this.rows = page.getSize();
		this.page = page.getCurrent();
		this.totalPage = getTotalPage(totalRows, rows);
		this.records = page.getRecords();
	}

	PageDto(Page page, Object records) {
		this.totalRows = page.getTotal();
		this.rows = page.getSize();
		this.page = page.getCurrent();
		this.totalPage = getTotalPage(totalRows, rows);
		this.records = (List) records;
	}

	private Long getTotalPage(Long totalRows, Long rows) {
		return totalRows / rows + (totalRows % rows == 0 ? 0: 1);
	}
}

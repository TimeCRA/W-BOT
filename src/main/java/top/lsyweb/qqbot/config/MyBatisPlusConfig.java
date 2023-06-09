package top.lsyweb.qqbot.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.incrementer.IKeyGenerator;
import com.baomidou.mybatisplus.extension.incrementer.H2KeyGenerator;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.PaginationInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MyBatisPlusConfig
{
	/**
	 *  插件集合
	 */
	@Bean
	public MybatisPlusInterceptor mybatisPlusInterceptor() {
		//  插件
		MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
		// 分页插件
		PaginationInnerInterceptor paginationInnerInterceptor = new PaginationInnerInterceptor(DbType.MYSQL);
		// 添加分页插件
		interceptor.addInnerInterceptor(paginationInnerInterceptor);
		return interceptor;
	}
}

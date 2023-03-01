package top.lsyweb.qqbot.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 异步使用的线程池
 */
@EnableAsync
@Configuration
public class ExecutorPoolConfig {
	@Bean("executor")
	public Executor taskExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(20);
		executor.setMaxPoolSize(50);
		executor.setQueueCapacity(5000);
		executor.setKeepAliveSeconds(120);
		executor.setThreadNamePrefix("task-");
		/**
		 * AbortPolicy（默认）：丢弃任务，抛出RejectedExecutionException异常
		 * DiscardPolicy：也是丢弃任务，但不抛出异常
		 * DiscardOldestPolicy：对被拒绝的任务（新来的）不抛弃，而是抛弃队列里面等待最久的一个任务，然后把该拒绝任务加到队列
		 * CallerRunsPolicy：重试添加当前被拒绝的任务，他会自动重复调用 execute() 方法，直到成功。
		 */
		executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
		return executor;
	}
}

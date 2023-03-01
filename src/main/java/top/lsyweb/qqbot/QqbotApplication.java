package top.lsyweb.qqbot;

import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;
import com.zhuangxv.bot.EnableBot;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableBot
@MapperScan("top.lsyweb.qqbot.mapper")
@EnableScheduling
@SpringBootApplication
public class QqbotApplication
{

	public static void main(String[] args) {
		SpringApplication.run(QqbotApplication.class, args);
	}

}

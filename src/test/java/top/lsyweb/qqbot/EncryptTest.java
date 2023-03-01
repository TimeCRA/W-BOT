package top.lsyweb.qqbot;

import org.jasypt.encryption.StringEncryptor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @Auther: Erekilu
 * @Date: 2023-03-01
 */
@SpringBootTest
public class EncryptTest
{
	@Autowired
	StringEncryptor encryptor;

	@Test
	void encrypt() {
		System.out.println(encryptor.encrypt(""));
	}
}

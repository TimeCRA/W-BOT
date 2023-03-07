package top.lsyweb.qqbot.util;

import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.net.InetSocketAddress;
import java.net.Proxy;

/**
 * @Auther: Erekilu
 * @Date: 2023-03-02
 */
public class ProxyRestTemplate
{
	private static RestTemplate restTemplate;

	public static RestTemplate getInstance() {
		if (restTemplate == null) {
			SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
			InetSocketAddress address = new InetSocketAddress("127.0.0.1", 7890);
			Proxy proxy = new Proxy(Proxy.Type.HTTP, address);
			factory.setProxy(proxy);
			factory.setConnectTimeout(10000);
			factory.setReadTimeout(30000);
			return new RestTemplate(factory);
		}

		return restTemplate;
	}
}

package top.lsyweb.qqbot.util;

import lombok.extern.slf4j.Slf4j;

import javax.net.ssl.*;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

@Slf4j
public class HttpsDownloadUtils
{
	/**
	 * 使用代理下载
	 * @param fileUrl   https 远程路径
	 * @throws Exception
	 */
	public static InputStream downloadFileProxy(String fileUrl) throws Exception {
		return innerDownloadFile(fileUrl, true);
	}

	/**
	 * 默认下载
	 * @param fileUrl   https 远程路径
	 * @throws Exception
	 */
	public static InputStream downloadFile(String fileUrl) throws Exception {
		return innerDownloadFile(fileUrl, false);
	}

	/**
	 * @param fileUrl   https 远程路径
	 * @throws Exception
	 */
	public static InputStream innerDownloadFile(String fileUrl, boolean isProxy) throws Exception {
		SSLContext sslcontext = SSLContext.getInstance("SSL", "SunJSSE");
		sslcontext.init(null, new TrustManager[]{new X509TrustUtiil()}, new java.security.SecureRandom());
		URL url = new URL(fileUrl);
		HostnameVerifier ignoreHostnameVerifier = new HostnameVerifier() {
			@Override
			public boolean verify(String s, SSLSession sslsession) {
				log.warn("Hostname is not matched for cert.");
				return true;
			}
		};
		HttpsURLConnection.setDefaultHostnameVerifier(ignoreHostnameVerifier);
		HttpsURLConnection.setDefaultSSLSocketFactory(sslcontext.getSocketFactory());
		// HttpsURLConnection urlCon = isProxy ? ((HttpsURLConnection) url.openConnection(getProxy())) : ((HttpsURLConnection) url.openConnection());
		HttpsURLConnection urlCon = (HttpsURLConnection) url.openConnection();
//		urlCon.setRequestProperty("User-Agent", "Cloudflare Workers");
//		urlCon.setRequestProperty("Referer", "https://www.pixiv.net/");
		if (isProxy) {
			if (PathUtil.isWin()) {
				urlCon.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:99.0) Gecko/20100101 Firefox/99.0");
			} else {
				urlCon.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/73.0.3683.103 Safari/537.36 OPR/60.0.3255.83");
			}
			urlCon.setRequestProperty("referer", "https://www.pixiv.net");
		}
		// urlCon.setConnectTimeout(6000);
		// urlCon.setReadTimeout(6000);
		int code = urlCon.getResponseCode();
		if (code != HttpURLConnection.HTTP_OK) {
			throw new Exception("文件读取失败");
		}

		return urlCon.getInputStream();

	}

	/**
	 * X509Trust
	 */
	static class X509TrustUtiil implements X509TrustManager
	{

		@Override
		public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

		}

		@Override
		public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

		}

		@Override
		public X509Certificate[] getAcceptedIssuers() {
			return null;
		}

	}

	public static void main(String[] args) throws Exception {
	}

	public static Proxy getProxy() {
		InetSocketAddress address = new InetSocketAddress("127.0.0.1", 58591);
		Proxy proxy = new Proxy(Proxy.Type.HTTP, address);
		return proxy;
	}
}

package com.sange.telegram.bot.util.http;

import com.sange.telegram.bot.util.properties.ConfigUtils;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Credentials;
import okhttp3.OkHttpClient;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;

/**
 * HttpClient工厂
 *
 * 可以配置代理
 */
@Slf4j
public class HttpClientFactory {

    public static HttpClient create() {
        return create(null, null, null, null);
    }

    public static HttpClient create(boolean proxy) {
        if (proxy) {
            String proxyHost = ConfigUtils.get("proxy.http.host");
            Integer proxyPort = ConfigUtils.getInt("proxy.http.port");
            String proxyAccount = ConfigUtils.get("proxy.http.account");
            String proxyPass = ConfigUtils.get("proxy.http.password");
            return create(proxyHost, proxyPort, proxyAccount, proxyPass);
        } else {
            return create(null, null, null, null);
        }
    }

    public static HttpClient create(String proxyHost, Integer proxyPort, String proxyAccount, String proxyPass) {
        return new HttpClient(createOkHttpClient(proxyHost, proxyPort, proxyAccount, proxyPass));
    }

    public static OkHttpClient createOkHttpClient(boolean proxy) {
        if (proxy) {
            String proxyHost = ConfigUtils.get("proxy.http.host");
            Integer proxyPort = ConfigUtils.getInt("proxy.http.port");
            String proxyAccount = ConfigUtils.get("proxy.http.account");
            String proxyPass = ConfigUtils.get("proxy.http.password");
            return createOkHttpClient(proxyHost, proxyPort, proxyAccount, proxyPass);
        } else {
            return createOkHttpClient(null, null, null, null);
        }
    }

    public static OkHttpClient createOkHttpClient(String proxyHost, Integer proxyPort, String proxyAccount, String proxyPass) {
        TrustManager[] trustManagers = buildTrustManagers();
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(75, TimeUnit.SECONDS)
                .writeTimeout(75, TimeUnit.SECONDS)
                .readTimeout(75, TimeUnit.SECONDS)
                .sslSocketFactory(createSSLSocketFactory(trustManagers), (X509TrustManager) trustManagers[0])
                .hostnameVerifier((hostName, session) -> true)
                .retryOnConnectionFailure(true);
        if (proxyHost != null && proxyPort != null) {
            builder.proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort)));
            if (proxyAccount != null && proxyPass != null) {
                builder.proxyAuthenticator((route, response) -> {
                    //设置代理服务器账号密码
                    String credential = Credentials.basic(proxyAccount, proxyPass);
                    return response.request().newBuilder()
                            .header("Proxy-Authorization", credential)
                            .build();
                });
            }
        }
        return builder.build();
    }

    private static TrustManager[] buildTrustManagers() {
        return new TrustManager[]{
                new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(X509Certificate[] chain, String authType) {
                    }

                    @Override
                    public void checkServerTrusted(X509Certificate[] chain, String authType) {
                    }

                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[]{};
                    }
                }
        };
    }

    /*
     * 生成安全套接字工厂，用于https请求的证书跳过
     */
    private static SSLSocketFactory createSSLSocketFactory(TrustManager[] trustAllCerts) {
        SSLSocketFactory ssfFactory = null;
        try {
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new SecureRandom());
            ssfFactory = sc.getSocketFactory();
        } catch (Exception e) {

        }
        return ssfFactory;
    }
}

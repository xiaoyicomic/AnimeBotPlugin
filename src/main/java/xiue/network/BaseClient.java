package xiue.network;

import okhttp3.*;
import xiue.utils.FileUtil;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.time.Duration;
import java.util.Objects;

public class BaseClient {
    private OkHttpClient client;

    //创建管理器
    private X509TrustManager x509TrustManager = new X509TrustManager() {
        @Override
        public void checkClientTrusted(
                java.security.cert.X509Certificate[] x509Certificates,
                String s) {
        }

        @Override
        public void checkServerTrusted(
                java.security.cert.X509Certificate[] x509Certificates,
                String s) {
        }

        @Override
        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
            return new java.security.cert.X509Certificate[]{};
        }
    };
    private TrustManager[] trustAllCerts = new TrustManager[]{x509TrustManager};

    public BaseClient() {
        client = new OkHttpClient.Builder()
                .readTimeout(Duration.ofSeconds(10))
                .writeTimeout(Duration.ofSeconds(10))
                //信任所有证书
                .hostnameVerifier((s, sslSession) -> true)
                .sslSocketFactory(Objects.requireNonNull(getSSLFactory()), x509TrustManager)
                .addInterceptor(getInterceptor())
                .build();
    }

    protected Interceptor getInterceptor() {
        return chain -> {
            //去掉okhttp3默认的User-Agent
            Request request = chain.request().newBuilder()
                    .removeHeader("User-Agent")
                    .addHeader("User-Agent",
                            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.106 Safari/537.36")
                    .build();
            return chain.proceed(request);
        };
    }

    public Response get(String url) throws IOException {
        Request request = new Request.Builder().url(url).get().build();
        return client.newCall(request).execute();
    }

    public void getAsync(String url, Callback callback) {
        Request request = new Request.Builder().url(url)
                .get().build();
        client.newCall(request).enqueue(callback);
    }

    public OkHttpClient getClient() {
        return this.client;
    }

    private SSLSocketFactory getSSLFactory() {
        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            return sslContext.getSocketFactory();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void downloadFile(String url, File output) throws IOException {
        byte[] data = get(url).body().bytes();
        FileUtil.write(output, data);
    }

    protected String formatStrCharset(byte[] str, String charset) throws UnsupportedEncodingException {
        if (str == null)
            return null;
        return new String(str, charset);
    }

}

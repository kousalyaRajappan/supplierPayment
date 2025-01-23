package za.co.topitup.suppliers;

import android.annotation.SuppressLint;

import java.security.cert.CertificateException;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;

public class UnsafeOkHttpClient {

        public static OkHttpClient getUnsafeOkHttpClient() {
                try {
                        @SuppressLint("CustomX509TrustManager") final TrustManager[] trustAllCerts = new TrustManager[]{
                                new X509TrustManager() {
                                        @SuppressLint("TrustAllX509TrustManager")
                                        @Override
                                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                                        }


                                        @SuppressLint("TrustAllX509TrustManager")
                                        @Override
                                        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                                        }


                                        @Override
                                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                                                return new java.security.cert.X509Certificate[]{};
                                        }
                                }
                        };


                        final SSLContext sslContext = SSLContext.getInstance("SSL");
                        sslContext.init(null, trustAllCerts, new java.security.SecureRandom());


                        // Create an ssl socket factory with our all-trusting manager
                        final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();


                        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                                .connectTimeout(20, TimeUnit.SECONDS)
                                .readTimeout(60, TimeUnit.SECONDS)
                                .writeTimeout(60, TimeUnit.SECONDS)
                                .retryOnConnectionFailure(true);

                        builder.sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0]);
                        builder.hostnameVerifier((hostname, session) -> true);


                        return builder.build();
                } catch (Exception e) {
                        throw new RuntimeException(e);
                }
        }
}
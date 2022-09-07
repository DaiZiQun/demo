package com.example.demo.utils;

import com.alibaba.fastjson.JSONObject;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.*;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Objects;

/**
 * @author DZQ
 * @date 2022/9/7 15:08
 */
public class HttpUtils {

    private static final Logger log = LoggerFactory.getLogger(HttpUtils.class);

    private HttpUtils() {}

    //多线程共享实例
    private static CloseableHttpClient httpClient;

    static {
        SSLContext sslContext = createSSLContext();
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE);
        // 注册http套接字工厂和https套接字工厂
        Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory> create()
                .register("http", PlainConnectionSocketFactory.INSTANCE)
                .register("https", sslsf)
                .build();
        // 连接池管理器
        PoolingHttpClientConnectionManager connMgr = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
        connMgr.setMaxTotal(300);//连接池最大连接数
        connMgr.setDefaultMaxPerRoute(300);//每个路由最大连接数，设置的过小，无法支持大并发
        connMgr.setValidateAfterInactivity(5 * 1000); //在从连接池获取连接时，连接不活跃多长时间后需要进行一次验证
        // 请求参数配置管理器
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(60000)
                .setSocketTimeout(60000)
                .setConnectionRequestTimeout(60000)
                .build();
        // 获取httpClient客户端
        httpClient = HttpClients.custom()
                .setConnectionManager(connMgr)
                .setDefaultRequestConfig(requestConfig)
                .build();
    }

    /**
     * GET请求
     * @param url
     * @return
     */
    public static String getUrl(String url) {
        log.info("请求地址：{}",url);
        return sendHttp(HttpMethod.GET, url, null, null);
    }

    /**
     * GET请求/带头部的信息
     * @param url
     * @param header
     * @return
     */
    public static String getUrl(String url, Map<String, String> header) {
        return sendHttp(HttpMethod.GET, url, header, null);
    }

    /**
     * POST请求/无参数
     * @param url
     * @return
     */
    public static String postJson(String url) {
        return postJson(url, null, null);
    }

    /**
     * POST请求/有参数
     * @param url
     * @param param
     * @return
     */
    public static String postJson(String url, String param) {
        return postJson(url, null, param);
    }

    /**
     * POST请求/无参数带头部
     * @param url
     * @param header
     * @return
     */
    public static String postJson(String url, Map<String, String> header) {
        return postJson(url, header, null);
    }

    /**
     * POST请求/有参数带头部
     * @param url
     * @param header
     * @param params
     * @return
     */
    public static String postJson(String url, Map<String, String> header, String params) {
        return sendHttp(HttpMethod.POST, url, header, params);
    }



    /**
     * 发送http请求(通用方法)
     * @param httpMethod 请求方式（GET、POST、PUT、DELETE）
     * @param url        请求路径
     * @param header     请求头
     * @param params     请求body（json数据）
     * @return 响应文本
     */
    public static String sendHttp(HttpMethod httpMethod, String url, Map<String, String> header, String params) {
        String infoMessage = new StringBuilder().append("request sendHttp，url:").append(url).append("，method:").append(httpMethod.name()).append("，header:").append(JSONObject.toJSONString(header)).append("，param:").append(params).toString();
        log.info(infoMessage);
        //返回结果
        String result = null;
        long beginTime = System.currentTimeMillis();
        try {
            ContentType contentType = ContentType.APPLICATION_JSON.withCharset("UTF-8");
            HttpRequestBase request = buildHttpMethod(httpMethod, url);
            if (Objects.nonNull(header) && !header.isEmpty()) {
                for (Map.Entry<String, String> entry : header.entrySet()) {
                    //打印头部信息
                    if(log.isDebugEnabled()){
                        log.debug(entry.getKey() + ":" + entry.getValue());
                    }
                    request.setHeader(entry.getKey(), entry.getValue());
                }
            }
            if (params!=null ) {
                if(HttpMethod.POST.equals(httpMethod) || HttpMethod.PUT.equals(httpMethod)){
                    ((HttpEntityEnclosingRequest) request).setEntity(new StringEntity(params, contentType));
                }
            }
            CloseableHttpResponse response = httpClient.execute(request);
            HttpEntity httpEntity = response.getEntity();
            log.info("sendHttp response status:{}", response.getStatusLine());
            if (Objects.nonNull(httpEntity)) {
                result = EntityUtils.toString(httpEntity, "UTF-8");
                log.info("sendHttp response body:{}", result);
            }
            HttpClientUtils.closeQuietly(response);//关闭返回对象
        } catch (Exception e) {
            log.error(infoMessage + " failure", e);
        }
        long endTime = System.currentTimeMillis();
        log.info("request sendHttp response time cost:" + (endTime - beginTime) + " ms");
        return result;
    }

    /**
     * 请求方法（全大些）
     */
    public enum HttpMethod {
        GET, POST, PUT, DELETE
    }

    /**
     * 上传返回结果封装
     */
    public static class RequestResult{
        private boolean isSuccess;//是否成功
        private String msg;//消息

        public boolean isSuccess() {
            return isSuccess;
        }

        public RequestResult setSuccess(boolean success) {
            isSuccess = success;
            return this;
        }

        public String getMsg() {
            return msg;
        }

        public RequestResult setMsg(String msg) {
            this.msg = msg;
            return this;
        }

        public RequestResult() {
            this.isSuccess = false;
        }
    }

    /**
     * 构建请求方法
     * @param method
     * @param url
     * @return
     */
    private static HttpRequestBase buildHttpMethod(HttpMethod method, String url) {
        if (HttpMethod.GET.equals(method)) {
            return new HttpGet(url);
        } else if (HttpMethod.POST.equals(method)) {
            return new HttpPost(url);
        } else if (HttpMethod.PUT.equals(method)) {
            return new HttpPut(url);
        } else if (HttpMethod.DELETE.equals(method)) {
            return new HttpDelete(url);
        } else {
            return null;
        }
    }

    /**
     * 配置证书
     * @return
     */
    private static SSLContext createSSLContext(){
        try {
            //信任所有,支持导入ssl证书
            TrustStrategy acceptingTrustStrategy = (cert, authType) -> true;
            SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(null, acceptingTrustStrategy).build();
            return sslContext;
        } catch (Exception e) {
            log.error("初始化ssl配置失败", e);
            throw new RuntimeException("初始化ssl配置失败");
        }
    }

    /**
     * 复制文件流
     * @param input
     * @return
     */
    private static ByteArrayOutputStream cloneInputStream(InputStream input) {
        try {
            ByteArrayOutputStream byteOutStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len;
            while ((len = input.read(buffer)) > -1) {
                byteOutStream.write(buffer, 0, len);
            }
            byteOutStream.flush();
            return byteOutStream;
        } catch (IOException e) {
       }
        return null;
    }

    /**
     * 输入流转字节流
     * @param in
     * @return
     */
    private static byte[] inputStream2byte(InputStream in) {
        ByteArrayOutputStream bos = null;
        try {
            bos = new ByteArrayOutputStream();
            byte[] b = new byte[1024];
            int n;
            while ((n = in.read(b)) != -1) {
                bos.write(b, 0, n);
            }
            in.close();
            bos.close();
            byte[] buffer = bos.toByteArray();
            return buffer;
        } catch (IOException e) {
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
                log.error("clone inputStream error", e);
            }
            try {
                if (bos != null) {
                    bos.close();
                }
            } catch (IOException e) {
                log.error("clone outputStream error", e);
            }
        }
        return null;
    }

}

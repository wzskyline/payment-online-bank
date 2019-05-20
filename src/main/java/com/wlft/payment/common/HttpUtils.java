package com.wlft.payment.common;

import com.wlft.payment.exception.HttpException;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;

public class HttpUtils {

    private static Logger logger = Logger.getLogger(HttpUtils.class);

    public static HttpClient httpClient = new HttpClient();

    /**
         * POST請求
         *
         * @param url - URL
         * @param data - Data
         * @param cookie - Cookie
         * @return 回應
         * @throws HttpException - 網路發生錯誤時，返回該例外
         */
    public static String post(String url, NameValuePair[] data, String cookie ) throws HttpException {
        logger.debug("POST Request: " + url);
        String res = null;

        try {
            PostMethod method = new PostMethod(url);
            method.setRequestHeader("cookie", cookie);
            method.setRequestBody(data);
            httpClient.executeMethod(method);
            res = method.getResponseBodyAsString();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new HttpException("Network Fail!", -1);
        }
        return res;
    }

    /**
         * GET 請求
         *
         * @param url - URL
         * @param params - 參數
         * @param cookie - Cookie
         * @return 回應
         * @throws HttpException - 網路發生錯誤時，返回該例外
         */
    public static String get(String url, Map<String,String> params, String cookie) throws HttpException {
        logger.debug("GET Request: " + url);
        String res = null;

        try {
            GetMethod method = new GetMethod(url);
            method.setRequestHeader("cookie", cookie);
            HttpMethodParams arg = method.getParams();
            for (Map.Entry<String, String> entry : params.entrySet()) {
                arg.setParameter(entry.getKey(),entry.getValue());
            }
            method.setParams(arg);
            httpClient.executeMethod(method);
            res = method.getResponseBodyAsString();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new HttpException("Network Fail!", -1);
        }
        return res;
    }
    /**
     * 向指定URL发送GET方法的请求
     *
     * @param url   发送请求的URL
     * @param param 请求参数，请求参数应该是 name1=value1&name2=value2 的形式。
     * @return URL 所代表远程资源的响应结果
     */
    public static String sendGet(String url, String param) {
        String result = "";
        BufferedReader in = null;
        try {
            String urlNameString = url + "?" + param;
            URL realUrl = new URL(urlNameString);
            // 打开和URL之间的连接
            URLConnection connection = realUrl.openConnection();
            // 设置通用的请求属性
            connection.setRequestProperty("accept", "*/*");
            connection.setRequestProperty("connection", "Keep-Alive");
            connection.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            // 建立实际的连接
            connection.connect();
            // 获取所有响应头字段
            Map<String, List<String>> map = connection.getHeaderFields();
            // 遍历所有的响应头字段
            //for (String key : map.keySet()) {
            //    System.out.println(key + "--->" + map.get(key));
            //}
            // 定义 BufferedReader输入流来读取URL的响应
            in = new BufferedReader(new InputStreamReader(
                    connection.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
            System.out.println("发送GET请求出现异常！" + e);
            e.printStackTrace();
        }
        // 使用finally块来关闭输入流
        finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        return result;
    }
}

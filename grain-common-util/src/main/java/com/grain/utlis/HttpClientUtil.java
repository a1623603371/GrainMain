package com.grain.utlis;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;


import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HttpClientUtil {
    public static String doGet(String url) {
        //创建Client对象
        CloseableHttpClient httpClient = HttpClients.createDefault();
        //创建httpclient get请求
        HttpGet httpGet = new HttpGet(url);
        CloseableHttpResponse response = null;
        try {
            //执行请求
            response = httpClient.execute(httpGet);
            //判断状态是否为200
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                HttpEntity entity = response.getEntity();
                String result = EntityUtils.toString(entity, "UTF-8");
                EntityUtils.consume(entity);
                httpClient.close();
                return result;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return null;
    }

    public static String doPost(String url, Map<String, String> paramMap) {
        //创建client对象
        CloseableHttpClient httpClient = HttpClients.createDefault();
        //创建httpPost对象
        HttpPost httpPost = new HttpPost(url);
        CloseableHttpResponse response = null;
        try {
            if (paramMap != null) {
                List<BasicNameValuePair> list = new ArrayList<>();
                paramMap.forEach((k, v) -> {
                    list.add(new BasicNameValuePair(k, v));
                });
                HttpEntity entity = new UrlEncodedFormEntity(list, "utf-8");
                httpPost.setEntity(entity);
            }
            //执行请求
            response = httpClient.execute(httpPost);
            //判断返回状态是200
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                HttpEntity httpEntity = response.getEntity();
                String result = EntityUtils.toString(httpEntity, "utf-8");
                EntityUtils.consume(httpEntity);
                httpClient.close();
                return result;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return null;
    }
}

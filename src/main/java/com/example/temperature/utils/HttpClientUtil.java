package com.example.temperature.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.nacos.client.utils.StringUtils;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HttpClientUtil {

    public static RestTemplate restTemplate(){
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setReadTimeout(30000);
        factory.setConnectTimeout(5000);
        RestTemplate restTemplate = new RestTemplate(factory);

        List<HttpMessageConverter<?>> messageConverters = restTemplate.getMessageConverters();
        for (int i = 0; i < messageConverters.size(); i++) {
            HttpMessageConverter<?> httpMessageConverter = messageConverters.get(i);
            if (httpMessageConverter.getClass().equals(StringHttpMessageConverter.class)) {
                messageConverters.set(i, new StringHttpMessageConverter(StandardCharsets.UTF_8));
            }
        }
        return restTemplate;
    }

    @Retryable(value = RestClientException.class, maxAttempts = 3, backoff = @Backoff(delay = 5000L,multiplier = 2))
    public static String getStringJson(String url) {
        RestTemplate restTemplate = restTemplate();
        HttpHeaders requestHeaders = new HttpHeaders();
        HttpEntity httpEntity = new HttpEntity(requestHeaders);
        ResponseEntity<String> results = restTemplate.exchange(url, HttpMethod.GET, httpEntity, String.class);
        return results.getBody();
    }


    public static Map<String, String> getMapJson(String url) {
        String body = getStringJson(url);
        return getMapSwap(body);
    }

    public static Map<String, String> getMapJson2(String url) {
        String body = getStringJson(url);
        Map<String, String> bodyMap = getMap(body);
        if(StringUtils.isBlank(bodyMap.get("weatherinfo"))){
            return null;
        }

        return getMap(bodyMap.get("weatherinfo"));
    }


    private static Map<String, String> getMap(String res) {
        Map maps = (Map) JSON.parse(res);
        Map<String, String> ret = new HashMap<>();
        for (Object map : maps.entrySet()) {
            ret.put(gstring(((Map.Entry) map).getKey()), gstring(((Map.Entry) map).getValue()));
        }
        return ret;
    }

    /**
     * '{"name":"李雷","age":"19",}'
     * - > {"李雷":"name","19":"age"}
     * @param res
     * @return
     */
    private static Map<String, String> getMapSwap(String res) {
        Map maps = (Map) JSON.parse(res);
        Map<String, String> ret = new HashMap<>();
        for (Object map : maps.entrySet()) {
            ret.put(gstring(((Map.Entry) map).getValue()), gstring(((Map.Entry) map).getKey()));
        }
        return ret;
    }

    private static String gstring(Object value) {
        if (value == null || "".equals(value)) {
            return null;
        }
        return value.toString();
    }


}

package com.example.temperature.controller;

import com.alibaba.nacos.client.utils.StringUtils;
import com.example.temperature.utils.HttpClientUtil;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Semaphore;

/**
 * @author xiaoyu
 * @data 2021/8/26.
 */
public class TemperatureManage {

    private final Semaphore semaphore;

    private String provinceUrl;
    private String cityUrl;
    private String countyUrl;
    private String weatherUrl;

    public TemperatureManage(int count, String provinceUrl, String cityUrl, String countyUrl, String weatherUrl){
        this.semaphore = new Semaphore(count, true);
        this.provinceUrl = provinceUrl;
        this.cityUrl = cityUrl;
        this.countyUrl = countyUrl;
        this.weatherUrl = weatherUrl;
    }

    public Optional<Integer> getTemperature(String province, String city, String county) {
        try {
            semaphore.acquire();
            System.out.println("正在使用资源..." + Thread.currentThread().getName());

            Thread.sleep(1000);

            Optional<Integer> optional = getTemperature2(province, city, county);
            return optional;
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            semaphore.release();
            System.out.println("使用完释放资源...，剩余可用数量：" + semaphore.availablePermits());
        }

        return Optional.empty();
    }

    private Optional<Integer> getTemperature2(String province, String city, String county) {
        Map<String, String> provinceMap = HttpClientUtil.getMapJson(provinceUrl);
        String provinceCode =  provinceMap.get(province);
        if(StringUtils.isBlank(provinceCode)){
            System.out.println("province isn't exist");
            return Optional.empty();
        }

        String url2 = String.format(cityUrl, provinceCode);
        Map<String, String> cityMap = HttpClientUtil.getMapJson(url2);
        String cityCode =  cityMap.get(city);
        if(StringUtils.isBlank(cityCode)){
            System.out.println("city isn't exist");
            return Optional.empty();
        }

        String url3 = String.format(countyUrl, provinceCode + cityCode);
        Map<String, String> countyMap = HttpClientUtil.getMapJson(url3);
        String countyCode =  countyMap.get(county);
        if(StringUtils.isBlank(countyCode)){
            System.out.println("county isn't exist");
            return Optional.empty();
        }

        String url4 = String.format(weatherUrl, provinceCode + cityCode + countyCode);
        Map<String, String> result = HttpClientUtil.getMapJson2(url4);
        if(CollectionUtils.isEmpty(result)){
            System.out.println("faild to find temperature");
            return Optional.empty();
        }
        String tempStr = result.get("temp");
        if(StringUtils.isBlank(tempStr)){
            System.out.println("temp is null");
            return Optional.empty();
        }

        BigDecimal bigDecimal = new BigDecimal(tempStr);
        int temp = bigDecimal.setScale(0, BigDecimal.ROUND_UP).intValue();

        return Optional.of(temp);
    }

}

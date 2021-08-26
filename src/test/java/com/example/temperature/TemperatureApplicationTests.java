package com.example.temperature;

import com.example.temperature.controller.TemperatureManage;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.util.Assert;

import java.util.Optional;
import java.util.concurrent.CountDownLatch;

@SpringBootTest
class TemperatureApplicationTests {

    @Autowired
    private Environment env;

    @Test
    void testTemperature() {
        // 最大TPS
        int maxTPS = Integer.valueOf(env.getProperty("tps.max"));
        // 测试请求数
        int testCount = Integer.valueOf(env.getProperty("test.count"));
        String provinceUrl = env.getProperty("china.code.url.province");
        String cityUrl = env.getProperty("china.code.url.city");
        String countyUrl = env.getProperty("china.code.url.county");
        String weatherUrl = env.getProperty("china.code.url.weather");
        // 阻塞主线程等待子线程执行完在退出
        CountDownLatch latch = new CountDownLatch(testCount);

        TemperatureManage temperatureManage = new TemperatureManage(maxTPS, provinceUrl, cityUrl, countyUrl, weatherUrl);
        Thread[] threads = new Thread[testCount];
        for (int i = 0; i < testCount; i++){
            int id = i;
            threads[i] = new Thread(){
                @Override
                public void run(){
                    System.out.println("userId：" + id + "，使用资源准备...");
                    Optional<Integer> temp = temperatureManage.getTemperature("江苏","苏州","常熟");
                    Assert.notNull(temp.get(),"faild get temperature of one certain county in China");
                    System.out.println("temp：" + temp.get() + "，使用资源结束...");
                    latch.countDown();
                }
            };
        }

        // 执行
        for (int i = 0; i < testCount; i++){
            Thread thread = threads[i];
            thread.start();
        }

        // 等待子线程执行完成
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

}

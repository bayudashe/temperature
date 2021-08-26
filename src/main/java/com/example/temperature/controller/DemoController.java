package com.example.temperature.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * @author xiaoyu
 * @data 2021/8/25.
 */
@RestController
@RequestMapping("/demo")
public class DemoController {

    @RequestMapping("SpringBoot")
    public String stringBoot(){
        return "Hello World! this is my SpringBoot";
    }

}

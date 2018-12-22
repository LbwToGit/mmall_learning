package com.mmall.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/test/")
public class TestController {
    @RequestMapping(value = "test.do")
    @ResponseBody
    public String test(){
        System.out.println("===============================>");
        System.out.println("===============================>");
        System.out.println("===============================>");
        System.out.println("===============================>");

        return null;
    }
}

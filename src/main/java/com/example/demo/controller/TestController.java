package com.example.demo.controller;

import com.example.demo.service.TestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author DZQ
 * @date 2022/9/7 14:23
 */
@RestController("testController")
@RequestMapping("")
public class TestController {
    @Autowired
    private TestService testService;
    @GetMapping("/index")
    public String test() {
        testService.test1();
        return "success";
    }



}

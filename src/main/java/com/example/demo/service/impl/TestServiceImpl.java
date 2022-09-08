package com.example.demo.service.impl;

import com.alibaba.fastjson.JSON;
import com.example.demo.dto.TokenResult;
import com.example.demo.dto.User;
import com.example.demo.dto.moban;
import com.example.demo.service.TestService;
import com.example.demo.utils.HttpUtils;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author DZQ
 * @date 2022/9/7 14:20
 */
@Service
@EnableScheduling
public class TestServiceImpl implements TestService {


    private final String APPID = "wx6a4e715a2456bb2b";

    private final String APPSECRET = "9215e9f63065f9df9a748286159671d3";
    //获取token
    private final String GETTOKEN = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=";
    //获取用户列表
    private final String GETUSERS = "https://api.weixin.qq.com/cgi-bin/user/get?access_token=";

    private final String GETUSERMESSAGE = "https://api.weixin.qq.com/cgi-bin/message/template/send?access_token=";

    private final String key = "2f72ec0b78de4e99bb1c4130b1296aec";

    private final String TIANQI = "https://devapi.qweather.com/v7/weather/now?";

    private long time = new Date().getTime();


    private String token;

    @Override
    @Scheduled(cron = "* * 0/2 * * ?")
    public void test1() {
        long now = new Date().getTime();
        if ((now - time) > 7200000 || token == null) {
            TokenResult tokenResult = JSON.parseObject(HttpUtils.getUrl(GETTOKEN + APPID + "&secret=" + APPSECRET), TokenResult.class);
            token = tokenResult.getAccess_token();
        }
        User user = JSON.parseObject(HttpUtils.getUrl(GETUSERS + token + "&next_openid="), User.class);


        String hefengResult = HttpUtils.getUrl(TIANQI + "key=" + key + "&location=120.37,30.30&lang=zh");
        String tianqi = "";
        String wendu="";
        String tiganwendu="";
        if (!StringUtils.isEmpty(hefengResult)) {
            tianqi = Arrays.asList(hefengResult.split(",")).stream().filter(t -> t.contains("text")).collect(Collectors.toList()).get(0).split(":")[1];
            wendu = Arrays.asList(hefengResult.split(",")).stream().filter(t -> t.contains("temp")).collect(Collectors.toList()).get(0).split(":")[1];
            tiganwendu = Arrays.asList(hefengResult.split(",")).stream().filter(t -> t.contains("feelsLike")).collect(Collectors.toList()).get(0).split(":")[1];
        }
        String zhongwen = "";
        String yinwen="";
        String meiriyijuResult=HttpUtils.getUrl("http://open.iciba.com/dsapi/");
        if(StringUtils.hasLength(meiriyijuResult)){
            zhongwen = Arrays.asList(meiriyijuResult.split(",")).stream().filter(t -> t.contains("note")).collect(Collectors.toList()).get(0).split(":")[1];
            yinwen = Arrays.asList(meiriyijuResult.split(",")).stream().filter(t -> t.contains("content")).collect(Collectors.toList()).get(0).split(":")[1];
        }

        for (String s : user.getData().getOpenid()) {
            if(s.equals("ok-5Q55hLD8kgL9WVuwNvYC5LV6A")){
                Map<String, Object> param = new HashMap<>();
                Map<String, Object> data = new HashMap<>();
                data.put("frist", new moban(tianqi, "#173177"));
                data.put("query1", new moban(wendu, "#173177"));
                data.put("query2", new moban(tiganwendu, "#173177"));
                data.put("query3", new moban(yinwen, "#173177"));
                data.put("query4", new moban(zhongwen, "#173177"));
                param.put("touser", s);
                param.put("template_id", "8-6091tOKg7QOplIzmwk3_lbR0U63NoKCpRDi-HJYls");
                param.put("url", "http://weixin.qq.com/download");
                param.put("topcolor", "#FF0000");
                param.put("data", data);
                String jsonString = JSON.toJSONString(param);
                String result=HttpUtils.postJson(GETUSERMESSAGE+token,jsonString);
                System.out.println(result);
            }

        }


    }


}

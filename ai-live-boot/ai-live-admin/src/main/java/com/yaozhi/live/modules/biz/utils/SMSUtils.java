package com.yaozhi.live.modules.biz.utils;

import com.aliyuncs.CommonRequest;
import com.aliyuncs.CommonResponse;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SMSUtils {
    @Value("${aliyun.sms.accessKeyId}")
    private String accessKeyId;
    @Value("${aliyun.sms.secret}")
    private String secret;
    @Value("${aliyun.sms.signName}")
    private String signName; // 短信签名
    @Value("${aliyun.sms.templateCode}")
    private String templateCode;  //短信模板
    @Value("${aliyun.sms.regionId}")
    private String regionId;   // 短信服务器区域


    public void sendSms(String phone, String code) {

        DefaultProfile profile = DefaultProfile.getProfile("cn-hangzhou", accessKeyId, secret);
        DefaultAcsClient client = new DefaultAcsClient(profile);
        CommonRequest request = new CommonRequest();

        request.setSysMethod(MethodType.POST);
        //下面两个不能动
        request.setSysProduct("Dysmsapi");
        request.setSysDomain("dysmsapi.aliyuncs.com");

        request.setSysVersion("2017-05-25");
        request.setSysAction("SendSms");
        //自定义参数(手机号,验证码,签名,模板)
        request.putQueryParameter("RegoinId", regionId);
        request.putQueryParameter("PhoneNumbers", phone);
        request.putQueryParameter("SignName", signName); //填自己申请的名称
        request.putQueryParameter("TemplateCode", templateCode);
        request.putQueryParameter("TemplateParam", "{\"code\":\"" + code + "\"}");

        try {
            CommonResponse response = client.getCommonResponse(request);
            log.info("阿里云短信响应信息：{}", response.getData());
            boolean success = response.getHttpResponse().isSuccess();
            log.info("短信发送是否成功：{}", success);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("短信发送失败：{}", e.getMessage());
        }
    }
}

package com.yaozhi.live;

import com.yaozhi.live.modules.biz.dto.GenerateScriptRequest;
import com.yaozhi.live.modules.biz.service.AiScriptService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;


@RunWith(SpringRunner.class)
@SpringBootTest
public class AiTest {

    @Autowired
    private AiScriptService aiScriptService;

    @Test
    public void test() {
        GenerateScriptRequest request = new GenerateScriptRequest();
        request.setInput("你好");
        request.setProjectId(1L);
        String text = aiScriptService.generateScript(request);

        System.out.println(text);
    }

}

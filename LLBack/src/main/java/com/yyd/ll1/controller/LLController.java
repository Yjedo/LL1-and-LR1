package com.yyd.ll1.controller;

import com.yyd.ll1.LL1.LL1;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
public class LLController {
    @RequestMapping("/analyze")
    public Object analyze(String GS, String str) throws IOException {
        System.out.println(GS+" "+str);
        LL1 ll1 = new LL1();
        //从前台获取文法、与测试句子
        ll1.readGS(GS, str);
        return ll1.run();
    }
}

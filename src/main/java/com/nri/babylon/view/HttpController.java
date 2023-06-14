package com.nri.babylon.view;

import com.nri.babylon.testing.TestLibs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Controller
public class HttpController {

    @Autowired
    TestLibs testLibs;

    @GetMapping("/")
    public String getIndex(){
        return "index";
    }

    @GetMapping("/test-libs")
    public String getTestLibs(){
        testLibs.printTest();
        return "test-lib";
    }

    @GetMapping("/audioTest")
    public String index() {
        return "audioTest";
    }
}

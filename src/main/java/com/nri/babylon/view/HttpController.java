package com.nri.babylon.view;

import com.nri.babylon.testing.TestLibs;
import com.nri.library.text_translation.NRITextTranslation;
import com.nri.library.tts.NRITextToSpeech;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HttpController {

    @Autowired
    private NRITextToSpeech nriTextToSpeech;

    @Autowired
    private NRITextTranslation nriTextTranslation;

    @Autowired
    private TestLibs testLibs;

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


    @GetMapping("/call")
    public String getIndex2(Model model) {
        model.addAttribute("voices", nriTextToSpeech.getVoices());
        model.addAttribute("languages", nriTextTranslation.getSupportedLanguages());
        return "index2";
    }
}

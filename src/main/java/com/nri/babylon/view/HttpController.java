package com.nri.babylon.view;

import com.nri.babylon.testing.TestLibs;
import com.nri.babylon.view.model.Register;
import com.nri.library.text_translation.NRITextTranslation;
import com.nri.library.tts.NRITextToSpeech;
import org.kurento.tutorial.groupcall.UserSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class HttpController {
    private static final Logger log = LoggerFactory.getLogger(HttpController.class);

    @Autowired
    private NRITextToSpeech nriTextToSpeech;

    @Autowired
    private NRITextTranslation nriTextTranslation;

    @Autowired
    private TestLibs testLibs;

    @GetMapping("/")
    public String getIndex() {
        return "index";
    }

    @GetMapping("/test-libs")
    public String getTestLibs() {
        testLibs.printTest();
        return "test-lib";
    }

    @GetMapping("/audioTest")
    public String index() {
        return "audioTest";
    }

    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("voices", nriTextToSpeech.getVoices());
        model.addAttribute("languages", nriTextTranslation.getSupportedLanguages());

        return "register-page";
    }


    @GetMapping("/call")
    public String getIndex2(Model model) {
        model.addAttribute("voices", nriTextToSpeech.getVoices());
        model.addAttribute("languages", nriTextTranslation.getSupportedLanguages());
        return "index2";
    }


    @GetMapping("/call2")
    public String getCall2(Model model,
                           @RequestParam(name = "name") String name, @RequestParam(name = "room") String room,
                           @RequestParam(name = "voiceId") String voiceId, @RequestParam(name = "languageId") String languageId) {

        log.info("request param: name : " + name);
        log.info("request param: room : " + room);
        log.info("request param: voiceId : " + voiceId);
        log.info("request param: languageId : " + languageId);
        model.addAttribute("myName",name);
        model.addAttribute("myRoomName",room);
        model.addAttribute("myVoice",voiceId);
        model.addAttribute("myLanguage",languageId);

        model.addAttribute("voices", nriTextToSpeech.getVoices());
        model.addAttribute("languages", nriTextTranslation.getSupportedLanguages());
        return "index2";
    }

    @PostMapping("/register")
    public String greetingSubmit(Model model, @ModelAttribute Register register) {
        String name = "jeffName";
        String room = "testRoom";
        String voiceId = "testVoiceId";
        String languageId = "testLangId";


        String requestParam =
                "?name=" + name + "&" + "room="
                        + room + "&" + "voiceId="
                        + voiceId + "&" + "languageId=" + languageId;
        return "redirect:/call2" +requestParam;
    }
}

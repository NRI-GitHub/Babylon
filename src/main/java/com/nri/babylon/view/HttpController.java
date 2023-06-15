package com.nri.babylon.view;

import com.nri.babylon.testing.TestLibs;
import com.nri.babylon.view.model.Register;
import com.nri.library.text_translation.NRITextTranslation;
import com.nri.library.tts.NRITextToSpeech;
import org.kurento.tutorial.groupcall.Room;
import org.kurento.tutorial.groupcall.RoomManager;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class HttpController {
    private static final Logger log = LoggerFactory.getLogger(HttpController.class);

    @Autowired
    private NRITextToSpeech nriTextToSpeech;

    @Autowired
    private NRITextTranslation nriTextTranslation;

    @Autowired
    private TestLibs testLibs;

    @Autowired
    private RoomManager roomManager;

    @GetMapping("/")
    public String getIndex() {
        return "redirect:/register";
    }


    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("voices", nriTextToSpeech.getVoices());
        model.addAttribute("languages", nriTextTranslation.getSupportedLanguages());

        return "register-page";
    }

    @PostMapping("/register")
    public String registerSubmit(Model model, @ModelAttribute Register register,
                                 RedirectAttributes redirectAttributes) {
        if (!register.validate()) {
            redirectAttributes.addFlashAttribute("errorMessage", "There was an error in the form data. Please check your inputs.");
            return "redirect:/register";
        }

        String name = register.getName();
        String room = register.getRoom();
        String voiceId = register.getVoice();
        String languageId = register.getLanguageId();

        Room room1 = roomManager.getRoom(room);
        boolean isRoomFull = room1.getParticipants().size() >=2;
        if (isRoomFull){
            redirectAttributes.addFlashAttribute("errorMessage", "The room is currently full!");
            return "redirect:/register";
        }

        String requestParam =
                "?name=" + name + "&" + "room="
                        + room + "&" + "voiceId="
                        + voiceId + "&" + "languageId=" + languageId;
        return "redirect:/meet" +requestParam;
    }

    @GetMapping("/meet")
    public String getMeet(Model model,
                           @RequestParam(name = "name") String name, @RequestParam(name = "room") String room,
                           @RequestParam(name = "voiceId") String voiceId, @RequestParam(name = "languageId") String languageId) {

        model.addAttribute("myName",name);
        model.addAttribute("myRoomName",room);
        model.addAttribute("myVoice",voiceId);
        model.addAttribute("myLanguage",languageId);

        model.addAttribute("voices", nriTextToSpeech.getVoices());
        model.addAttribute("languages", nriTextTranslation.getSupportedLanguages());
        return "index";
    }
}

package no.toreb.nrknewsconsumer.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.info.BuildProperties;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

@Controller
@RequiredArgsConstructor
class HomeController {

    private final BuildProperties buildProperties;

    @GetMapping({"/*"})
    public String index() {
        return "index";
    }

    @ModelAttribute("buildProperties")
    private BuildProperties buildProperties() {
        return buildProperties;
    }
}

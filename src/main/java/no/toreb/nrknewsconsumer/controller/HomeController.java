package no.toreb.nrknewsconsumer.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.info.BuildProperties;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

@Controller
class HomeController {

    private final BuildProperties buildProperties;

    private final int serverPort;

    private final String contextPath;

    public HomeController(final BuildProperties buildProperties,
                          @Value("${server.port}") final int serverPort,
                          @Value("${server.servlet.context-path:}") final String contextPath) {
        this.buildProperties = buildProperties;
        this.serverPort = serverPort;
        this.contextPath = contextPath;
    }

    @GetMapping({ "/*"})
    public String index() {
        return "index";
    }

    @ModelAttribute("buildProperties")
    private BuildProperties buildProperties() {
        return buildProperties;
    }

    @ModelAttribute("baseUrl")
    private String baseUrl() {
        return String.format("http://localhost:%d%s/api",
                             serverPort, StringUtils.hasLength(contextPath) ? "/" + contextPath : "");
    }
}

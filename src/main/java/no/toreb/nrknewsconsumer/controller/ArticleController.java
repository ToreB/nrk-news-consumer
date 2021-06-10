package no.toreb.nrknewsconsumer.controller;

import lombok.RequiredArgsConstructor;
import no.toreb.nrknewsconsumer.model.Article;
import no.toreb.nrknewsconsumer.repository.ArticleRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/articles")
@RequiredArgsConstructor
class ArticleController {

    private final ArticleRepository articleRepository;

    @GetMapping
    public List<Article> getAll(@RequestParam(value = "page", defaultValue = "0") final long page,
                                @RequestParam(value = "size", defaultValue = "10") final long size) {
        return articleRepository.findAll(size, page * size);
    }
}

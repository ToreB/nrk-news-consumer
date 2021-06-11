package no.toreb.nrknewsconsumer.controller;

import lombok.RequiredArgsConstructor;
import no.toreb.nrknewsconsumer.model.Article;
import no.toreb.nrknewsconsumer.repository.ArticleRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/articles")
@RequiredArgsConstructor
class ArticleController {

    private final ArticleRepository articleRepository;

    @GetMapping
    public List<Article> getAll(@RequestParam(value = "page", defaultValue = "1") final long page,
                                @RequestParam(value = "size", defaultValue = "10") final long size) {
        return articleRepository.findAll(size, (page - 1) * size);
    }

    @GetMapping("/hidden")
    public List<Article> getAllHidden(@RequestParam(value = "page", defaultValue = "1") final long page,
                                      @RequestParam(value = "size", defaultValue = "10") final long size) {
        return articleRepository.findAllHidden(size, (page - 1) * size);
    }

    @PutMapping("/hidden")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void hide(@RequestBody final HideArticleRequest request) {
        if (request.isHide()) {
            articleRepository.hideArticle(request.getArticleId());
        } else {
            articleRepository.showArticle(request.getArticleId());
        }
    }
}

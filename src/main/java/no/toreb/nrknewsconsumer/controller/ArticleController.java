package no.toreb.nrknewsconsumer.controller;

import lombok.RequiredArgsConstructor;
import no.toreb.nrknewsconsumer.service.ArticleService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/articles")
@RequiredArgsConstructor
class ArticleController {

    private final ArticleService articleService;

    @GetMapping
    public ArticleResponse getAllNonHandled(final PageParam pageParam) {
        return articleService.getAllNonHandled(pageParam);
    }

    @GetMapping("/hidden")
    public ArticleResponse getAllHidden(final PageParam pageParam) {
        return articleService.getAllHidden(pageParam);
    }

    @GetMapping("/read-later")
    public ArticleResponse getAllReadLater(final PageParam pageParam) {
        return articleService.getAllReadLater(pageParam);
    }

    @GetMapping("/covid-19")
    public ArticleResponse getAllCovid19(final PageParam pageParam) {
        return articleService.getAllCovid19(pageParam);
    }

    @GetMapping("/ukraine-russia")
    public ArticleResponse getAllUkraineRussia(final PageParam pageParam) {
        return articleService.getAllUkraineRussia(pageParam);
    }

    @PutMapping("/hidden")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void hide(@RequestBody final HideArticleRequest request) {
        articleService.toggleArticleVisibility(request.getArticleId(), request.isHide());
    }

    @PutMapping("/read-later")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void readLater(@RequestBody final ReadLaterRequest request) {
        articleService.toggleReadLater(request.getArticleId(), request.isReadLater());
    }
}

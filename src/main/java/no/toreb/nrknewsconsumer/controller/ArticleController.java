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

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/articles")
@RequiredArgsConstructor
class ArticleController {

    private final ArticleRepository articleRepository;

    @GetMapping
    public ArticleResponse getAllNonHandled(@RequestParam(value = "page", defaultValue = "1") final long page,
                                            @RequestParam(value = "size", defaultValue = "10") final long size) {
        final List<Article> articles = articleRepository.findAllNonHandled(size, calculateOffset(page, size));
        final long totalCount = articleRepository.countAllNonHandled();
        return new ArticleResponse(articles, totalCount);
    }

    @GetMapping("/hidden")
    public ArticleResponse getAllHidden(@RequestParam(value = "page", defaultValue = "1") final long page,
                                        @RequestParam(value = "size", defaultValue = "10") final long size) {
        final List<Article> articles = articleRepository.findAllHidden(size, calculateOffset(page, size));
        return new ArticleResponse(articles, -1);
    }

    @GetMapping("/read-later")
    public ArticleResponse getAllReadLater(@RequestParam(value = "page", defaultValue = "1") final long page,
                                           @RequestParam(value = "size", defaultValue = "10") final long size) {
        final List<Article> articles = articleRepository.findAllReadLater(size, calculateOffset(page, size));
        final long totalCount = articleRepository.countReadLater();
        return new ArticleResponse(articles, totalCount);
    }

    @GetMapping("/covid-19")
    public ArticleResponse getAllCovid19(@RequestParam(value = "page", defaultValue = "1") final long page,
                                         @RequestParam(value = "size", defaultValue = "10") final long size) {
        final List<Article> articles = articleRepository.findAllCovid19(size, calculateOffset(page, size));
        final long totalCount = articleRepository.countCovid19();
        return new ArticleResponse(articles, totalCount);
    }

    @GetMapping("/ukraine-russia")
    public ArticleResponse getAllUkraineRussia(@RequestParam(value = "page", defaultValue = "1") final long page,
                                               @RequestParam(value = "size", defaultValue = "10") final long size) {
        final List<Article> articles = articleRepository.findAllUkraineRussia(size, calculateOffset(page, size));
        final long totalCount = articleRepository.countUkraineRussia();
        return new ArticleResponse(articles, totalCount);
    }

    @PutMapping("/hidden")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void hide(@RequestBody final HideArticleRequest request) {
        if (request.isHide()) {
            articleRepository.hideArticle(request.getArticleId(), currentDateTimeAsString());
        } else {
            articleRepository.showArticle(request.getArticleId());
        }
    }

    @PutMapping("/read-later")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void readLater(@RequestBody final ReadLaterRequest request) {
        if (request.isReadLater()) {
            articleRepository.addReadLater(request.getArticleId(), currentDateTimeAsString());
        } else {
            articleRepository.removeReadLater(request.getArticleId());
        }
    }

    private String currentDateTimeAsString() {
        return OffsetDateTime.now()
                             .withOffsetSameInstant(ZoneOffset.UTC)
                             .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }

    private long calculateOffset(final long page, final long size) {
        return (page - 1) * size;
    }
}

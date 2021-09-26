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
    public List<Article> getAllNonHidden(@RequestParam(value = "page", defaultValue = "1") final long page,
                                         @RequestParam(value = "size", defaultValue = "10") final long size) {
        return articleRepository.findAllNonHandled(size, (page - 1) * size);
    }

    @GetMapping("/hidden")
    public List<Article> getAllHidden(@RequestParam(value = "page", defaultValue = "1") final long page,
                                      @RequestParam(value = "size", defaultValue = "10") final long size) {
        return articleRepository.findAllHidden(size, (page - 1) * size);
    }

    @GetMapping("/read-later")
    public List<Article> getAllReadLater(@RequestParam(value = "page", defaultValue = "1") final long page,
                                         @RequestParam(value = "size", defaultValue = "10") final long size) {
        return articleRepository.findAllReadLater(size, (page - 1) * size);
    }

    @GetMapping("/covid-19")
    public List<Article> getAllCovid19(@RequestParam(value = "page", defaultValue = "1") final long page,
                                       @RequestParam(value = "size", defaultValue = "10") final long size) {
        return articleRepository.findAllCovid19(size, (page - 1) * size);
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
}

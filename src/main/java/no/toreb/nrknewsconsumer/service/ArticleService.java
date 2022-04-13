package no.toreb.nrknewsconsumer.service;

import lombok.RequiredArgsConstructor;
import no.toreb.nrknewsconsumer.controller.ArticleResponse;
import no.toreb.nrknewsconsumer.controller.PageParam;
import no.toreb.nrknewsconsumer.model.Article;
import no.toreb.nrknewsconsumer.repository.ArticleRepository;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ArticleService {

    private final ArticleRepository articleRepository;

    public ArticleResponse getAllNonHandled(final PageParam pageParam) {
        final List<Article> articles = articleRepository.findAllNonHandled(pageParam.getSize(),
                                                                           calculateOffset(pageParam),
                                                                           pageParam.getSortOrder());
        final long totalCount = articleRepository.countAllNonHandled();
        return new ArticleResponse(articles, totalCount);
    }

    public ArticleResponse getAllHidden(final PageParam pageParam) {
        final List<Article> articles = articleRepository.findAllHidden(pageParam.getSize(),
                                                                       calculateOffset(pageParam),
                                                                       pageParam.getSortOrder());
        return new ArticleResponse(articles, -1);
    }

    public ArticleResponse getAllReadLater(final PageParam pageParam) {
        final List<Article> articles = articleRepository.findAllReadLater(pageParam.getSize(),
                                                                          calculateOffset(pageParam),
                                                                          pageParam.getSortOrder());
        final long totalCount = articleRepository.countReadLater();
        return new ArticleResponse(articles, totalCount);
    }

    public ArticleResponse getAllCovid19(final PageParam pageParam) {
        final List<Article> articles = articleRepository.findAllCovid19(pageParam.getSize(),
                                                                        calculateOffset(pageParam),
                                                                        pageParam.getSortOrder());
        final long totalCount = articleRepository.countCovid19();
        return new ArticleResponse(articles, totalCount);
    }

    public ArticleResponse getAllUkraineRussia(final PageParam pageParam) {
        final List<Article> articles = articleRepository.findAllUkraineRussia(pageParam.getSize(),
                                                                              calculateOffset(pageParam),
                                                                              pageParam.getSortOrder());
        final long totalCount = articleRepository.countUkraineRussia();
        return new ArticleResponse(articles, totalCount);
    }

    public void toggleArticleVisibility(final String articleId, final boolean hide) {
        if (hide) {
            articleRepository.hideArticle(articleId, currentDateTimeUtc());
        } else {
            articleRepository.showArticle(articleId);
        }
    }

    public void toggleReadLater(final String articleId, final boolean readLater) {
        if (readLater) {
            articleRepository.addReadLater(articleId, currentDateTimeUtc());
        } else {
            articleRepository.removeReadLater(articleId);
        }
    }

    private OffsetDateTime currentDateTimeUtc() {
        return OffsetDateTime.now().withOffsetSameInstant(ZoneOffset.UTC);
    }

    private int calculateOffset(final PageParam pageParam) {
        return (pageParam.getPage() - 1) * pageParam.getSize();
    }
}

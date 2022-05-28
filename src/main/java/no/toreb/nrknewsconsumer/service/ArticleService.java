package no.toreb.nrknewsconsumer.service;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import no.toreb.nrknewsconsumer.controller.ArticleResponse;
import no.toreb.nrknewsconsumer.controller.PageParam;
import no.toreb.nrknewsconsumer.model.Article;
import no.toreb.nrknewsconsumer.repository.ArticleRepository;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleService {

    private final ArticleRepository articleRepository;

    public ArticleResponse getAllNonHandled(final PageParam pageParam) {
        return createArticleResponse(() -> articleRepository.findAllNonHandled(pageParam.getSize(),
                                                                               calculateOffset(pageParam),
                                                                               pageParam.getSortOrder()),
                                     articleRepository::countAllNonHandled);
    }

    public ArticleResponse getAllHidden(final PageParam pageParam) {
        final List<Article> articles = articleRepository.findAllHidden(pageParam.getSize(),
                                                                       calculateOffset(pageParam),
                                                                       pageParam.getSortOrder());
        return new ArticleResponse(articles, -1);
    }

    public ArticleResponse getAllReadLater(final PageParam pageParam) {
        return createArticleResponse(() -> articleRepository.findAllReadLater(pageParam.getSize(),
                                                                              calculateOffset(pageParam),
                                                                              pageParam.getSortOrder()),
                                     articleRepository::countReadLater);
    }

    public ArticleResponse getAllDisease(final PageParam pageParam) {
        return createArticleResponse(() -> articleRepository.findAllDisease(pageParam.getSize(),
                                                                            calculateOffset(pageParam),
                                                                            pageParam.getSortOrder()),
                                     articleRepository::countDisease);
    }

    public ArticleResponse getAllUkraineRussia(final PageParam pageParam) {
        return createArticleResponse(() -> articleRepository.findAllUkraineRussia(pageParam.getSize(),
                                                                                  calculateOffset(pageParam),
                                                                                  pageParam.getSortOrder()),
                                     articleRepository::countUkraineRussia);
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

    private ArticleResponse createArticleResponse(final Supplier<List<Article>> articlesSupplier,
                                                  final Supplier<Long> articleCountSupplier) {
        final CompletableFuture<List<Article>> articlesFuture = CompletableFuture.supplyAsync(articlesSupplier);
        final CompletableFuture<Long> countFuture = CompletableFuture.supplyAsync(articleCountSupplier);

        try {
            return new ArticleResponse(get(articlesFuture), get(countFuture));
        } catch (final Exception e) {
            cancel(articlesFuture);
            cancel(countFuture);
            throw e;
        }
    }

    @SneakyThrows
    private <T> T get(final CompletableFuture<T> future) {
        return future.get(30, TimeUnit.SECONDS);
    }

    private void cancel(final CompletableFuture<?> future) {
        try {
            if (!future.isDone()) {
                future.cancel(true);
            }
        } catch (final Exception e) {
            log.error("Error occurred when cancelling CompletableFuture.", e);
        }
    }

    private OffsetDateTime currentDateTimeUtc() {
        return OffsetDateTime.now().withOffsetSameInstant(ZoneOffset.UTC);
    }

    private int calculateOffset(final PageParam pageParam) {
        return (pageParam.getPage() - 1) * pageParam.getSize();
    }
}

package no.toreb.nrknewsconsumer.task;

import no.toreb.nrknewsconsumer.model.Article;
import no.toreb.nrknewsconsumer.repository.ArticleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@MockitoSettings
class FetchArticlesTaskTest {

    @Mock
    private ArticleRepository articleRepository;

    @Mock
    private ArticleFetcher articleFetcher;

    private final String feedUrl = "http://the-feed-url.no";

    private FetchArticlesTask task;

    @BeforeEach
    void setUp() {
        task = new FetchArticlesTask("TestTask",
                                     articleRepository,
                                     articleFetcher,
                                     feedUrl,
                                     Duration.ofSeconds(1));
    }

    @Test
    void run_whenTaskHasNotBeenRunYet_shouldRunImmediately() {
        task.run();

        verify(articleFetcher).fetch(feedUrl);
    }

    @Test
    void run_whenTaskHasBeenRunAndWaitDurationHasNotPassed_shouldNotRun() {
        task.run();
        verify(articleFetcher).fetch(feedUrl);

        task.run();
        // Should still only be one time.
        verify(articleFetcher, times(1)).fetch(feedUrl);
    }

    @Test
    void run_whenTaskHasBeenRunAndWaitDurationHasPassed_shouldRun() throws InterruptedException {
        task.run();
        verify(articleFetcher).fetch(feedUrl);

        TimeUnit.SECONDS.sleep(1);

        task.run();
        verify(articleFetcher, times(2)).fetch(feedUrl);
    }

    @Test
    void run_shouldFetchAndSaveArticles() {
        final List<Article> articles = List.of(
                article("1"),
                article("2"),
                article("3"),
                article("4"),
                article("5")
        );

        when(articleFetcher.fetch(any())).thenReturn(articles);

        task.run();

        articles.forEach(article -> verify(articleRepository).save(article));
    }

    private Article article(final String articleId) {
        return Article.builder()
                      .articleId(articleId)
                      .publishedAt(OffsetDateTime.now())
                      .build();

    }
}
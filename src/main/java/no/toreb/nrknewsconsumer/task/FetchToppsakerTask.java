package no.toreb.nrknewsconsumer.task;

import lombok.extern.slf4j.Slf4j;
import no.toreb.nrknewsconsumer.model.Article;
import no.toreb.nrknewsconsumer.repository.ArticleRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@ConditionalOnProperty(name = "task.fetch-toppsaker.enabled", havingValue = "true", matchIfMissing = true)
class FetchToppsakerTask {

    private final ArticleRepository articleRepository;

    private final ArticleFetcher articleFetcher;

    private final String articlesFeedUrl;

    private final Duration syncRateDuration;

    private LocalDateTime lastFetchTime;

    public FetchToppsakerTask(final ArticleRepository articleRepository,
                              final ArticleFetcher articleFetcher,
                              @Value("${task.fetch-toppsaker.articles-feed-url}") final String articlesFeedUrl,
                              @Value("${task.fetch-toppsaker.fetch-rate}") final Duration syncRateDuration) {
        this.articleRepository = articleRepository;
        this.articleFetcher = articleFetcher;
        this.articlesFeedUrl = articlesFeedUrl;
        this.syncRateDuration = syncRateDuration;
    }

    @Transactional
    @Scheduled(fixedDelayString = "${task.fetch-toppsaker.fixed-delay}",
               initialDelayString = "${task.fetch-toppsaker.initial-delay}")
    public void run() {
        log.trace("Run task");
        if (!shouldFetch()) {
            return;
        }

        final List<Article> articles = articleFetcher.fetch(articlesFeedUrl);
        final List<Article> newArticles = articleRepository.filterOutExistingArticles(articles);

        log.info("Found {} / {} new news articles from {}", newArticles.size(), articles.size(), articlesFeedUrl);
        newArticles.forEach(articleRepository::save);
        lastFetchTime = LocalDateTime.now();
    }

    /**
     * By checking whether to fetch as a separate calculation from the task scheduling, combined with a rather frequent
     * scheduling of the task, the task should be able to better handle if the operating system frequently goes into
     * sleep or hibernation.
     * This should make the task fetch shortly after the OS wakes up.
     *
     * @return true if articles should be fetched, false otherwise.
     */
    private boolean shouldFetch() {
        log.trace("Last fetch time: {}", lastFetchTime);
        if (lastFetchTime == null) {
            return true;
        }

        final LocalDateTime nextSyncTime = lastFetchTime.plusSeconds(syncRateDuration.toSeconds());
        log.trace("Next sync time: {}", nextSyncTime);
        return LocalDateTime.now().compareTo(nextSyncTime) >= 0;
    }
}

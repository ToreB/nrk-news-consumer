package no.toreb.nrknewsconsumer.task;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.toreb.nrknewsconsumer.model.Article;
import no.toreb.nrknewsconsumer.repository.ArticleRepository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class FetchArticlesTask {

    private final String taskName;

    private final ArticleRepository articleRepository;

    private final ArticleFetcher articleFetcher;

    private final String articlesFeedUrl;

    private final Duration syncRateDuration;

    private LocalDateTime lastFetchTime;

    @Transactional
    public void run() {
        log.trace("Run task {}", taskName);
        if (!shouldFetch()) {
            return;
        }

        final List<Article> articles = articleFetcher.fetch(articlesFeedUrl);
        final List<Article> newArticles = articleRepository.filterOutExistingArticles(articles);

        log.info("{}: Found {} / {} new news articles from {}",
                 taskName, newArticles.size(), articles.size(), articlesFeedUrl);
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
        log.trace("{}: Last fetch time: {}", taskName, lastFetchTime);
        if (lastFetchTime == null) {
            return true;
        }

        final LocalDateTime nextSyncTime = lastFetchTime.plusSeconds(syncRateDuration.toSeconds());
        log.trace("{}: Next sync time: {}", taskName, nextSyncTime);
        return LocalDateTime.now().compareTo(nextSyncTime) >= 0;
    }
}

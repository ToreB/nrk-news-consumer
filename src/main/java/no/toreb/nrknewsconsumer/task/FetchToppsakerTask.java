package no.toreb.nrknewsconsumer.task;

import lombok.extern.slf4j.Slf4j;
import no.toreb.nrknewsconsumer.model.Article;
import no.toreb.nrknewsconsumer.repository.ArticleRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@ConditionalOnProperty(name = "task.fetch-toppsaker.enabled", havingValue = "true", matchIfMissing = true)
class FetchToppsakerTask {

    private final ArticleRepository articleRepository;

    private final ArticleFetcher articleFetcher;

    private final String articlesFeedUrl;

    public FetchToppsakerTask(final ArticleRepository articleRepository,
                              final ArticleFetcher articleFetcher,
                              @Value("${task.fetch-toppsaker.articles-feed-url}") final String articlesFeedUrl) {
        this.articleRepository = articleRepository;
        this.articleFetcher = articleFetcher;
        this.articlesFeedUrl = articlesFeedUrl;
    }

    @Transactional
    @Scheduled(fixedDelayString = "${task.fetch-toppsaker.fixed-delay}",
               initialDelayString = "${task.fetch-toppsaker.initial-delay}")
    public void run() {
        final List<Article> articles = articleFetcher.fetch(articlesFeedUrl);

        final List<String> existingArticleIds = articleRepository.distinctArticleIds();

        final List<Article> newArticles =
                articles.stream()
                        .filter(article -> !existingArticleIds.contains(article.getArticleId()))
                        .collect(Collectors.toList());

        log.info("Found {} new news articles", newArticles.size());
        newArticles.forEach(articleRepository::save);
    }
}

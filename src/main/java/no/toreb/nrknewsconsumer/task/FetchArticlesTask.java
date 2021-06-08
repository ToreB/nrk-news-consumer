package no.toreb.nrknewsconsumer.task;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.toreb.nrknewsconsumer.model.Article;
import no.toreb.nrknewsconsumer.repository.ArticleRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
class FetchArticlesTask {

    private final ArticleRepository articleRepository;

    private final ArticleFetcher articleFetcher;

    @Transactional
    @Scheduled(fixedDelayString = "${task.fetch-articles.fixed-delay}",
               initialDelayString = "${task.fetch-articles.initial-delay}")
    public void run() {
        final List<Article> articles = articleFetcher.fetch();

        final List<String> existingArticleIds = articleRepository.distinctArticleIds();

        final List<Article> newArticles = articles.stream()
                                              .filter(article -> !existingArticleIds.contains(article.getArticleId()))
                                              .collect(Collectors.toList());

        log.info("Found {} new news articles", newArticles.size());
        newArticles.forEach(articleRepository::save);
    }
}

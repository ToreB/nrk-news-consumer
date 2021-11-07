package no.toreb.nrknewsconsumer.task;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.toreb.nrknewsconsumer.model.Article;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ArticleFetcher {

    private final RestTemplate restTemplate;

    private final ArticleFeedParser feedParser;

    public List<Article> fetch(final String articlesFeedUrl) {
        log.debug("Fetching articles from {}.", articlesFeedUrl);
        final ResponseEntity<String> response;
        try {
            response = restTemplate.getForEntity(articlesFeedUrl, String.class);
            log.debug("Response: {}", response.getStatusCode());
        } catch (final RestClientResponseException e) {
            log.error("Fetch articles from feed failed with response {}: {}",
                      e.getRawStatusCode(), e.getResponseBodyAsString());
            throw e;
        }

        final List<Article> articles = feedParser.parseFeed(response.getBody());

        log.debug("Fetched {} articles from {}.", articles.size(), articlesFeedUrl);

        return articles;
    }
}

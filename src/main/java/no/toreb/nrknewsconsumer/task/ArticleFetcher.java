package no.toreb.nrknewsconsumer.task;

import lombok.extern.slf4j.Slf4j;
import no.toreb.nrknewsconsumer.model.Article;
import no.toreb.nrknewsconsumer.model.ArticleCategory;
import no.toreb.nrknewsconsumer.model.ArticleMedia;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.XML;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
public class ArticleFetcher {

    private final RestTemplate restTemplate;

    public ArticleFetcher(final RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

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

        //noinspection ConstantConditions
        final JSONObject content = XML.toJSONObject(response.getBody(), true);
        final JSONArray items = content.getJSONObject("rss")
                                       .getJSONObject("channel")
                                       .getJSONArray("item");
        final Set<Article> articles = new HashSet<>();
        items.forEach(element -> {
            final JSONObject item = (JSONObject) element;
            final JSONArray categories = item.optJSONArray("category");
            final Set<ArticleCategory> articleCategories = new HashSet<>();
            if (categories != null) {
                categories.forEach(category -> articleCategories.add(new ArticleCategory(category.toString())));
            }

            final JSONObject mediaContent = item.optJSONObject("media:content");
            final Set<ArticleMedia> articleMedia = new HashSet<>();
            if (mediaContent != null) {
                final JSONObject mediaCredit = mediaContent.optJSONObject("media:credit");
                articleMedia.add(ArticleMedia.builder()
                                             .medium(mediaContent.getString("medium"))
                                             .type(mediaContent.optString("type"))
                                             .url(mediaContent.getString("url"))
                                             .title(mediaContent.optString("media:title", null))
                                             .credit(mediaCredit != null ? mediaCredit.optString("content") : null)
                                             .build());
            }

            final Article article = Article.builder()
                                           .articleId(extractArticleId(item))
                                           .title(item.getString("title"))
                                           .description(item.optString("description", null))
                                           .link(item.getString("link"))
                                           .author(item.optString("dc:creator", null))
                                           .publishedAt(OffsetDateTime.parse(item.getString("dc:date"))
                                                                      .withOffsetSameInstant(ZoneOffset.UTC))
                                           .categories(articleCategories)
                                           .media(articleMedia)
                                           .build();
            articles.add(article);
        });

        log.debug("Fetched {} articles from {}.", articles.size(), articlesFeedUrl);

        return articles.stream()
                       .sorted(Comparator.comparing(Article::getPublishedAt))
                       .collect(Collectors.toList());
    }

    private String extractArticleId(final JSONObject item) {
        return item.getJSONObject("guid").getString("content");
    }
}

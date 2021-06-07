package no.toreb.nrknewsconsumer.task;

import lombok.extern.slf4j.Slf4j;
import no.toreb.nrknewsconsumer.model.Article;
import no.toreb.nrknewsconsumer.model.ArticleCategory;
import no.toreb.nrknewsconsumer.model.ArticleMedia;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.XML;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
public class ArticleFetcher {

    private final String articlesFeedUrl;

    private final RestTemplate restTemplate;

    public ArticleFetcher(final RestTemplate restTemplate,
                          @Value("${articles-feed-url}") final String articlesFeedUrl) {
        this.restTemplate = restTemplate;
        this.articlesFeedUrl = articlesFeedUrl;
    }

    public List<Article> fetch() {
        log.debug("Fetch articles from {}", articlesFeedUrl);
        final ResponseEntity<String> response = restTemplate.getForEntity(articlesFeedUrl, String.class);
        log.debug("Response: {}", response.getStatusCode());
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException(String.format("Fetch articles from feed failed with response %d: %s",
                                                     response.getStatusCodeValue(), response.getBody()));
        }

        //noinspection ConstantConditions
        final JSONObject content = XML.toJSONObject(response.getBody(), true);
        final JSONArray items = content.getJSONObject("rss").getJSONObject("channel").getJSONArray("item");
        final List<Article> articles = new ArrayList<>();
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
                                                                      .withOffsetSameInstant(ZoneOffset.UTC)
                                                                      .toLocalDateTime())
                                           .categories(articleCategories)
                                           .media(articleMedia)
                                           .build();
            articles.add(article);
        });

        log.debug("Fetched {} articles", articles.size());

        return articles;
    }

    private String extractArticleId(final JSONObject item) {
        return item.getJSONObject("guid").getString("content");
    }
}

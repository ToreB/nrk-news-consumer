package no.toreb.nrknewsconsumer.task;

import lombok.extern.slf4j.Slf4j;
import no.toreb.nrknewsconsumer.model.Article;
import no.toreb.nrknewsconsumer.model.ArticleCategory;
import no.toreb.nrknewsconsumer.model.ArticleMedia;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.XML;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
public class ArticleFeedParser {

    public List<Article> parseFeed(final String feedContent) {
        final JSONObject content = XML.toJSONObject(feedContent, true);
        final JSONArray items = content.getJSONObject("rss")
                                       .getJSONObject("channel")
                                       .getJSONArray("item");
        final Set<Article> articles = new HashSet<>();
        items.forEach(element -> {
            final JSONObject item = (JSONObject) element;

            final String articleId = extractArticleId(item);
            final String articleLink = item.optString("link", null);
            if (articleId == null || !StringUtils.hasText(articleLink)) {
                log.warn("Found article without ID or link. It will be skipped. Article: {}", item);
                return;
            }

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

            final String dateString = item.optString("dc:date", null);
            final OffsetDateTime publishedAt;
            if (dateString != null) {
                publishedAt = OffsetDateTime.parse(dateString).withOffsetSameInstant(ZoneOffset.UTC);
            } else {
                log.warn("Found article without date. Current date and time will be used. Article: {}", item);
                publishedAt = OffsetDateTime.now(Clock.systemUTC());
            }
            final Article article = Article.builder()
                                           .articleId(articleId)
                                           .title(item.getString("title"))
                                           .description(item.optString("description", null))
                                           .link(articleLink)
                                           .author(item.optString("dc:creator", null))
                                           .publishedAt(publishedAt)
                                           .categories(articleCategories)
                                           .media(articleMedia)
                                           .build();
            articles.add(article);
        });

        return articles.stream()
                       .sorted(Comparator.comparing(Article::getPublishedAt))
                       .collect(Collectors.toList());
    }

    private String extractArticleId(final JSONObject item) {
        return Optional.ofNullable(item.optJSONObject("guid"))
                       .map(guid -> guid.optString("content", null))
                       .orElse(null);
    }
}

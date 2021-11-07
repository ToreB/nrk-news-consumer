package no.toreb.nrknewsconsumer.task;

import no.toreb.nrknewsconsumer.model.Article;
import no.toreb.nrknewsconsumer.model.ArticleCategory;
import no.toreb.nrknewsconsumer.model.ArticleMedia;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.XML;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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

        return articles.stream()
                       .sorted(Comparator.comparing(Article::getPublishedAt))
                       .collect(Collectors.toList());
    }

    private String extractArticleId(final JSONObject item) {
        return item.getJSONObject("guid").getString("content");
    }
}

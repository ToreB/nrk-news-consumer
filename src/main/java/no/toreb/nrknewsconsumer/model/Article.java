package no.toreb.nrknewsconsumer.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;
import org.springframework.data.annotation.Id;

import java.time.OffsetDateTime;
import java.util.Set;

@Value
@Builder(toBuilder = true)
public class Article {

    @Id
    Long genId;
    String articleId;
    String title;
    String description;
    String link;
    String author;
    OffsetDateTime publishedAt;

    Set<ArticleMedia> media;
    Set<ArticleCategory> categories;

    @JsonCreator
    public Article(@JsonProperty("genId") final Long genId,
                   @JsonProperty("articleId") final String articleId,
                   @JsonProperty("title") final String title,
                   @JsonProperty("description") final String description,
                   @JsonProperty("link") final String link,
                   @JsonProperty("author") final String author,
                   @JsonProperty("publishedAt") final OffsetDateTime publishedAt,
                   @JsonProperty("media") final Set<ArticleMedia> media,
                   @JsonProperty("categories") final Set<ArticleCategory> categories) {
        this.genId = genId;
        this.articleId = articleId;
        this.title = title;
        this.description = description;
        this.link = link;
        this.author = author;
        this.publishedAt = publishedAt;
        this.media = Set.copyOf(media);
        this.categories = Set.copyOf(categories);
    }
}

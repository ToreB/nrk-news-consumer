package no.toreb.nrknewsconsumer.model;

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
}

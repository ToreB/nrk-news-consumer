alter table hidden_articles add column hidden_at text;

update hidden_articles
set hidden_at = (select published_at from article a where a.article_id = article)
where hidden_at is null;
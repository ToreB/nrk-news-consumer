create table article
(
    gen_id       integer not null,
    article_id   text not null,
    title        text not null,
    description  text,
    link         text not null,
    author       text,
    published_at text not null,
    created_at   text default (strftime('%Y-%m-%dT%H:%M:%fZ', 'now')) not null,

    constraint article_pk primary key (gen_id)
);

create table article_media
(
    article integer not null,
    medium  text    not null,
    type    text,
    url     text    not null,
    credit  text,
    title   text,

    constraint article_media_pk primary key (article, medium)
);

create table article_category
(
    article  integer not null,
    category text    not null,

    constraint article_category_pk primary key (article, category)
);


create table article
(
    gen_id       bigint identity         not null,
    article_id   varchar(4000)           not null,
    title        varchar(4000)           not null,
    description  varchar(4000),
    link         varchar(4000)           not null,
    author       varchar(4000),
    published_at timestamp               not null,
    created_at   timestamp default now() not null,

    constraint article_pk primary key (gen_id)
);

create table article_media
(
    article bigint        not null,
    medium  varchar(4000) not null,
    type    varchar(4000),
    url     varchar(4000) not null,
    credit  varchar(4000),
    title   varchar(4000),

    constraint article_media_pk primary key (article, medium)
);

create table article_category
(
    article  bigint        not null,
    category varchar(4000) not null,

    constraint article_category_pk primary key (article, category)
);


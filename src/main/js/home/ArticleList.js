import { Button, Grid } from "@material-ui/core";
import React, { useEffect, useState } from 'react';

function fetchArticles(url, page) {
    return fetch(`${url}?size=12&page=${page}`)
        .then(res => res.json());
}

function ArticleList({ articlesUrl }) {
    let [articles, setArticles] = useState([]);
    let [page, setPage] = useState(1);

    useEffect(() => {
        fetchArticles(articlesUrl, page)
            .then(resultJson => setArticles(resultJson))
    }, [page]);

    let buttonStyle = {
        fontSize: '24px',
        marginRight: '30px'
    };

    return (
        <div id="articles">
            <Grid container>
                {articles.map(article => <ArticleElement key={article.articleId} article={article} />)}
            </Grid>
            <div id="pagination">
                <Button style={buttonStyle}
                        disabled={page === 1}
                        onClick={() => setPage(page - 1)}>Previous</Button>
                <Button style={buttonStyle}
                        onClick={() => setPage(page + 1)}>Next</Button>
            </div>
        </div>
    );
}

function ArticleElement({ article }) {
    let itemStyle = {
        border: '1px solid black',
        borderRadius: '5px',
        margin: '10px 10px 10px',
        padding: '25px',
        maxWidth: '400px'
    };
    let titleStyle = {
        fontSize: '20px',
        fontWeight: 'bold',
        display: 'inline'
    };
    let publishedStyle = {
        fontWeight: 'bold',
        fontSize: 'small',
        marginTop: '0px',
        textAlign: 'left'
    };

    let images = article.media.filter(media => media.medium === 'image');

    return (
        <Grid item key={article.articleId} style={itemStyle} xs={12}>
            <p style={publishedStyle}>{formatDate(article.publishedAt)}</p>
            <a target="_blank" href={article.link}><p style={titleStyle}>{article.title}</p></a>
            <p>{article.description}</p>
            {images.map((image, i) => <img key={i} src={image.url} alt={image.title} width="100%" />)}
        </Grid>
    );
}

function formatDate(dateString) {
    let options = {
        weekday: 'long',
        month: 'long',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
    };
    return new Date(dateString).toLocaleString('no-nb', options);
}

export default ArticleList;
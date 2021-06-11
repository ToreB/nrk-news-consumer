import { Button, Grid, Switch } from "@material-ui/core";
import React, { useEffect, useState } from 'react';

function fetchArticles(url, page) {
    return fetch(`${url}/articles?size=12&page=${page}`)
        .then(res => res.json());
}

function toggleArticleVisibility(url, articleId, hide, successCallback) {
    let requestOptions = {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ articleId, hide })
    };
    fetch(`${url}/articles/hide`, requestOptions)
        .then(res => {
            if (res.status >= 200 && res.status < 300) {
                successCallback();
            }
        });
}

function ArticleList({ baseUrl }) {
    let [articles, setArticles] = useState([]);
    let [page, setPage] = useState(1);
    let [hiddenArticles, setHiddenArticles] = useState([]);

    let toggleArticleVisibilityFunction = (articleId, hide, callback) => {
        toggleArticleVisibility(baseUrl, articleId, hide, callback)
        if (hide) {
            setHiddenArticles([...hiddenArticles, articleId])
        } else {
            setHiddenArticles(hiddenArticles.filter(article => article !== articleId))
        }
    };

    let loadArticles = () => {
        fetchArticles(baseUrl, page).then(resultJson => setArticles(resultJson))
        setHiddenArticles([]);
    };

    useEffect(() => loadArticles(), [page]);

    let buttonStyle = {
        fontSize: '24px',
        marginRight: '30px'
    };

    return (
        <div id="articles">
            <Grid container>
                {articles.map(article => <ArticleElement key={article.articleId}
                                                         article={article}
                                                         toggleArticleVisibilityFunction={toggleArticleVisibilityFunction} />)}
            </Grid>
            <div id="pagination">
                <Button style={buttonStyle}
                        disabled={page === 1 || hiddenArticles.length > 0}
                        onClick={() => setPage(page - 1)}>Previous</Button>
                <Button style={buttonStyle}
                        onClick={() => {
                            loadArticles();
                        }}>Reload</Button>
                <Button style={buttonStyle}
                        disabled={hiddenArticles.length > 0}
                        onClick={() => setPage(page + 1)}>Next</Button>
            </div>
        </div>
    );
}

function ArticleElement({ article, toggleArticleVisibilityFunction }) {
    let [hidden, setHidden] = useState(false);

    let itemStyle = {
        border: '1px solid black',
        borderRadius: '5px',
        margin: '10px 10px 10px',
        padding: '15px 25px',
        maxWidth: '400px',
        backgroundColor: hidden ? 'lightGray': 'white'
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
    let hideSwitchStyle = {}

    let images = article.media.filter(media => media.medium === 'image');

    return (
        <Grid item key={article.articleId} style={itemStyle} xs={12}>
            <Grid container alignItems="baseline">
                <Grid item xs={6}>
                    <p style={publishedStyle}>{formatDate(article.publishedAt)}</p>
                </Grid>
                <Grid item container justify="flex-end" xs={6}>
                    <Switch style={hideSwitchStyle}
                            checked={hidden}
                            onChange={() => toggleArticleVisibilityFunction(
                                article.articleId,
                                !hidden, () => setHidden(!hidden))}
                            size="medium" />
                </Grid>
            </Grid>
            <div style={{ clear: 'both' }}>
                <a target="_blank" href={article.link}><p style={titleStyle}>{article.title}</p></a>
                <p>{article.description}</p>
                {images.map((image, i) => <img key={i} src={image.url} alt={image.title} width="100%" />)}
            </div>
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
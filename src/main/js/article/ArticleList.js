import { Button, Chip, Grid, Switch } from "@material-ui/core";
import React, { useEffect, useState } from 'react';

export const Mode = {
    NON_HIDDEN: "non-hidden",
    HIDDEN: "hidden",
    COVID_19: "covid-19"
};

function fetchArticles(apiContextPath, page, mode) {
    let path = `${apiContextPath}/articles`;
    switch (mode) {
        case Mode.HIDDEN:
            path += "/hidden";
            break;
        case Mode.COVID_19:
            path += "/covid-19"
            break;
    }

    return fetch(`${path}?size=12&page=${page}`)
        .then(res => res.json());
}

function toggleArticleVisibility(apiContextPath, articleId, hide, successCallback) {
    let requestOptions = {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ articleId, hide })
    };
    fetch(`${apiContextPath}/articles/hidden`, requestOptions)
        .then(res => {
            if (res.status >= 200 && res.status < 300) {
                successCallback();
            }
        });
}

function ArticleList({ apiContextPath, mode }) {
    let [articles, setArticles] = useState([]);
    let [page, setPage] = useState(1);
    let [toggledArticles, setToggledArticles] = useState([]);

    let toggleArticleVisibilityFunction = (articleId, toggled, callback) => {
        toggleArticleVisibility(apiContextPath, articleId, toggled, callback)
        if (toggled) {
            setToggledArticles([...toggledArticles, articleId])
        } else {
            setToggledArticles(toggledArticles.filter(article => article !== articleId))
        }
    };

    let loadArticles = () => {
        fetchArticles(apiContextPath, page, mode).then(resultJson => setArticles(resultJson))
        setToggledArticles([]);
        window.scrollTo(0, 0);
    };

    useEffect(() => loadArticles(), [page]);

    let buttonStyle = {
        fontSize: '24px',
        marginRight: '30px'
    };

    return (
        <div id="articles">
            <Grid container>
                {articles.map(article => {
                    return <ArticleElement key={article.articleId}
                                           article={article}
                                           initiallyHidden={mode === Mode.HIDDEN}
                                           toggleArticleVisibilityFunction={toggleArticleVisibilityFunction} />
                })}
            </Grid>
            <div id="pagination">
                <Button style={buttonStyle}
                        disabled={page === 1 || toggledArticles.length > 0}
                        onClick={() => setPage(page - 1)}>Previous</Button>
                <Button style={buttonStyle}
                        onClick={() => loadArticles()}>Reload</Button>
                <Button style={buttonStyle}
                        disabled={articles.length === 0 || toggledArticles.length > 0}
                        onClick={() => setPage(page + 1)}>Next</Button>
            </div>
        </div>
    );
}

function ArticleElement({ article, initiallyHidden, toggleArticleVisibilityFunction }) {
    let [hidden, setHidden] = useState(initiallyHidden);

    let itemStyle = {
        border: '1px solid black',
        borderRadius: '5px',
        margin: '10px 10px 10px',
        padding: '15px 25px',
        maxWidth: '400px',
        backgroundColor: hidden ? 'lightGray' : 'white'
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
    let chipStyle = {
        marginRight: '5px',
        marginTop: '5px'
    };

    let images = article.media.filter(media => media.medium === 'image');
    let categories = article.categories.map(it => it.category);

    let toggleHidden = hidden => toggleArticleVisibilityFunction(article.articleId, hidden, () => setHidden(hidden));

    return (
        <Grid item key={article.articleId} style={itemStyle} xs={12}>
            <Grid container alignItems="baseline">
                <Grid item xs={6}>
                    <p style={publishedStyle}>{formatDate(article.publishedAt)}</p>
                </Grid>
                <Grid item container justify="flex-end" xs={6}>
                    <Switch checked={hidden}
                            onChange={() => toggleHidden(!hidden)}
                            size="medium" />
                </Grid>
            </Grid>
            <div style={{ clear: 'both' }}>
                <a target="_blank"
                   href={article.link}
                   onClick={() => toggleHidden(true)}><p style={titleStyle}>{article.title}</p></a>
                <p>{article.description}</p>
                {images.map((image, i) => <img key={i} src={image.url} alt={image.title} width="100%" />)}
                <div>
                    {categories.map((category, i) =>
                        <Chip key={i} label={category} variant="default" size="small" style={chipStyle} />)}
                </div>
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
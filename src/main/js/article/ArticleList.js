import { Button, Chip, Grid, IconButton, Switch } from "@material-ui/core";
import WatchLaterIcon from "@material-ui/icons/WatchLater";
import WatchLaterOutlinedIcon from "@material-ui/icons/WatchLaterOutlined";
import React, { useEffect, useState } from 'react';

export const Mode = {
    NON_HIDDEN: "non-hidden",
    HIDDEN: "hidden",
    COVID_19: "covid-19",
    READ_LATER: "read-later"
};

function resolvePath(apiContextPath, mode) {
    let path = `${apiContextPath}/articles`;
    switch (mode) {
        case Mode.HIDDEN:
            path += "/hidden";
            break;
        case Mode.COVID_19:
            path += "/covid-19"
            break;
        case Mode.READ_LATER:
            path += "/read-later"
            break;
    }

    return path;
}

function fetchArticles(apiContextPath, page, mode) {
    let path = resolvePath(apiContextPath, mode);

    return fetch(`${path}?size=12&page=${page}`)
        .then(res => res.json());
}

function toggleArticleVisibility(apiContextPath, articleId, hide, successCallback) {
    const requestOptions = {
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

function toggleReadLater(apiContextPath, articleId, readLater, successCallback) {
    const requestOptions = {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ articleId, readLater })
    };
    fetch(`${apiContextPath}/articles/read-later`, requestOptions)
        .then(res => {
            if (res.status >= 200 && res.status < 300) {
                successCallback();
            }
        });
}

function ArticleList({ apiContextPath, mode }) {
    const [articles, setArticles] = useState([]);
    const [page, setPage] = useState(1);
    const [hiddenToggledArticles, setHiddenToggledArticles] = useState([]);
    const [readLaterToggledArticles, setReadLaterToggledArticles] = useState([]);
    const [totalCountArticles, setTotalCountArticles] = useState(0);

    const toggleArticleVisibilityFunction = (articleId, toggled, callback) => {
        toggleArticleVisibility(apiContextPath, articleId, toggled, callback);
        const markToggled = mode === Mode.HIDDEN ? !toggled : toggled;
        if (markToggled) {
            setHiddenToggledArticles([...hiddenToggledArticles, articleId]);
        } else {
            setHiddenToggledArticles(hiddenToggledArticles.filter(id => id !== articleId));
        }
    };
    const toggleReadLaterFunction = (articleId, toggled, callback) => {
        toggleReadLater(apiContextPath, articleId, toggled, callback);
        if (toggled) {
            setReadLaterToggledArticles([...readLaterToggledArticles, articleId]);
        } else {
            setReadLaterToggledArticles(readLaterToggledArticles.filter(id => id !== articleId));
        }
    };

    const loadArticles = (isReload = false) => {
        fetchArticles(apiContextPath, page, mode)
            .then(resultJson => {
                setTotalCountArticles(resultJson.totalCount);
                const fetchedArticles = resultJson.articles;
                if (!isReload) {
                    setArticles(fetchedArticles);
                    return;
                }

                const newArticles = fetchedArticles.map(article => {
                    return { ...article, isNew: !articles.some(it => it.articleId === article.articleId) }
                });
                setArticles(newArticles);
            })
        setHiddenToggledArticles([]);
        setReadLaterToggledArticles([]);
        window.scrollTo(0, 0);
    };

    useEffect(() => loadArticles(), [page]);

    const buttonStyle = {
        fontSize: '24px',
        marginRight: '30px'
    };

    const countViewStyle = {
        display: mode === Mode.HIDDEN ? 'none' : 'block',
        border: '1px solid black',
        borderRadius: '5px',
        margin: '10px 10px 10px',
        padding: '0px 25px',
        maxWidth: '350px',
        fontWeight: 'bold'
    };

    const countToggledArticles = hiddenToggledArticles.length + readLaterToggledArticles.length;
    return (
        <div id="articles">
            <Grid container>
                <Grid item xs={12}>
                    <div style={countViewStyle}>
                        <p>Articles in this view: {totalCountArticles}</p>
                    </div>
                </Grid>
                {articles.map(article => {
                    return <ArticleElement key={article.articleId}
                                           article={article}
                                           initiallyHidden={mode === Mode.HIDDEN}
                                           initiallyReadLater={mode === Mode.READ_LATER}
                                           toggleArticleVisibilityFunction={toggleArticleVisibilityFunction}
                                           toggleReadLaterFunction={toggleReadLaterFunction}
                    />
                })}
            </Grid>
            <div id="pagination">
                <Button style={buttonStyle}
                        disabled={page === 1 || countToggledArticles > 0}
                        onClick={() => setPage(page - 1)}>Previous</Button>
                <Button style={buttonStyle}
                        onClick={() => loadArticles(true)}>Reload</Button>
                <Button style={buttonStyle}
                        disabled={articles.length === 0 || countToggledArticles > 0}
                        onClick={() => setPage(page + 1)}>Next</Button>
            </div>
        </div>
    );
}

function ArticleElement({ article, toggleArticleVisibilityFunction, toggleReadLaterFunction }) {
    const [hidden, setHidden] = useState(article.hidden === true);
    const [readLater, setReadLater] = useState(article.readLater === true)

    const images = article.media.filter(media => media.medium === 'image');
    const categories = article.categories.map(it => it.category);

    const itemStyle = {
        border: `1px ${readLater ? 'dashed' : 'solid'} black`,
        borderRadius: '5px',
        margin: '10px 10px 10px',
        padding: '5px 25px 15px',
        maxWidth: '400px',
        backgroundColor: hidden ? 'lightGray' : 'white'
    };
    const titleStyle = {
        fontSize: '20px',
        fontWeight: 'bold',
        display: 'inline'
    };
    const publishedStyle = {
        fontWeight: 'bold',
        fontSize: 'small',
        textAlign: 'left'
    };
    const chipStyle = {
        marginRight: '5px',
        marginTop: '5px',
    };
    const chipStyleYtring = {
        ...chipStyle,
        backgroundColor: 'orange'
    };
    const newArticleIndicatorStyle = {
        height: '10px',
        width: '10px',
        borderRadius: '5px',
        backgroundColor: 'green',
        position: 'absolute',
        marginLeft: '-18px',
        marginTop: '2px'
    };

    const readLaterIcon = readLater ? <WatchLaterIcon /> : <WatchLaterOutlinedIcon />

    const toggleHiddenHandler =
        value => toggleArticleVisibilityFunction(article.articleId, value, () => setHidden(value));
    const toggleReadLaterHandler =
        value => toggleReadLaterFunction(article.articleId, value, () => setReadLater(value));
    return (
        <Grid item key={article.articleId} style={itemStyle} xs={12}>
            <div style={article.isNew ? newArticleIndicatorStyle : { display: 'None' }} />
            <Grid container alignItems="center">
                <Grid item xs={6}>
                    <p style={publishedStyle}>{formatDate(article.publishedAt)}</p>
                </Grid>
                <Grid item container justifyContent="flex-end" alignItems="center" spacing={1} xs>
                    <Grid item>
                        <IconButton size="small" onClick={() => toggleReadLaterHandler(!readLater)}>
                            {readLaterIcon}
                        </IconButton>
                    </Grid>
                    <Grid item>
                        <Switch checked={hidden}
                                onChange={() => toggleHiddenHandler(!hidden)}
                                size="medium" />
                    </Grid>
                </Grid>
            </Grid>
            <div style={{ clear: 'both' }}>
                <a target="_blank"
                   href={article.link}
                   onClick={() => toggleHiddenHandler(true)}><p style={titleStyle}>{article.title}</p></a>
                <p>{article.description}</p>
                {images.map((image, i) => <img key={i} src={image.url} alt={image.title} width="100%" />)}
                <div>
                    {categories.map((category, i) =>
                        <Chip key={i}
                              label={category}
                              variant="default"
                              size="small"
                              style={category.toLowerCase() === 'ytring' ? chipStyleYtring : chipStyle} />)}
                </div>
            </div>
        </Grid>
    );
}

function formatDate(dateString) {
    const options = {
        weekday: 'long',
        month: 'long',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
    };
    return new Date(dateString).toLocaleString('no-nb', options);
}

export default ArticleList;
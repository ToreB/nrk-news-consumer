import { Button, Chip, FormControl, Grid, IconButton, InputLabel, MenuItem, Select, Switch } from "@material-ui/core";
import WatchLaterIcon from "@material-ui/icons/WatchLater";
import WatchLaterOutlinedIcon from "@material-ui/icons/WatchLaterOutlined";
import React, { useEffect, useLayoutEffect, useState } from 'react';

const ONE_HOUR = 60 * 60 * 1000;

export const Mode = {
    NON_HIDDEN: "non-hidden",
    HIDDEN: "hidden",
    DISEASE: "disease",
    UKRAINE_RUSSIA: "ukraine-russia",
    READ_LATER: "read-later"
};

const SortOrder = {
    ASC: "ASC",
    DESC: "DESC"
}

function resolvePath(apiContextPath, mode) {
    return `${apiContextPath}/articles/${mode !== Mode.NON_HIDDEN ? mode : ''}`;
}

function fetchArticles(apiContextPath, page, mode, sortOrder) {
    let path = resolvePath(apiContextPath, mode);

    return fetch(`${path}?size=12&page=${page}&sortOrder=${sortOrder}`)
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
    const [sortOrder, setSortOrder] = useState(mode === Mode.HIDDEN ? SortOrder.DESC : SortOrder.ASC)

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
        fetchArticles(apiContextPath, page, mode, sortOrder)
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
    };

    useEffect(() => {
        window.scrollTo(0, 0);
        loadArticles();

        const interval = setInterval(() => loadArticles(), ONE_HOUR);
        return () => clearInterval(interval)
    }, [page, sortOrder]);
    useLayoutEffect(() => {
        const newArticleIndicators = document.getElementsByClassName('newArticleIndicator');
        if (newArticleIndicators.length) {
            newArticleIndicators[0].scrollIntoView(true);
        }
    }, [articles]);

    const buttonStyle = {
        fontSize: '24px',
        marginRight: '30px'
    };

    const headerViewStyle = {
        display: 'block',
        border: '1px solid black',
        borderRadius: '5px',
        margin: '10px 10px 10px',
        padding: '0px 25px',
        maxWidth: '400px',
        fontWeight: 'bold'
    }

    const countViewStyle = {
        display: mode === Mode.HIDDEN ? 'none' : 'block',
    };

    const countToggledArticles = hiddenToggledArticles.length + readLaterToggledArticles.length;
    return (
        <div id="articles">
            <Grid container>
                <Grid item xs={12}>
                    <Grid container style={headerViewStyle}>
                        <Grid item style={countViewStyle}>
                            <p>Articles in this view: {totalCountArticles}</p>
                        </Grid>
                        <Grid item>
                            <FormControl fullWidth>
                                <InputLabel id="sort-order-select-label">Sort order: </InputLabel>
                                <Select labelId="sort-order-select-label"
                                        id="sort-order-select"
                                        value={sortOrder}
                                        label="Sort order"
                                        onChange={(event) => {
                                            setSortOrder(event.target.value);
                                            setPage(1);
                                        }}>
                                    <MenuItem value={SortOrder.ASC}>{SortOrder.ASC}</MenuItem>
                                    <MenuItem value={SortOrder.DESC}>{SortOrder.DESC}</MenuItem>
                                </Select>
                            </FormControl>
                        </Grid>
                    </Grid>
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
            <div className={article.isNew ? 'newArticleIndicator' : null}
                 style={article.isNew ? newArticleIndicatorStyle : { display: 'None' }} />
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
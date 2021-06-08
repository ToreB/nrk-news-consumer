import React, { useEffect, useState } from 'react';

function ArticleList() {
    let [articles, setArticles] = useState([]);

    useEffect(() => {
        fetch("http://localhost:8080/articles")
            .then(res => res.json())
            .then(resultJson => setArticles(resultJson));
    }, []);

    return (
        <div id="articles">
            {articles.map(article => <ArticleElement key={article.articleId} article={article} />)}
        </div>
    );
}

function ArticleElement({ article }) {
    let divStyle = {
        border: '1px solid black',
        borderRadius: '5px',
        margin: '10px 10px 10px',
        padding: '25px',
        maxWidth: '600px'
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
        <div style={divStyle}>
            <p style={publishedStyle}>{formatDate(article.publishedAt)}</p>
            <a target="_blank" href={article.link}><p style={titleStyle}>{article.title}</p></a>
            <p>{article.description}</p>
            {images.map((image, i) => <img key={i} src={image.url} alt={image.title} width="100%" />)}
        </div>
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
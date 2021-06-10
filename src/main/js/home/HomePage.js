import React from 'react';
import ArticleList from "./ArticleList";

function HomePage({ articlesUrl }) {
    return <ArticleList articlesUrl={articlesUrl} />
}

export default HomePage;
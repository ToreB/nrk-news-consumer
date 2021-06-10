import { Container } from "@material-ui/core";
import React from 'react';
import ArticleList from "./ArticleList";

function HomePage({ articlesUrl }) {
    return (
        <Container maxWidth={false} style={{ marginLeft: "unset" }}>
            <ArticleList articlesUrl={articlesUrl} />
        </Container>
    );
}

export default HomePage;
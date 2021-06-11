import { Container } from "@material-ui/core";
import React from 'react';
import ArticleList from "./ArticleList";

function HomePage({ baseUrl }) {
    return (
        <Container maxWidth={false} style={{ marginLeft: "unset" }}>
            <ArticleList baseUrl={baseUrl} />
        </Container>
    );
}

export default HomePage;
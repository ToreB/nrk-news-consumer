import { Container } from "@material-ui/core";
import React from 'react';
import ArticleList, { Mode } from "./ArticleList";

function HiddenArticlesPage({ baseUrl }) {
    return (
        <Container maxWidth={false} style={{ marginLeft: "unset" }}>
            <ArticleList baseUrl={baseUrl} mode={Mode.HIDDEN} />
        </Container>
    );
}

export default HiddenArticlesPage;
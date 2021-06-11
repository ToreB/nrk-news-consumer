import { Container } from "@material-ui/core";
import React from 'react';
import ArticleList, { Mode } from "./ArticleList";

function NonHiddenArticlesPage({ baseUrl }) {
    return (
        <Container maxWidth={false} style={{ marginLeft: "unset" }}>
            <ArticleList baseUrl={baseUrl} mode={Mode.NON_HIDDEN} />
        </Container>
    );
}

export default NonHiddenArticlesPage;
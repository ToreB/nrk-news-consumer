import { Container } from "@material-ui/core";
import React from 'react';
import ArticleList, { Mode } from "./ArticleList";

function NonHiddenArticlesPage({ apiContextPath }) {
    return (
        <Container maxWidth={false} style={{ marginLeft: "unset" }}>
            <ArticleList apiContextPath={apiContextPath} mode={Mode.NON_HIDDEN} />
        </Container>
    );
}

export default NonHiddenArticlesPage;
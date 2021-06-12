import { Container } from "@material-ui/core";
import React from 'react';
import ArticleList, { Mode } from "./ArticleList";

function HiddenArticlesPage({ apiContextPath }) {
    return (
        <Container maxWidth={false} style={{ marginLeft: "unset" }}>
            <ArticleList apiContextPath={apiContextPath} mode={Mode.HIDDEN} />
        </Container>
    );
}

export default HiddenArticlesPage;
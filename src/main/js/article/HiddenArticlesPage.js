import { Container } from "@material-ui/core";
import React from 'react';
import ArticleList, { Mode } from "./ArticleList";

function HiddenArticlesPage({ apiContextPath }) {
    let containerStyle = {
        marginLeft: "unset",
        marginRight: "unset",
        paddingLeft: "unset",
        paddingRight: "unset"
    };
    return (
        <Container maxWidth={false} style={containerStyle}>
            <ArticleList apiContextPath={apiContextPath} mode={Mode.HIDDEN} />
        </Container>
    );
}

export default HiddenArticlesPage;
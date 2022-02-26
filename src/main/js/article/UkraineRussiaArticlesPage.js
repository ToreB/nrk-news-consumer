import { Container } from "@material-ui/core";
import React from 'react';
import ArticleList, { Mode } from "./ArticleList";

function UkraineRussiaArticlesPage({ apiContextPath }) {
    let containerStyle = {
        marginLeft: "unset",
        marginRight: "unset",
        paddingLeft: "unset",
        paddingRight: "unset"
    };
    return (
        <Container maxWidth={false} style={containerStyle}>
            <ArticleList apiContextPath={apiContextPath} mode={Mode.UKRAINE_RUSSIA} />
        </Container>
    );
}

export default UkraineRussiaArticlesPage;
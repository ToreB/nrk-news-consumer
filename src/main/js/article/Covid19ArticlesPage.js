import { Container } from "@material-ui/core";
import React from 'react';
import ArticleList, { Mode } from "./ArticleList";

function Covid19ArticlesPage({ apiContextPath }) {
    let containerStyle = {
        marginLeft: "unset",
        marginRight: "unset",
        paddingLeft: "unset",
        paddingRight: "unset"
    };
    return (
        <Container maxWidth={false} style={containerStyle}>
            <ArticleList apiContextPath={apiContextPath} mode={Mode.COVID_19} />
        </Container>
    );
}

export default Covid19ArticlesPage;
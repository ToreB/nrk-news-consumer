import { Container } from "@material-ui/core";
import { blue, red } from "@material-ui/core/colors";
import createTheme from "@material-ui/core/styles/createTheme";
import ThemeProvider from "@material-ui/styles/ThemeProvider";
import React from 'react';
import { Route, Routes } from "react-router-dom";
import ArticleList, { Mode } from "./article/ArticleList";
import Footer from './common/Footer';
import Header from "./common/Header";
import Navigation from "./common/Navigation";
import InfoPage from "./info/InfoPage";

function ArticlePage({ apiContextPath, mode }) {
    let containerStyle = {
        marginLeft: "unset",
        marginRight: "unset",
        paddingLeft: "unset",
        paddingRight: "unset"
    };
    return (
        <Container maxWidth={false} style={containerStyle}>
            <ArticleList apiContextPath={apiContextPath} mode={mode} key={mode} />
        </Container>
    );
}

function App({ buildProperties, apiContextPath }) {

    const theme = createTheme({
        palette: {
            primary: {
                main: blue[500]
            },
            secondary: {
                main: red[500]
            }
        }
    });

    return (
        <ThemeProvider theme={theme}>
            <Navigation>
                <Header appName={buildProperties.name} />

                <main>
                    <Routes>
                        <Route path="/info"
                               element={<InfoPage buildProperties={buildProperties} />} />
                        <Route path="/hidden-articles"
                               element={<ArticlePage apiContextPath={apiContextPath} mode={Mode.HIDDEN} />} />
                        <Route path="/read-later"
                               element={<ArticlePage apiContextPath={apiContextPath} mode={Mode.READ_LATER} />} />
                        <Route path="/covid-19"
                               element={<ArticlePage apiContextPath={apiContextPath} mode={Mode.COVID_19} />} />
                        <Route path="/ukraine-russia"
                               element={<ArticlePage apiContextPath={apiContextPath} mode={Mode.UKRAINE_RUSSIA} />} />
                        <Route path="/"
                               element={<ArticlePage apiContextPath={apiContextPath} mode={Mode.NON_HIDDEN} />} />
                    </Routes>
                </main>

                <Footer buildProperties={buildProperties} />
            </Navigation>
        </ThemeProvider>
    )
        ;
}

export default App;
import { Container } from "@material-ui/core";
import { blue, red } from "@material-ui/core/colors";
import createTheme from "@material-ui/core/styles/createTheme";
import ThemeProvider from "@material-ui/styles/ThemeProvider";
import React from 'react';
import { Route, Switch } from "react-router-dom";
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
                    <Switch>
                        <Route path="/info">
                            <InfoPage buildProperties={buildProperties} />
                        </Route>
                        <Route path="/hidden-articles">
                            <ArticlePage apiContextPath={apiContextPath} mode={Mode.HIDDEN} />
                        </Route>
                        <Route path="/read-later">
                            <ArticlePage apiContextPath={apiContextPath} mode={Mode.READ_LATER} />
                        </Route>
                        <Route path="/covid-19">
                            <ArticlePage apiContextPath={apiContextPath} mode={Mode.COVID_19} />
                        </Route>
                        <Route path="/ukraine-russia">
                            <ArticlePage apiContextPath={apiContextPath} mode={Mode.UKRAINE_RUSSIA} />
                        </Route>
                        <Route path="/">
                            <ArticlePage apiContextPath={apiContextPath} mode={Mode.NON_HIDDEN} />
                        </Route>
                    </Switch>
                </main>

                <Footer buildProperties={buildProperties} />
            </Navigation>
        </ThemeProvider>
    );
}

export default App;
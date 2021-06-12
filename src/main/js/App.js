import { blue, red } from "@material-ui/core/colors";
import createMuiTheme from "@material-ui/core/styles/createMuiTheme";
import ThemeProvider from "@material-ui/styles/ThemeProvider";
import React from 'react';
import { Route, Switch } from "react-router-dom";
import HiddenArticlesPage from "./article/HiddenArticlesPage";
import NonHiddenArticlesPage from "./article/NonHiddenArticlesPage";
import Footer from './common/Footer';
import Header from "./common/Header";
import Navigation from "./common/Navigation";
import InfoPage from "./info/InfoPage";

function App({ buildProperties, apiContextPath }) {

    const theme = createMuiTheme({
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
                            <HiddenArticlesPage apiContextPath={apiContextPath} />
                        </Route>
                        <Route path="/">
                            <NonHiddenArticlesPage apiContextPath={apiContextPath} />
                        </Route>
                    </Switch>
                </main>

                <Footer buildProperties={buildProperties} />
            </Navigation>
        </ThemeProvider>
    );
}

export default App;
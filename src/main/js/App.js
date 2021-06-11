import { blue, red } from "@material-ui/core/colors";
import createMuiTheme from "@material-ui/core/styles/createMuiTheme";
import ThemeProvider from "@material-ui/styles/ThemeProvider";
import React from 'react';
import { Route, Switch } from "react-router-dom";
import Footer from './common/Footer';
import Header from "./common/Header";
import Navigation from "./common/Navigation";
import HomePage from "./home/HomePage";
import InfoPage from "./info/InfoPage";

function App({ buildProperties, baseUrl }) {

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
                        <Route path="/">
                            <HomePage baseUrl={baseUrl}/>
                        </Route>
                    </Switch>
                </main>

                <Footer buildProperties={buildProperties} />
            </Navigation>
        </ThemeProvider>
    );
}

export default App;
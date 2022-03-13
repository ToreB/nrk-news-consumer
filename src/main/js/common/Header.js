import { Button, Toolbar, Typography } from "@material-ui/core";
import AppBar from "@material-ui/core/AppBar";
import makeStyles from "@material-ui/core/styles/makeStyles";
import React from 'react';
import { Link } from "react-router-dom";

const useStyles = makeStyles({
    root: {
        flexGrow: 1,
    },
    title: {
        flexGrow: 1
    }
});

function Header({ appName }) {

    const classes = useStyles();

    return (
        <header className={classes.root}>
            <AppBar position="static">
                <Toolbar>
                    <Typography className={classes.title} variant="h6">{appName}</Typography>

                    <nav>
                        <Button color="inherit" component={Link} to="/">Articles</Button>
                        <Button color="inherit" component={Link} to="/covid-19">Covid-19</Button>
                        <Button color="inherit" component={Link} to="/ukraine-russia">Ukraine & Russia</Button>
                        <Button color="inherit" component={Link} to="/read-later">Read later</Button>
                        <Button color="inherit" component={Link} to="/hidden-articles">Hidden articles</Button>
                        <Button color="inherit" component={Link} to="/info">Info</Button>
                    </nav>
                </Toolbar>
            </AppBar>
        </header>
    );
}

export default Header;
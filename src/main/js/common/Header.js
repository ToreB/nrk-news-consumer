import { Button, Toolbar, Typography } from "@material-ui/core";
import AppBar from "@material-ui/core/AppBar";
import makeStyles from "@material-ui/core/styles/makeStyles";
import React from 'react';
import { Link } from "react-router-dom";

const useStyles = makeStyles({
    root: {
        flexGrow: 1
    },
    title: {
        flexGrow: 1,
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
                        <Button color="inherit" component={Link} to="/">Home</Button>
                        <Button color="inherit" component={Link} to="/info">Info</Button>
                    </nav>
                </Toolbar>
            </AppBar>
        </header>
    );
}

export default Header;
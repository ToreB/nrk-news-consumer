import { Grid } from "@material-ui/core";
import AppBar from "@material-ui/core/AppBar";
import makeStyles from "@material-ui/core/styles/makeStyles";
import Toolbar from "@material-ui/core/Toolbar";
import PropTypes from 'prop-types'
import React from 'react'
import { BoldValueKeyValueTextElement } from "./KeyValueTextElement";

const useStyles = makeStyles({
    footer: {
    }
});

function Footer({ buildProperties }) {
    const classes = useStyles();
    return (
        <footer className={classes.footer}>
            <AppBar position="static">
                <Toolbar>
                    <Grid container spacing={1}>
                        <Grid item>
                            <BoldValueKeyValueTextElement keyName={"Application version"}
                                                          value={buildProperties.version} />
                        </Grid>
                        <Grid item>
                            <BoldValueKeyValueTextElement keyName={"Build time"}
                                                          value={buildProperties.time} />
                        </Grid>
                    </Grid>
                </Toolbar>
            </AppBar>
        </footer>
    );
}

Footer.propTypes = {
    buildProperties: PropTypes.shape({
        version: PropTypes.string,
        time: PropTypes.string
    })
}

export default Footer;
import React from 'react';
import { BrowserRouter } from "react-router-dom";

function Navigation({ children }) {
    return (
        <BrowserRouter>
            {children}
        </BrowserRouter>
    );
}

export default Navigation;
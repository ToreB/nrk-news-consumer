import React from 'react'
import { createRoot } from 'react-dom/client'
import App from './App'

export function initializeApp(elementId, props) {
    const element = document.getElementById(elementId);
    const root = createRoot(element);
    root.render(<App {...props} />);
}
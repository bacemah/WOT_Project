import * as bootstrap from 'bootstrap'
import "./routes.js"

(()=> {
    'use strict'

    window.onload = () => {
        if ('serviceWorker' in navigator) {
            navigator.serviceWorker.register('./sw.js').then(function (registration) {
                // Service worker registered correctly.
                console.log('ServiceWorker registration successful with scope:', registration.scope)
            }).catch(function (err) {
                // Troubles in registering the service worker. :(
                console.log('Serviceworker registration failed:', err)
            });
        }
    }
    document.addEventListener('DOMContentLoaded', () =>{
        [...document.querySelectorAll('[data-bs-toggle="tooltip"]')].map(el => new bootstrap.Tooltip(el));
        [...document.querySelectorAll('[data-bs-toggle="popover"]')].map(el => new bootstrap.Popover(el));

    });
})();
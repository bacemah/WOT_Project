import * as bootstrap from 'bootstrap';
import VanillaRouter from "./router.js";
import {checkSession, registerPKCEClickListener, handlePKCERedirect, registrationListener} from './iam.js';


(()=>{
    'use strict'

    window.onerror = (msg, url, line, col, error) => {
       var extra = !col ? '' : '\ncolumn: ' + col;
       extra += !error ? '' : '\nerror: ' + error;
       console.log("Error: " + msg + "\nurl: " + url + "\nline: " + line + extra);
       // TODO: Report this error via ajax so you can keep track
       //       of what pages have JS issues
       // If you return true, then error alerts (like in older versions of Internet Explorer) will be suppressed.
       return true;
    };

    document.querySelectorAll('[data-bs-toggle="popover"]')
       .forEach(popover => {
          new bootstrap.Popover(popover)
       })

    const tooltipTriggerList = document.querySelectorAll('[data-bs-toggle="tooltip"]')
    const tooltipList = [...tooltipTriggerList].map(tooltipTriggerEl => new bootstrap.Tooltip(tooltipTriggerEl))

    const getStoredTheme = () => localStorage.getItem('theme')
    const setStoredTheme = theme => localStorage.setItem('theme', theme)
    const getPreferredTheme = () => {
       const storedTheme = getStoredTheme()
       if (storedTheme) {
          return storedTheme
       }
       return window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light'
    }
    const setTheme = theme => {
       if (theme === 'auto') {
          document.documentElement.setAttribute('data-bs-theme', (window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light'))
       } else {
          document.documentElement.setAttribute('data-bs-theme', theme)
       }
    }
    setTheme(getPreferredTheme())
    const showActiveTheme = (theme, focus = false) => {
       const themeSwitcher = document.querySelector('#bd-theme')
       if (!themeSwitcher) {
          return
       }
       const themeSwitcherText = document.querySelector('#bd-theme-text')
       const activeThemeIcon = document.querySelector('.theme-icon-active use')
       const btnToActive = document.querySelector(`[data-bs-theme-value="${theme}"]`)
       const svgOfActiveBtn = btnToActive.querySelector('svg use').getAttribute('href')
       document.querySelectorAll('[data-bs-theme-value]').forEach(element => {
          element.classList.remove('active')
          element.setAttribute('aria-pressed', 'false')
       })
       btnToActive.classList.add('active')
       btnToActive.setAttribute('aria-pressed', 'true')
       activeThemeIcon.setAttribute('href', svgOfActiveBtn)
       const themeSwitcherLabel = `${themeSwitcherText.textContent} (${btnToActive.dataset.bsThemeValue})`
       themeSwitcher.setAttribute('aria-label', themeSwitcherLabel)
       if (focus) {
          themeSwitcher.focus()
       }
    }
    window.matchMedia('(prefers-color-scheme: dark)').addEventListener('change', () => {
       const storedTheme = getStoredTheme()
       if (storedTheme !== 'light' && storedTheme !== 'dark') {
          setTheme(getPreferredTheme())
       }
    })


    window.addEventListener('DOMContentLoaded', async () => {
       showActiveTheme(getPreferredTheme())
       document.querySelectorAll('[data-bs-theme-value]')
          .forEach(toggle => {
             toggle.addEventListener('click', () => {
                const theme = toggle.getAttribute('data-bs-theme-value')
                setStoredTheme(theme)
                setTheme(theme)
                showActiveTheme(theme, true)
             })
          })
       registrationListener()
       registerPKCEClickListener();
    })

    let presenters = new Map();

    document.addEventListener('signIn',(evt)=>{
       let accessToken = sessionStorage.getItem('accessToken');
       if (accessToken === null) {
          sessionStorage.setItem('accessToken',evt.detail.access_token);
       }
       document.getElementById("signin").classList.add("d-none");
       document.getElementById("signup").classList.add("d-none");
       document.getElementById("signout").classList.remove("d-none");
       const router = new VanillaRouter({
          type: 'history',
          routes: {
             "/": "welcome",
             "/dashboard": "dashboard",
             "/about":"about"
          }
       }).listen().on('route', async e => {
          if(checkSession()){
             console.log(e.detail.route, e.detail.url);
             console.log("%c "+e.detail.url.search,"color:red;fontWeight:bold");
             const mainContent = document.getElementById('mainContent');
             mainContent.innerHTML = await fetch('./pages/' + e.detail.route + '.html').then(response=>response.text())
             const subjectContent = document.getElementById('profile');
             let subject = sessionStorage.getItem('subject');
             subjectContent.innerHTML =`Welcome ${subject}`;
             document.title = mainContent.getElementsByTagName('title')[0].innerHTML;
             if(presenters.has(e.detail.route)){
                let cachedPresenter = presenters.get(e.detail.route);
                cachedPresenter.load();
             }
             let presenter = null;
             switch(e.detail.route){
                case 'welcome':
                   presenter = new HomePresenter();
                   break;
                case 'countries':
                   //presenter = new CountryPresenter();
                   break;
                default:
                   alert(e.detail.route);
             }
             presenters.set(e.detail.route,presenter)
          }else{
             window.location.reload(true);
          }
       });
    });
    document.addEventListener('signOut', () => {
       // Clear session storage
       sessionStorage.removeItem('accessToken');
       sessionStorage.removeItem('subject');
       sessionStorage.removeItem('oauth-session');

       // Reset UI
       document.getElementById("signin").classList.remove("d-none");
       document.getElementById("signup").classList.remove("d-none");
       document.getElementById("signout").classList.add("d-none");

       // Redirect to the specified location
       window.location.href = "https://smarthydro.lme:8443";
    });

// Add a click listener to the signout button
    document.getElementById('signout').addEventListener('click', () => {
       const signOutEvent = new CustomEvent('signOut');
       document.dispatchEvent(signOutEvent);
    });


    if(!checkSession()){

       let mainContent = document.getElementById('mainContent');
       mainContent.innerHTML = '';
       mainContent.appendChild(document.getElementById('welcome-content').content.cloneNode(true));
       handlePKCERedirect();
       const router = new VanillaRouter({
          type: 'history',
          routes: {
             "/": "welcome",
             "/about":"about"
          }
       }).listen().on("route", async e =>{
          const mainContent = document.getElementById('mainContent');
          mainContent.innerHTML = await fetch('./pages/' + e.detail.route + '.html').then(response=>response.text())
       })

    }else{
       const signInEvent = new CustomEvent("signIn", { detail: sessionStorage.getItem('oauth-session') });
       document.dispatchEvent(signInEvent);
    }

    if ("serviceWorker" in navigator) {
       window.addEventListener("load", function () {
          navigator.serviceWorker
             .register("/ServiceWorker.js")
             .then(res => console.log("service worker registered"))
             .catch(err => console.log("service worker not registered", err));
       });
    }

})()
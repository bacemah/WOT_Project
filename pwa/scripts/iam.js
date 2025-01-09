import {switchToSubdomain} from './utils.js';

var config = {
    client_id: "flamesensepwa",
    redirect_uri: "https://flamesense.lme:8443/",
    authorization_endpoint: "https://iam.flamesense.lme:8443/rest-iam/authorize",
    token_endpoint: "https://iam.flamesense.lme:8443/rest-iam/oauth/token",
    requested_scopes: "resource.read resource.write",
    registration_endpoint: "https://iam.flamesense.lme:8443/rest-iam/register/authorize"
};

function parseJwt(token){
    var base64Url = token.split('.')[1];
    var base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    var jsonPayload = decodeURIComponent(atob(base64).split('').map(function(c) {
        return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
    }).join(''));
    return JSON.parse(jsonPayload);
}

export function checkSession(){
    let accessToken = sessionStorage.getItem('accessToken');
    if(accessToken !== null){
        let payload = parseJwt(accessToken);
        if(payload.exp < Math.round(Date.now() / 1000)){
            sessionStorage.removeItem('accessToken');
            sessionStorage.removeItem('subject');
            sessionStorage.removeItem('groups');
            return false;
        }
        sessionStorage.setItem('subject',payload.sub);
        sessionStorage.setItem('groups',payload.groups);
        return true;
    }
    return false;
}
//////////////////////////////////////////////////////////////////////
// REGISTRATION REQUEST
export function registrationListener(){
    document.getElementById("signup").addEventListener("click", async function(e){
        e.preventDefault();
        var url = config.registration_endpoint
            + "?client_id="+encodeURIComponent(config.client_id)
            + "&redirect_uri="+encodeURIComponent(config.redirect_uri)
        ;
        window.location = url;
    });
}

//////////////////////////////////////////////////////////////////////
// OAUTH REQUEST
export function registerPKCEClickListener(){
    document.getElementById("signin").addEventListener("click", async function(e){
        e.preventDefault();
        var state = generateRandomString();
        localStorage.setItem("pkce_state", state);
        var code_verifier = generateRandomString();
        localStorage.setItem("pkce_code_verifier", code_verifier);
        var code_challenge = await pkceChallengeFromVerifier(code_verifier);
        var url = config.authorization_endpoint
            + "?response_type=code"
            + "&client_id="+encodeURIComponent(config.client_id)
            + "&state="+encodeURIComponent(state)
            + "&scope="+encodeURIComponent(config.requested_scopes)
            + "&redirect_uri="+encodeURIComponent(config.redirect_uri)
            + "&code_challenge="+encodeURIComponent(code_challenge)
            + "&code_challenge_method=S256"
        ;
        window.location = url;
    });
}

//////////////////////////////////////////////////////////////////////
// GENERAL HELPER FUNCTIONS
function sendPostRequest(url, params, success, error) {
    var request = new XMLHttpRequest();
    request.open('POST', url, true);
    request.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded; charset=UTF-8');
    request.onload = function() {
        var body = {};
        try {
            body = JSON.parse(request.response);
        } catch(e) {}

        if(request.status == 200) {
            success(request, body);
        } else {
            error(request, body);
        }
    }
    request.onerror = function() {
        error(request, {});
    }
    var body = Object.keys(params).map(key => key + '=' + params[key]).join('&');
    request.send(body);
}

function parseQueryString(string) {
    if(string == "") { return {}; }
    var segments = string.split("&").map(s => s.split("=") );
    var queryString = {};
    segments.forEach(s => queryString[s[0]] = s[1]);
    return queryString;
}

export function handlePKCERedirect(){
    //////////////////////////////////////////////////////////////////////
    // OAUTH REDIRECT HANDLING
    document.getElementById("signin").classList.remove("d-none");
    document.getElementById("signout").classList.add("d-none");
    let q = parseQueryString(window.location.search.substring(1));
    if(q.error) {
        alert("Error returned from authorization server: "+q.error);
        console.log(error.error+": "+error.error_description);
    }
    if(q.code) {
        // Verify state matches what we set at the beginning
        if(localStorage.getItem("pkce_state") != q.state) {
            alert("Invalid state");
        } else {
            sendPostRequest(config.token_endpoint, {
                grant_type: "authorization_code",
                code: q.code,
                client_id: config.client_id,
                redirect_uri: config.redirect_uri,
                code_verifier: localStorage.getItem("pkce_code_verifier")
            }, function(request, body) {
                const signInEvent = new CustomEvent("signIn", { detail: body });
                document.dispatchEvent(signInEvent);
                window.history.replaceState({}, null, "/");
            }, function(request, error) {
                console.log(error.error+": "+error.error_description);
            });
        }
        localStorage.removeItem("pkce_state");
        localStorage.removeItem("pkce_code_verifier");
    }
}
//////////////////////////////////////////////////////////////////////
// PKCE HELPER FUNCTIONS

function generateRandomString() {
    var array = new Uint32Array(28);
    window.crypto.getRandomValues(array);
    return Array.from(array, dec => ('0' + dec.toString(16)).substr(-2)).join('');
}

function sha256(plain) {
    const encoder = new TextEncoder();
    const data = encoder.encode(plain);
    return window.crypto.subtle.digest('SHA-256', data);
}

function base64urlencode(str) {
    return btoa(String.fromCharCode.apply(null, new Uint8Array(str)))
        .replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/, '');
}

async function pkceChallengeFromVerifier(v) {
    let hashed = await sha256(v);
    return base64urlencode(hashed);
}

async function generateRandomString(length) {
    const array = new Uint32Array(length);
    window.crypto.getRandomValues(array);
    return Array.from(array, (dec) => ("0" + dec.toString(16)).substr(-2)).join("");
}

async function sha256(plain) {
    const encoder = new TextEncoder();
    const data = encoder.encode(plain);
    return window.crypto.subtle.digest("SHA-256", data);
}

function base64urlencode(str) {
    return btoa(String.fromCharCode.apply(null, new Uint8Array(str)))
        .replace(/\+/g, "-")
        .replace(/\//g, "_")
        .replace(/=+$/, "");
}

async function pkceChallengeFromVerifier(v) {
    const hashed = await sha256(v);
    return base64urlencode(hashed);
}

function utf8_to_b64(str) {
    return window.btoa(unescape(encodeURIComponent(str)));
}
async function start() {
    var state = await generateRandomString(28);
    var code_verifier = await generateRandomString(28);
    var code_challenge = await pkceChallengeFromVerifier(code_verifier);

    console.log("state:", state);
    console.log("code_verifier:", code_verifier);
    localStorage.setItem("codeverif", code_verifier);
    console.log("code_challenge:", code_challenge);

    var step = utf8_to_b64(state + "#" + code_challenge);
    console.log("step:", step);

    var step2 = "Bearer " + step;
    console.log(step2)
    $.ajax({
        url: "https://127.0.0.1:8080/rest-api/authorize",
        type: "POST",
        headers: {
            Accept: "application/json",
            "Content-Type": "application/json",
            "Pre-Authorization": step2,
        },
        complete: function (data) {
            console.log("Authorization request completed.");
            console.log("data.responseJSON:", data.responseJSON);

            // Scenario: Successful Authorization Request
            if (data.status === 302) {
                console.log("Authorization request successful.");
                localStorage.setItem("signInId", data.responseJSON.signInId);
            }
        }
    })
}



document.addEventListener('DOMContentLoaded',start)

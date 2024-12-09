
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
    var clientId = await generateRandomString(28);

    var code_verifier = await generateRandomString(28);

    var code_challenge = await pkceChallengeFromVerifier(code_verifier);

    var step = utf8_to_b64(clientId + "#" + code_challenge);

    var step2 = "Bearer " + step;

    var response =  await fetch(
        "http://127.0.0.1:8080/rest-api/authorize",
        {
            method: "GET",
            headers : {
                "Content-Type": "application/json",
                "Pre-Authorization": step2
            }
        }
    )
    let mainContent = document.getElementById("mainContent")
    mainContent.innerHTML = await response.text()
    var email = ""
    var password =""

    setTimeout(async () => {
        let emailInput = document.getElementById("email")
        let passwordInput = document.getElementById("password")
        emailInput.addEventListener("change", (e) => {
            email = e.currentTarget.value
        })
        passwordInput.addEventListener("change", (e) => {
            password = e.currentTarget.value
        })

        const data =  {
                        username : email ,
                        password : password
        }

        var signInResponse = await fetch(
            "rest-api/login/authorization" ,
            {
                    method : "POST" ,
                    body : JSON.stringify(data)
            }
        )


    },3000)
}




setTimeout(()=>{
    let signInButton = document.getElementById("signInButt")
    signInButton.addEventListener("click", start)
},2000)




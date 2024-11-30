import VanillaRouter from "./router.js";

const router = new VanillaRouter({
    type : "history" ,
    routes : {
        "/" : "SignIn" ,
        "/Signup" : "SignUp"
    }

}).listen().on("route" , async e => {
    let mainElement = document.getElementById("mainContent");
    let text = await fetch("pages/" + e.detail.route + ".html").then(x => x.text()).catch(e => console.log(e))
    mainElement.innerHTML= text
})
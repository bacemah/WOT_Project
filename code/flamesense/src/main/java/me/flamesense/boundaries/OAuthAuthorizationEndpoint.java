package me.flamesense.boundaries;

import jakarta.json.Json;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import jakarta.ws.rs.core.NewCookie;
import me.flamesense.utils.CCCipher;

import java.io.UnsupportedEncodingException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Path("/")
public class OAuthAuthorizationEndpoint {

    public final static String XSS_COOKIE_NAME = "xssCookie" ;

    @Context
    private UriInfo uriInfo;

    @GET
    @Path("/authorize")
    public Response authorize(@HeaderParam("Pre-Authorization") String authorization ) throws UnsupportedEncodingException {
        var bytes=Base64.getDecoder().decode(authorization.substring("Bearer ".length()));
        var decoded = new String(bytes,"ISO-8859-1");
        var credentials = decoded.split("#");
        var codeChallenge = credentials[1];
        var secureCookie = new NewCookie.Builder(XSS_COOKIE_NAME)
                .httpOnly(true)
                .secure(true)
                .sameSite(NewCookie.SameSite.STRICT)
                .domain(uriInfo.getRequestUri().getHost())
                .expiry(Date.from(Instant.now().plus(17, ChronoUnit.MINUTES)))
                .value(CCCipher.encrypt(codeChallenge))
                .build();

        StreamingOutput stream = (output)->{
            try(var resourceStream = getClass().getResourceAsStream("/html/SignIn.html")){
                assert resourceStream != null;
                output.write(resourceStream.readAllBytes());
            }
        };
        return Response.ok(stream).cookie(secureCookie).build();
    }
    @POST
    @Path("/login/authorization")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response login(@FormParam("username")String username, @FormParam("password")String password,@CookieParam(XSS_COOKIE_NAME)Cookie xssTSCookie) {
        System.out.println(username);
        var cookieValue = xssTSCookie.getValue();
        //"include cookiValue in authorisation code";
        return Response.ok(Json.createObjectBuilder()
                    .add("code" , 200)
                    .add("message" , cookieValue)
                    .build()).build();

    }




}

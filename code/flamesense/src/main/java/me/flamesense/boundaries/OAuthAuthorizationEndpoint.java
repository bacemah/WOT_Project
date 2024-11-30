package me.flamesense.boundaries;

import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import java.io.UnsupportedEncodingException;
import java.util.*;

@Path("/")
public class OAuthAuthorizationEndpoint {

    public final static String XSS_COOKIE_NAME = "xssCookie" ;

    @Context
    private UriInfo uriInfo;

    @POST
    @Path("/authorize")
    public Response authorize(@HeaderParam("Pre-Authorization") String authorization) throws UnsupportedEncodingException {
        byte[] bytes=Base64.getDecoder().decode(authorization.substring("Bearer ".length()));
        String decoded = new String(bytes,"ISO-8859-1")
        String[] credentials = decoded.split("#");
        NewCookie cookie = new NewCookie(XSS_COOKIE_NAME, //generate the cookie for security
                oAuth2PKCE.generateXSSToken(credentials[0],uriInfo.getBaseUri().getPath()),
                uriInfo.getBaseUri().getPath(),
                uriInfo.getBaseUri().getHost(),"Secure Http Only Cookie",86400,true,true);
        return Response .status(Response.Status.FOUND)
                .cookie(cookie)
                .entity("{\"signInId\":\""+oAuth2PKCE.addChallenge(credentials[1],credentials[0])+ //Return SignInId
                        "\"}").build();
    }
}

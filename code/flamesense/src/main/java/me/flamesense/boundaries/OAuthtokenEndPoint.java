package me.flamesense.boundaries;

import jakarta.json.Json;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;
import jakarta.ejb.EJB;

import me.flamesense.security.JwtManager;
@Path("/auth")
public class OAuthtokenEndPoint {

    @EJB
    private JwtManager jwtManager;


    @Path("/token")
    @GET
    public Response generateToken(@QueryParam("authorization_code") String authorizationCode,
                                  @QueryParam("code_verifier")String codeVerifier){
        //validate authorization code and code verifier according to OAuth 2.0 Authorization Flow with PKCE
        //Extract tenantId and subjectId from authorization code
        //Extract subject, approvedScopes and roles from database using tenantId and subjectId
        var tenantId = "watermarking123";
        var subject = "john.doe";
        var approvedScopes = "resource:read,resource:write";
        var roles = new String[]{"Moderator","Client"};
        var token = jwtManager.generateToken(tenantId,subject,approvedScopes,roles);
        return Response
                .ok(Json.createObjectBuilder()
                        .add("access-token", token)
                        .add("token_type", "Bearer")
                        .add("expires_in", 1020)
                        .build())
                .build();
    }
}

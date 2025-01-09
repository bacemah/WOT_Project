package tn.cot.smarthydro.bounadaries;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import tn.cot.smarthydro.entities.Identity;
import tn.cot.smarthydro.repositories.IdentityRepository;
import tn.cot.smarthydro.repositories.TenantRepository;
import tn.cot.smarthydro.security.AuthorizationCode;
import tn.cot.smarthydro.security.Argon2Utils;

import java.io.InputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Objects;


@Path("/")
@RequestScoped
public class OAuthAuthorizationEndpoint {

    public static final String CHALLENGE_RESPONSE_COOKIE_ID = "signInId";
    @Inject
    Argon2Utils argon2Utils;
    @Inject
    TenantRepository tenantRepository;
    @Inject
    IdentityRepository identityRepository;

    @GET
    @Path("/authorize")
    @Produces(MediaType.TEXT_HTML)
    public Response authorize(@Context UriInfo uriInfo) {
        var params = uriInfo.getQueryParameters();
        var clientId = params.getFirst("client_id");
        if (clientId == null || clientId.isEmpty()) {
            return informUserAboutError("Invalid client_id :" + clientId);
        }
        var tenant = tenantRepository.findByName(clientId);
        if (tenant == null) {
            return informUserAboutError("Invalid client_id :" + clientId);
        }
        if (tenant.getSupportedGrantTypes() != null && !tenant.getSupportedGrantTypes().contains("authorization_code")) {
            return informUserAboutError("Authorization Grant type, authorization_code, is not allowed for this tenant :" + clientId);
        }
        String redirectUri = params.getFirst("redirect_uri");
        if (tenant.getRedirectUri() != null && !tenant.getRedirectUri().isEmpty()) {
            if (redirectUri != null && !redirectUri.isEmpty() && !tenant.getRedirectUri().equals(redirectUri)) {
                return informUserAboutError("redirect_uri is pre-registred and should match");
            }
            redirectUri = tenant.getRedirectUri();
        } else {
            if (redirectUri == null || redirectUri.isEmpty()) {
                return informUserAboutError("redirect_uri is not pre-registred and should be provided");
            }
        }
        String responseType = params.getFirst("response_type");
        if (!"code".equals(responseType) && !"token".equals(responseType)) {
            String error = "invalid_grant :" + responseType + ", response_type params should be code or token:";
            return informUserAboutError(error);
        }
        String requestedScope = params.getFirst("scope");
        if (requestedScope == null || requestedScope.isEmpty()) {
            requestedScope = tenant.getRequiredScopes();
        }
        String codeChallengeMethod = params.getFirst("code_challenge_method");
        if(codeChallengeMethod==null || !codeChallengeMethod.equals("S256")){
            String error = "invalid_grant :" + codeChallengeMethod + ", code_challenge_method must be 'S256'";
            return informUserAboutError(error);
        }
        StreamingOutput stream = output -> {
            try (InputStream is = Objects.requireNonNull(getClass().getResource("/login.html")).openStream()){
                output.write(is.readAllBytes());
            }
        };
        return Response.ok(stream).location(uriInfo.getBaseUri().resolve("/login/authorization"))
                .cookie(new NewCookie.Builder(CHALLENGE_RESPONSE_COOKIE_ID)
                        .httpOnly(true)
                        .secure(true)
                        .sameSite(NewCookie.SameSite.STRICT)
                        .value(tenant.getName()+"#"+requestedScope+"$"+redirectUri)
                        .expiry(Date.from(Instant.now().plus(17, ChronoUnit.MINUTES)))
                        .build())
                .build();
    }

    @POST
    @Path("/login/authorization")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_HTML)
    public Response login(@CookieParam(CHALLENGE_RESPONSE_COOKIE_ID) Cookie cookie,
                          @FormParam("username")String username,
                          @FormParam("password")String password,
                          @Context UriInfo uriInfo) throws Exception {
        Identity identity;
        try{
            identity = identityRepository.findByUsername(username).get();
        }
        catch (Exception e){
            return informUserAboutError("Invalid username :" + username);
        }
        if (argon2Utils.check(identity.getPassword(), password.toCharArray())) {
            MultivaluedMap<String, String> params = uriInfo.getQueryParameters();
            String redirectURI = buildActualRedirectURI(
                    cookie.getValue().split("\\$")[1],
                    params.getFirst("response_type"),
                    cookie.getValue().split("#")[0],
                    username,
                    "resource.read,resource.write",
                    params.getFirst("code_challenge"), params.getFirst("state")
            );
            return Response.seeOther(UriBuilder.fromUri(redirectURI).build()).build();
        } else {
            URI location = UriBuilder.fromUri(cookie.getValue().split("\\$")[1])
                    .queryParam("error", "User doesn't approved the request.")
                    .queryParam("error_description", "User doesn't approved the request.")
                    .build();
            return Response.seeOther(location).build();
        }
    }

    private String buildActualRedirectURI(String redirectUri,String responseType,String clientId,String userId,String approvedScopes,String codeChallenge,String state) throws Exception {
        StringBuilder sb = new StringBuilder(redirectUri);
        if ("code".equals(responseType)) {
            AuthorizationCode authorizationCode = new AuthorizationCode(clientId,userId,
                    approvedScopes, Instant.now().plus(2, ChronoUnit.MINUTES).getEpochSecond(),redirectUri);
            sb.append("?code=").append(URLEncoder.encode(authorizationCode.getCode(codeChallenge), StandardCharsets.UTF_8));
        } else {
            return null;
        }
        if (state != null) {
            sb.append("&state=").append(state);
        }
        return sb.toString();
    }

    private Response informUserAboutError(String error) {
        return Response.status(Response.Status.BAD_REQUEST).entity("""
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8"/>
                    <title>Error</title>
                </head>
                <body>
                <aside class="container">
                    <p>%s</p>
                </aside>
                </body>
                </html>
                """.formatted(error)).build();
    }
}

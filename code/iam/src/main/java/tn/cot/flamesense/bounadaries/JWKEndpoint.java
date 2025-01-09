package tn.cot.smarthydro.bounadaries;

import jakarta.ejb.EJB;
import jakarta.ejb.EJBException;
import jakarta.json.Json;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import tn.cot.smarthydro.security.JwtManager;

@Path("/jwk")
public class JWKEndpoint {

    @EJB
    private JwtManager jwtManager;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getJWK(@QueryParam("kid") String kid) {
        try {
            return Response.ok(jwtManager.getPublicKeyAsJWK(kid)).build();
        }catch (EJBException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Json.createObjectBuilder().add("error",e.getMessage()).build()).build();
        }
    }
}
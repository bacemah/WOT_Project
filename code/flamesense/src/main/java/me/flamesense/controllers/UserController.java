package me.flamesense.controllers;

import jakarta.ejb.EJBException;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.net.URI;
import java.util.function.Supplier;
import jakarta.json.Json;



import me.flamesense.services.UserService;
import me.flamesense.DTO.LogInDto;
import me.flamesense.entity.User;


@RequestScoped
@Path("/users")
@Produces({"application/json"})
@Consumes(MediaType.APPLICATION_JSON)
public class UserController {
    private static final Supplier<WebApplicationException> NOT_FOUND =
            () -> new WebApplicationException(Response.Status.NOT_FOUND);


    @Inject
    private UserService userService;


    @POST
    @Path("/signup")
    public Response signup(User user) {
        try {
            this.userService.createUser(user);
            return Response.ok(Json.createObjectBuilder()
                    .add("status" , 201)
                    .add("message" , "User registered successfully")
                    )
                    .build();
        }
        catch (EJBException e) {
            return  Response.status(Response.Status.BAD_REQUEST).entity(e.getCause()).build();
        }
    }


    // i need to undrestand the logic of authorize







    @POST
    @Path("/signin")
    public Response getUser(LogInDto logindto){
        try {

            URI redirectUri = this.userService.authentication(logindto);
            return Response.status(Response.Status.FOUND).location(redirectUri).build();


        }catch (EJBException e){
            return Response.status(Response.Status.NOT_FOUND).entity(e.getCause()).build();
        }
    }



}

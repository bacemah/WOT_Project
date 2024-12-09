package me.flamesense.boundaries;

import jakarta.ejb.EJBException;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.net.URI;
import java.util.function.Supplier;
import jakarta.json.Json;


import me.flamesense.DTO.UpdateDto;
import me.flamesense.services.UserService;
import me.flamesense.DTO.LogInDto;
import me.flamesense.entity.User;


@RequestScoped
@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserEndpoint {
    private static final Supplier<WebApplicationException> NOT_FOUND =
            () -> new WebApplicationException(Response.Status.NOT_FOUND);

    @Inject
    private UserService userService;


    @POST
    @Path("/signup")
    public Response signup(User user) {
        try {
            this.userService.createUser(user);
            return Response.ok(Json.createObjectBuilder().add("message", "Created successfuly").build()).build();
        }
        catch (EJBException e) {
            return  Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
    }

    @DELETE
    @Path("/delete/{email}")
    public Response delete(@PathParam("email") String email) {
        try{
            this.userService.deleteUser(email);
            return Response.ok(Json.createObjectBuilder()
                    .add("message" , "User deleted successfuly").build()).build();
        }catch (EJBException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
    }

    @PATCH
    @Path("/update/{email}")
    public Response update(@PathParam("email") String email , UpdateDto updateDto) {

        try {
            this.userService.updateUser(email, updateDto);
            return Response.ok(Json.createObjectBuilder().add("message", "Updated successfuly").build()).build();
        }catch (EJBException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
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

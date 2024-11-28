package me.flamesense.boundaries;

import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import me.flamesense.DTO.LogInDto;



import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import me.flamesense.exceptions.UserAlreadyExistsException;
import me.flamesense.utils.Argon2Utils;
import me.flamesense.entity.User;
import me.flamesense.repositories.UserRepository;
import me.flamesense.models.Email;
import me.flamesense.filtres.Secured;
@ApplicationScoped
@Path("/users")
@Produces({"application/json"})
@Consumes(MediaType.APPLICATION_JSON)
public class UserResource {
    private static final Supplier<WebApplicationException> NOT_FOUND =
            () -> new WebApplicationException(Response.Status.NOT_FOUND);
    @Inject
    UserRepository userRepository;

    @Inject
    Argon2Utils argon2Utils;

    @POST
    @Path("/signup")
    public Response createUser(@Valid User user) {
        try {
            // we need to check if the user already exists !!!!
            if (userRepository.findByEmail(user.getEmail()).isPresent()) {
                throw new UserAlreadyExistsException("User with id " + user.getEmail() + " already exists");
            }
            user.hashPassword(user.getPassword(),argon2Utils);
            userRepository.save(user);
            return Response.ok("User added successfully!").build();
        }
        catch (UserAlreadyExistsException e) {
            return  Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
    }

    @POST
    @Path("/signin")
    public Response getUser(LogInDto logindto){
        try {
            String email = logindto.getEmail();
            char[] password = logindto.getPassword().toCharArray();
            User user = userRepository.findByEmail(email).orElseThrow(()-> new NotFoundException("User with email " + email + " not found"));

            if (!argon2Utils.check(user.getPassword() ,password)){
                return Response.status( 401 , "Unauthorized ").build();
            }
            return Response.ok(user).build();

        }catch (NotFoundException e){
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }
    }

}

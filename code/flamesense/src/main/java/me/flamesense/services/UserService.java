package me.flamesense.services;

import jakarta.ejb.EJBException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import jakarta.json.Json;
import jakarta.ws.rs.core.UriBuilder;

import me.flamesense.DTO.UpdateDto;
import me.flamesense.repositories.UserRepository;
import me.flamesense.entity.User;
import me.flamesense.utils.Argon2Utils;
import me.flamesense.DTO.LogInDto;

@ApplicationScoped
public class UserService{

    @Inject
    UserRepository userRepository;

    @Inject
    Argon2Utils argon2Utils;

    public void createUser(User user) {
        if(userRepository.findByEmail(user.getEmail()).isPresent()){
            throw new EJBException(Json.createObjectBuilder()
                    .add("error", "email already in use")
                    .build().toString());
        }
        LocalDateTime timestamp = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String time = timestamp.format(formatter);
        user.setCreatedAt(time);

        user.hashPassword(user.getPassword(), argon2Utils);
         userRepository.save(user);
    }


    public URI authentication(LogInDto logInDto ){
        String email = logInDto.getEmail();
        char[] password = logInDto.getPassword().toCharArray();
        User user = userRepository.findByEmail(email).orElseThrow(()-> new EJBException("User with email " + email + " not found"));

        if (!argon2Utils.check(user.getPassword() ,password)){
            throw new EJBException("Wrong password");
        }
         URI redirectUri = UriBuilder.fromPath("/auth/token").build();
        return  redirectUri;
    }


    public void deleteUser(String email){
            User user =userRepository.findByEmail(email).orElseThrow(()-> new EJBException("User with email " + email + " not found"));
            userRepository.delete(user);
    }

    public void updateUser(String email , UpdateDto updateDto){

        User user = userRepository.findByEmail(email).orElseThrow(()-> new EJBException("User with email " + email + " not found"));
        user.setFirstName(updateDto.getFirstName()) ;
        user.setLastName(updateDto.getLastName()) ;
        user.hashPassword(updateDto.getPassword(), argon2Utils);
        user.setPhoneNumber(updateDto.getPhoneNumber());
        LocalDateTime timestamp = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String time = timestamp.format(formatter);
        user.setUpdatedAt(time);
        userRepository.save(user);


    }


}

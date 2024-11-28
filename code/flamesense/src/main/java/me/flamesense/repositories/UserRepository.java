package me.flamesense.repositories;


import jakarta.data.repository.Repository;
import jakarta.data.repository.CrudRepository;

import java.util.Optional;
import java.util.stream.Stream;

import me.flamesense.entity.User;

@Repository
public interface  UserRepository extends CrudRepository<User ,String>{
    Optional<User> findByEmail(String email);
    Stream<User> findAll();

}

package me.flamesense.entity;

import jakarta.json.bind.annotation.JsonbVisibility;

import jakarta.nosql.Column;
import jakarta.nosql.Entity;
import jakarta.nosql.Id;

import java.io.Serializable;
import java.util.Objects;

import me.flamesense.utils.FieldPropertyVisibilityStrategy;
import me.flamesense.utils.Argon2Utils;


@Entity
@JsonbVisibility(FieldPropertyVisibilityStrategy.class)
public class User implements Serializable{

    @Id
    @Column("email")
    private String email;
    @Column("firstName")
    private String firstName;
    @Column("lastName")
    private String lastName;
    @Column("phoneNumber")
    private String phoneNumber;
    @Column("password")
    private String password;

    public User() {
    }

    public User(String email, String firstName, String lastName, String phoneNumber, String password) {
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
        this.password = password;
    }



    public String getEmail() {
        return email;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getPassword() {
        return password;
    }


    public void setEmail(String email) {
        this.email = email;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setPassword(String password) {
        this.password = password;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(email, user.email) && Objects.equals(firstName, user.firstName) && Objects.equals(lastName, user.lastName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email, firstName, lastName,  phoneNumber, password);
    }

    @Override
    public String toString() {
        return "Employee{" +
                "email='" + email + '\'' +
                ", forename='" + firstName + '\'' +
                ", surname='" + lastName + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", password='" + password + '\'' +
                '}';
    }




    public void hashPassword(String password, Argon2Utils argon2Utility) {
        this.password = argon2Utility.hash(password.toCharArray());
    }



}











package com.example.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data  //getters y setters
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "users")   //nombre de la tabla en base de datos
public class UserEntity { //usamos userEntity porque spring security utiliza clases User

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)   //genera id por nosotros.
    private Long id;

    @Email  //valida email
    @NotBlank
    @Size(max = 80)
    private String email;

    @NotBlank
    @Size(max = 30)
    private String username;

    @NotBlank
    private String password;

    //relacion entre las entidades. Es unidireccional. Solo necesitamos tener los roles aca en el User
    //   fetch.eagle: cuando yo consulto un usuario este trae todos los roles de una vez. cascade persist. Cuando borro un usuario no se borra el rol
    @ManyToMany(fetch = FetchType.EAGER, targetEntity = RoleEntity.class, cascade = CascadeType.PERSIST)
    @JoinTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))  //se genera una talba intermedia. defino como se llama esa tabla y las claves foraneas, tanto de usuario como de role.
    private Set<RoleEntity> roles;



}

package com.NorthrnLights.demo.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "\"user\"") // Escapa o nome da tabela pois "user" é palavra reservada no PostgreSQL
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Inheritance(strategy = InheritanceType.JOINED) // ou SINGLE_TABLE ou TABLE_PER_CLASS
public abstract class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userName;

    @Column(unique = true)
    private String email;

    @Column(name = "password")
    @JsonIgnore
    private String password;

    @Column(nullable = false)
    private Integer age;

    private String classRoom;

    @Enumerated(EnumType.STRING)
    private Role role;
    
    @Column(name = "profile_image")
    private String profileImage; // Caminho da imagem de perfil
}
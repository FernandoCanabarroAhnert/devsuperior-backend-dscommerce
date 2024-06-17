package com.devsuperior.dscommerce.dtos;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.devsuperior.dscommerce.entities.Role;
import com.devsuperior.dscommerce.entities.User;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class UserDTO {

    private Long id;
    @NotBlank(message = "Campo Requerido")
    private String name;
    @Email(message = "Email deve ser válido")
    @NotBlank(message = "Campo Requerido")
    private String email;
    @NotBlank(message = "Campo Requerido")
    private String phone;
    private LocalDate birthDate;

    private List<RoleDTO> roles = new ArrayList<>();

    public UserDTO() {
    }

    public UserDTO(User entity) {
        this.id = entity.getId();
        this.name = entity.getName();
        this.email = entity.getEmail();
        this.phone = entity.getPhone();
        this.birthDate = entity.getBirthDate();
        for (Role role : entity.getRoles()){
            roles.add(new RoleDTO(role));
        }
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public List<RoleDTO> getRoles() {
        return roles;
    }

    

    
    
}

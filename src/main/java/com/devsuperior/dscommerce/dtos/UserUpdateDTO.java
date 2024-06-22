package com.devsuperior.dscommerce.dtos;

import com.devsuperior.dscommerce.entities.User;
import com.devsuperior.dscommerce.services.validations.UserUpdateValid;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@UserUpdateValid
public class UserUpdateDTO extends UserDTO{

    @NotBlank(message = "Campo Obrigatório")
    @Size(min = 8,message = "Senha deve conter no mínimo 8 caracteres")
    private String password;

    public UserUpdateDTO() {
    }

    public UserUpdateDTO(User entity,String password) {
        super(entity);
        this.password = password;
    }

    public UserUpdateDTO(User entity) {
        super(entity);
        password = entity.getPassword();
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }



}

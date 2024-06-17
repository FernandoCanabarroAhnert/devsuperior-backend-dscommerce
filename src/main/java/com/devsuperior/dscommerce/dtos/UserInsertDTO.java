package com.devsuperior.dscommerce.dtos;

import com.devsuperior.dscommerce.entities.User;
import com.devsuperior.dscommerce.services.validations.UserInsertValid;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@UserInsertValid
public class UserInsertDTO extends UserDTO{

    @NotBlank(message = "Campo Obrigatório")
    @Size(min = 8,message = "Senha deve conter no mínimo 8 caracteres")
    private String password;

    public UserInsertDTO() {
    }

    public UserInsertDTO(User entity,String password) {
        super(entity);
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }



}

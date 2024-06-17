package com.devsuperior.dscommerce.services.validations;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.devsuperior.dscommerce.dtos.FieldMessage;
import com.devsuperior.dscommerce.dtos.UserInsertDTO;
import com.devsuperior.dscommerce.entities.User;
import com.devsuperior.dscommerce.repositories.UserRepository;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class UserInsertValidator implements ConstraintValidator<UserInsertValid,UserInsertDTO>{

    @Autowired
    private UserRepository userRepository;

    @Override
    public void initialize(UserInsertValid ann){}
    
    @Override
    public boolean isValid(UserInsertDTO dto, ConstraintValidatorContext context) {

        List<FieldMessage> list = new ArrayList<>();

        User user = userRepository.findByEmail(dto.getEmail());
        if (user != null) {
            list.add(new FieldMessage("email", "Este email já existe"));
        }

        for (FieldMessage f : list){
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(f.getMessage())
                .addPropertyNode(f.getFieldName())
                .addConstraintViolation();
        }
        return list.isEmpty();
    }

}

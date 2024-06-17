package com.devsuperior.dscommerce.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.devsuperior.dscommerce.dtos.EmailDTO;
import com.devsuperior.dscommerce.dtos.NewPasswordDTO;
import com.devsuperior.dscommerce.services.AuthService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService service;

    @PostMapping("/recover-token")
    public ResponseEntity<Void> createRecoverToken(@RequestBody @Valid EmailDTO obj){
        service.createRecoverToken(obj);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/new-password")
    public ResponseEntity<Void> saveNewPassword(@RequestBody @Valid NewPasswordDTO obj){
        service.saveNewPassword(obj);
        return ResponseEntity.noContent().build();
    }
}

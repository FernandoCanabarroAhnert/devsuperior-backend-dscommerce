package com.devsuperior.dscommerce.controllers.it;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.devsuperior.dscommerce.dtos.EmailDTO;
import com.devsuperior.dscommerce.dtos.NewPasswordDTO;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Transactional
public class AuthControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String existingEmail;
    private String nonExistingEmail;
    private String invalidEmail;

    private String tokenUUID;
    private String validPassword;
    private String blankPassword;
    private String invalidPassword;

    private EmailDTO valid;
    private EmailDTO nonExisting;
    private EmailDTO invalid;
    private EmailDTO blank;

    private NewPasswordDTO notFoundNewPassword;
    private NewPasswordDTO blankTokenNewPassword;
    private NewPasswordDTO newPasswordBlank;
    private NewPasswordDTO newPasswordInvalid;

    @BeforeEach
    public void setup() throws Exception{
        existingEmail = "maria@gmail.com";
        nonExistingEmail = "fernando@gmail.com";
        invalidEmail = "dhajhfahjse";

        valid = new EmailDTO(existingEmail);
        nonExisting = new EmailDTO(nonExistingEmail);
        invalid = new EmailDTO(invalidEmail);
        blank = new EmailDTO();

        tokenUUID = UUID.randomUUID().toString();
        blankPassword = "                    ";
        invalidPassword = "123";
        validPassword = "123456789";

        notFoundNewPassword = new NewPasswordDTO(tokenUUID, validPassword);
        blankTokenNewPassword = new NewPasswordDTO("", validPassword);
        newPasswordBlank = new NewPasswordDTO(tokenUUID, blankPassword);
        newPasswordInvalid = new NewPasswordDTO(tokenUUID, invalidPassword);
    }

    @Test
    public void createRecoverTokenShouldReturnHttpStatus204WhenEmailIsValidAndExists() throws Exception{
        mockMvc.perform(post("/auth/recover-token")
            .content(objectMapper.writeValueAsString(valid))
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());
    }

    @Test
    public void createRecoverTokenShouldReturnHttpStatus422WhenEmailIsBlank() throws Exception{
        mockMvc.perform(post("/auth/recover-token")
            .content(objectMapper.writeValueAsString(blank))
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.errors[0].fieldName").value("email"))
            .andExpect(jsonPath("$.errors[0].message").value("Campo requerido"));
    }

    @Test
    public void createRecoverTokenShouldReturnHttpStatus422WhenEmailIsInvalid() throws Exception{
        mockMvc.perform(post("/auth/recover-token")
            .content(objectMapper.writeValueAsString(invalid))
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.errors[0].fieldName").value("email"))
            .andExpect(jsonPath("$.errors[0].message").value("Email deve ser válido"));
    }

    @Test
    public void createRecoverTokenShouldReturnHttpStatus404WhenEmailIsValidButDoesNotExistInDatabase() throws Exception{
        mockMvc.perform(post("/auth/recover-token")
            .content(objectMapper.writeValueAsString(nonExisting))
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    @Test
    public void saveNewPasswordShouldReturnHttpStatus422WhenTokenIsBlank() throws Exception{
        mockMvc.perform(put("/auth/new-password")
            .content(objectMapper.writeValueAsString(blankTokenNewPassword))
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.errors[0].fieldName").value("token"))
            .andExpect(jsonPath("$.errors[0].message").value("Campo requerido"));
    }

    @Test
    public void saveNewPasswordShouldReturnHttpStatus422WhenPasswordIsBlank() throws Exception{
        mockMvc.perform(put("/auth/new-password")
            .content(objectMapper.writeValueAsString(newPasswordBlank))
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.errors[0].fieldName").value("password"))
            .andExpect(jsonPath("$.errors[0].message").value("Campo requerido"));
    }

    @Test
    public void saveNewPasswordShouldReturnHttpStatus422WhenTPasswordIsInvalid() throws Exception{
        mockMvc.perform(put("/auth/new-password")
            .content(objectMapper.writeValueAsString(newPasswordInvalid))
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.errors[0].fieldName").value("password"))
            .andExpect(jsonPath("$.errors[0].message").value("Senha deve conter no mínimo 8 caracteres"));
    }

    @Test
    public void saveNewPasswordShouldReturnHttpStatus404WhenTokenIsNotFound() throws Exception{
        mockMvc.perform(put("/auth/new-password")
            .content(objectMapper.writeValueAsString(notFoundNewPassword))
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }
}

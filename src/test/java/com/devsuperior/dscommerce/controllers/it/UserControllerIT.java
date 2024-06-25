package com.devsuperior.dscommerce.controllers.it;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.devsuperior.dscommerce.dtos.UserInsertDTO;
import com.devsuperior.dscommerce.dtos.UserUpdateDTO;
import com.devsuperior.dscommerce.entities.User;
import com.devsuperior.dscommerce.factories.RoleFactory;
import com.devsuperior.dscommerce.util.TokenUtil;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Transactional
public class UserControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TokenUtil tokenUtil;

    private String adminUsername,adminPassword;
    private String clientUsername,clientPassword;
    private String bearerTokenClient,bearerTokenAdmin;

    private Long existingId, nonExistingId, otherUserId;

    private User user;
    private UserInsertDTO insertDto;
    private UserUpdateDTO updateDto;

    @BeforeEach
    public void setup() throws Exception{
        
        adminUsername = "alex@gmail.com";
        adminPassword = "123456";

        clientUsername = "maria@gmail.com";
        clientPassword = "123456";

        bearerTokenClient = tokenUtil.obtainAccessToken(mockMvc, clientUsername, clientPassword);
        bearerTokenAdmin = tokenUtil.obtainAccessToken(mockMvc, adminUsername, adminPassword);

        existingId = 1L;
        nonExistingId = 999L;
        otherUserId = 2L;

        user = new User(null, "Fernando", "fernando@gmail.com", "123456789", LocalDate.parse("2005-10-28"), "12345678");
        user.addRole(RoleFactory.ROLE_CLIENT());
        insertDto = new UserInsertDTO(user);
        updateDto = new UserUpdateDTO(user);
    }

    @Test
    public void getMeShouldReturnHttpStatus401WhenNoUserIsLogged() throws Exception{
        mockMvc.perform(get("/users/me")
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnauthorized());
    }

    @Test
    public void getMeShouldReturnHttpStatus200WhenClientIsLogged() throws Exception{
        mockMvc.perform(get("/users/me")
            .header("Authorization", "Bearer " + bearerTokenClient)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1L))
            .andExpect(jsonPath("$.name").value("Maria Brown"))
            .andExpect(jsonPath("$.roles[0].id").value(1L))
            .andExpect(jsonPath("$.roles[0].authority").value("ROLE_CLIENT"));
    }

    @Test
    public void getMeShouldReturnHttpStatus200WhenAdminIsLogged() throws Exception{
        mockMvc.perform(get("/users/me")
            .header("Authorization", "Bearer " + bearerTokenAdmin)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(2L))
            .andExpect(jsonPath("$.name").value("Alex Green"))
            .andExpect(jsonPath("$.roles[0].id").value(1L))
            .andExpect(jsonPath("$.roles[0].authority").value("ROLE_CLIENT"))
            .andExpect(jsonPath("$.roles[1].id").value(2L))
            .andExpect(jsonPath("$.roles[1].authority").value("ROLE_ADMIN"));
    }

    @Test
    public void findAllShouldReturnHttpStatus403WhenNoUserIsLogged() throws Exception{
        mockMvc.perform(get("/users")
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnauthorized());
    }

    @Test
    public void findAllShouldReturnHttpStatus403WhenClientIsLogged() throws Exception{
        mockMvc.perform(get("/users")
            .header("Authorization", "Bearer " + bearerTokenClient)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden());
    }

    @Test
    public void findAllShouldReturnHttpStatus200WhenAdminIsLogged() throws Exception{
        mockMvc.perform(get("/users")
            .header("Authorization", "Bearer " + bearerTokenAdmin)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].id").value(1L))
            .andExpect(jsonPath("$.content[0].name").value("Maria Brown"))
            .andExpect(jsonPath("$.content[1].id").value(2L))
            .andExpect(jsonPath("$.content[1].name").value("Alex Green"));
    }

    @Test
    public void findByIdShouldReturnHttpStatus401WhenNoUserIsLogged() throws Exception{
        mockMvc.perform(get("/users/{id}",existingId)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnauthorized());
    }

    @Test
    public void findByIdShouldReturnHttpStatus403WhenClientIsLogged() throws Exception{
        mockMvc.perform(get("/users/{id}",existingId)
            .header("Authorization", "Bearer " + bearerTokenClient)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden());
    }

    @Test
    public void findByIdReturnHttpStatus200WhenAdminIsLoggedAndIdExists() throws Exception{
        mockMvc.perform(get("/users/{id}",existingId)
            .header("Authorization", "Bearer " + bearerTokenAdmin)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1L))
            .andExpect(jsonPath("$.name").value("Maria Brown"))
            .andExpect(jsonPath("$.roles[0].id").value(1L))
            .andExpect(jsonPath("$.roles[0].authority").value("ROLE_CLIENT"));
    }

    @Test
    public void findByIdShouldReturnHttpStatus404WhenAdminIsLoggedButIdDoesNotExist() throws Exception{
        mockMvc.perform(get("/users/{id}",nonExistingId)
            .header("Authorization", "Bearer " + bearerTokenAdmin)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    @Test
    public void insertShouldReturnHttpStatus201WhenDataIsValid() throws Exception{
        mockMvc.perform(post("/users")
            .content(objectMapper.writeValueAsString(insertDto))
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(3L))
            .andExpect(jsonPath("$.name").value("Fernando"))
            .andExpect(jsonPath("$.email").value("fernando@gmail.com"))
            .andExpect(jsonPath("$.roles[0].id").value(1L))
            .andExpect(jsonPath("$.roles[0].authority").value("ROLE_CLIENT"));
    }

    @Test
    public void insertShouldReturnHttpStatus422WhenNameIsBlank() throws Exception{
        insertDto.setName("");

        mockMvc.perform(post("/users")
            .content(objectMapper.writeValueAsString(insertDto))
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.errors[0].fieldName").value("name"))
            .andExpect(jsonPath("$.errors[0].message").value("Campo Requerido"));
    }

    @Test
    public void insertShouldReturnHttpStatus422WhenEmailIsBlank() throws Exception{
        insertDto.setEmail("");

        mockMvc.perform(post("/users")
            .content(objectMapper.writeValueAsString(insertDto))
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.errors[0].fieldName").value("email"))
            .andExpect(jsonPath("$.errors[0].message").value("Campo Requerido"));
    }

    @Test
    public void insertShouldReturnHttpStatus422WhenEmailAlreadyExists() throws Exception{
        insertDto.setEmail(clientUsername);

        mockMvc.perform(post("/users")
            .content(objectMapper.writeValueAsString(insertDto))
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.errors[0].fieldName").value("email"))
            .andExpect(jsonPath("$.errors[0].message").value("Este email já existe"));
    }

    @Test
    public void insertShouldReturnHttpStatus422WhenEmailIsInvalid() throws Exception{
        insertDto.setEmail("hasfhahhjsfas");

        mockMvc.perform(post("/users")
            .content(objectMapper.writeValueAsString(insertDto))
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.errors[0].fieldName").value("email"))
            .andExpect(jsonPath("$.errors[0].message").value("Email deve ser válido"));
    }

    @Test
    public void insertShouldReturnHttpStatus422WhenPhoneIsBlank() throws Exception{
        insertDto.setPhone("");

        mockMvc.perform(post("/users")
            .content(objectMapper.writeValueAsString(insertDto))
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.errors[0].fieldName").value("phone"))
            .andExpect(jsonPath("$.errors[0].message").value("Campo Requerido"));
    }

    @Test
    public void insertShouldReturnHttpStatus422WhenBirthDateIsNotInThePast() throws Exception{
        insertDto.setBirthDate(LocalDate.ofInstant(Instant.now().plusSeconds(1800), ZoneId.systemDefault()));

        mockMvc.perform(post("/users")
            .content(objectMapper.writeValueAsString(insertDto))
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.errors[0].fieldName").value("birthDate"))
            .andExpect(jsonPath("$.errors[0].message").value("Data de Nascimento deve ser no passado"));
    }

    @Test
    public void insertShouldReturnHttpStatus422WhenPasswordIsBlank() throws Exception{
        insertDto.setPassword("                  ");

        mockMvc.perform(post("/users")
            .content(objectMapper.writeValueAsString(insertDto))
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.errors[0].fieldName").value("password"))
            .andExpect(jsonPath("$.errors[0].message").value("Campo Obrigatório"));
    }

    @Test
    public void insertShouldReturnHttpStatus422WhenPasswordIsTooShort() throws Exception{
        insertDto.setPassword("123");

        mockMvc.perform(post("/users")
            .content(objectMapper.writeValueAsString(insertDto))
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.errors[0].fieldName").value("password"))
            .andExpect(jsonPath("$.errors[0].message").value("Senha deve conter no mínimo 8 caracteres"));
    }

    @Test
    public void updateShouldReturnHttpStatus401WhenNoUserIsLogged() throws Exception{
        mockMvc.perform(put("/users/{id}",existingId)
            .content(objectMapper.writeValueAsString(updateDto))
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnauthorized());
    }

    @Test
    public void updateShouldReturnHttpStatus403WhenOtherUserIsLogged() throws Exception{
        mockMvc.perform(put("/users/{id}",otherUserId)
            .header("Authorization", "Bearer " + bearerTokenClient)
            .content(objectMapper.writeValueAsString(updateDto))
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden());
    }

    @Test
    public void updateShouldReturnHttpStatus200WhenAdminIsLoggedAndIdExists() throws Exception{
        mockMvc.perform(put("/users/{id}",existingId)
            .header("Authorization", "Bearer " + bearerTokenAdmin)
            .content(objectMapper.writeValueAsString(updateDto))
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(existingId))
            .andExpect(jsonPath("$.name").value("Fernando"))
            .andExpect(jsonPath("$.email").value("fernando@gmail.com"))
            .andExpect(jsonPath("$.roles[0].id").value(1L))
            .andExpect(jsonPath("$.roles[0].authority").value("ROLE_CLIENT"));
    }

    @Test
    public void updateShouldReturnHttpStatus200WhenSelfIsLoggedAndIdExists() throws Exception{
        mockMvc.perform(put("/users/{id}",existingId)
            .header("Authorization", "Bearer " + bearerTokenClient)
            .content(objectMapper.writeValueAsString(updateDto))
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(existingId))
            .andExpect(jsonPath("$.name").value("Fernando"))
            .andExpect(jsonPath("$.email").value("fernando@gmail.com"))
            .andExpect(jsonPath("$.roles[0].id").value(1L))
            .andExpect(jsonPath("$.roles[0].authority").value("ROLE_CLIENT"));
    }

    @Test
    public void updateShouldReturnHttpStatus422WhenNameIsBlank() throws Exception{
        updateDto.setName("");

        mockMvc.perform(put("/users/{id}",existingId)
            .header("Authorization", "Bearer " + bearerTokenAdmin)
            .content(objectMapper.writeValueAsString(updateDto))
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.errors[0].fieldName").value("name"))
            .andExpect(jsonPath("$.errors[0].message").value("Campo Requerido"));
    }

    @Test
    public void updateShouldReturnHttpStatus422WhenEmailIsBlank() throws Exception{
        updateDto.setEmail("");

        mockMvc.perform(put("/users/{id}",existingId)
            .header("Authorization", "Bearer " + bearerTokenAdmin)
            .content(objectMapper.writeValueAsString(updateDto))
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.errors[0].fieldName").value("email"))
            .andExpect(jsonPath("$.errors[0].message").value("Campo Requerido"));
    }

    @Test
    public void updateShouldReturnHttpStatus422WhenEmailAlreadyExists() throws Exception{
        updateDto.setEmail(adminUsername);

        mockMvc.perform(put("/users/{id}",existingId)
            .header("Authorization", "Bearer " + bearerTokenAdmin)
            .content(objectMapper.writeValueAsString(updateDto))
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.errors[0].fieldName").value("email"))
            .andExpect(jsonPath("$.errors[0].message").value("Este email já existe"));
    }

    @Test
    public void updateShouldReturnHttpStatus422WhenEmailIsInvalid() throws Exception{
        updateDto.setEmail("hasfhahhjsfas");

        mockMvc.perform(put("/users/{id}",existingId)
            .header("Authorization", "Bearer " + bearerTokenAdmin)
            .content(objectMapper.writeValueAsString(updateDto))
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.errors[0].fieldName").value("email"))
            .andExpect(jsonPath("$.errors[0].message").value("Email deve ser válido"));
    }

    @Test
    public void updateShouldReturnHttpStatus422WhenPhoneIsBlank() throws Exception{
        updateDto.setPhone("");

        mockMvc.perform(put("/users/{id}",existingId)
            .header("Authorization", "Bearer " + bearerTokenAdmin)
            .content(objectMapper.writeValueAsString(updateDto))
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.errors[0].fieldName").value("phone"))
            .andExpect(jsonPath("$.errors[0].message").value("Campo Requerido"));
    }

    @Test
    public void updateShouldReturnHttpStatus422WhenBirthDateIsNotInThePast() throws Exception{
        updateDto.setBirthDate(LocalDate.ofInstant(Instant.now().plusSeconds(1800), ZoneId.systemDefault()));

        mockMvc.perform(put("/users/{id}",existingId)
            .header("Authorization", "Bearer " + bearerTokenAdmin)
            .content(objectMapper.writeValueAsString(updateDto))
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.errors[0].fieldName").value("birthDate"))
            .andExpect(jsonPath("$.errors[0].message").value("Data de Nascimento deve ser no passado"));
    }

    @Test
    public void updateShouldReturnHttpStatus422WhenPasswordIsBlank() throws Exception{
        updateDto.setPassword("                  ");

        mockMvc.perform(put("/users/{id}",existingId)
            .header("Authorization", "Bearer " + bearerTokenAdmin)
            .content(objectMapper.writeValueAsString(updateDto))
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.errors[0].fieldName").value("password"))
            .andExpect(jsonPath("$.errors[0].message").value("Campo Obrigatório"));
    }

    @Test
    public void updateShouldReturnHttpStatus422WhenPasswordIsTooShort() throws Exception{
        updateDto.setPassword("123");

        mockMvc.perform(put("/users/{id}",existingId)
            .header("Authorization", "Bearer " + bearerTokenAdmin)
            .content(objectMapper.writeValueAsString(updateDto))
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.errors[0].fieldName").value("password"))
            .andExpect(jsonPath("$.errors[0].message").value("Senha deve conter no mínimo 8 caracteres"));
    }

    @Test
    public void deleteShouldReturnHttpStatus401WhenNoUserIsLogged() throws Exception{
        mockMvc.perform(delete("/users/{id}",existingId)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnauthorized());
    }

    @Test
    public void deleteShouldReturnHttpStatus403WhenClientIsLogged() throws Exception{
        mockMvc.perform(delete("/users/{id}",existingId)
            .header("Authorization", "Bearer " + bearerTokenClient)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden());
    }

    @Test
    public void deleteShouldReturnHttpStatus204WhenAdminIsLoggedAndIdExistsAndIdIsNotDependent() throws Exception{
        mockMvc.perform(delete("/users/{id}",existingId)
            .header("Authorization", "Bearer " + bearerTokenAdmin)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());
    }

    @Test
    @Transactional(propagation = Propagation.SUPPORTS)
    public void deleteShouldReturnHttpStatus400WhenAdminIsLoggedAndIdExistsButIdIsDependent() throws Exception{
        mockMvc.perform(delete("/users/{id}",existingId)
            .header("Authorization", "Bearer " + bearerTokenAdmin)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void deleteShouldReturnHttpStatus404WhenAdminIsLoggedButIdDoesNotExist() throws Exception{
        mockMvc.perform(delete("/users/{id}",nonExistingId)
            .header("Authorization", "Bearer " + bearerTokenAdmin)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }
    
}

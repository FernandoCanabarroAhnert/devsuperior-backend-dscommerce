package com.devsuperior.dscommerce.controllers.ra;

import static io.restassured.RestAssured.baseURI;
import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItems;



import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.devsuperior.dscommerce.util.TokenUtil;

import io.restassured.http.ContentType;

@SpringBootTest
@Transactional
public class UserControllerRA {

    private String adminUsername,adminPassword;
    private String clientUsername,clientPassword;
    private String bearerTokenClient,bearerTokenAdmin;

    private Map<String,Object> postUserInstance;

    @BeforeEach
    public void setup() throws Exception{
        baseURI = "http://localhost:8080";
        
        adminUsername = "alex@gmail.com";
        adminPassword = "123456";

        clientUsername = "maria@gmail.com";
        clientPassword = "123456";

        bearerTokenClient = TokenUtil.obtainAccessTokenRestAssured(clientUsername, clientPassword);
        bearerTokenAdmin = TokenUtil.obtainAccessTokenRestAssured(adminUsername, adminPassword);

        postUserInstance = new HashMap<>();
        postUserInstance.put("name", "Bob Brown");
        postUserInstance.put("email", "bob@gmail.com");
        postUserInstance.put("phone", "12345678910");
        postUserInstance.put("birthDate", "1980-03-20");
        postUserInstance.put("password", "bob12345");
    }

     @Test
    public void getMeShouldReturnHttpStatus401WhenNoUserIsLogged() throws Exception{
        given()
            .get("/users/me")
        .then()
            .statusCode(401);
    }

    @Test
    public void getMeShouldReturnHttpStatus200WhenClientIsLogged() throws Exception{
        given()
            .header("Content-type","application/json")
            .header("Authorization","Bearer " + bearerTokenClient)
            .accept(ContentType.JSON)
        .when()
            .get("/users/me")
        .then()
            .statusCode(200)
            .body("id", equalTo(1))
            .body("name", equalTo("Maria Brown"))
            .body("email", equalTo("maria@gmail.com"))
            .body("roles[0].id", equalTo(1))
            .body("roles[0].authority", equalTo("ROLE_CLIENT"));
    }

    @Test
    public void getMeShouldReturnHttpStatus200WhenAdminIsLogged() throws Exception{
        given()
            .header("Content-type","application/json")
            .header("Authorization","Bearer " + bearerTokenAdmin)
            .accept(ContentType.JSON)
        .when()
            .get("/users/me")
        .then()
            .statusCode(200)
            .body("id", equalTo(2))
            .body("name", equalTo("Alex Green"))
            .body("email", equalTo("alex@gmail.com"))
            .body("roles.id", hasItems(2))
            .body("roles.authority", hasItems("ROLE_ADMIN"));
    }

    @Test
    public void findAllShouldReturnHttpStatus401WhenNoUserIsLogged() throws Exception{
        given()
            .get("/users")
        .then()
            .statusCode(401);
    }

    @Test
    public void findAllShouldReturnHttpStatus403WhenClientIsLogged() throws Exception{
        given()
            .header("Content-type","application/json")
            .header("Authorization","Bearer " + bearerTokenClient)
            .accept(ContentType.JSON)
        .when()
            .get("/users")
        .then()
            .statusCode(403);
    }

    @Test
    public void findAllPagedShouldReturnHttpStatus200WhenAdminIsLogged() throws Exception{
        given()
            .header("Content-type","application/json")
            .header("Authorization","Bearer " + bearerTokenAdmin)
            .accept(ContentType.JSON)
        .when()
            .get("/users")
        .then()
            .statusCode(200)
            .body("totalElements", equalTo(3))
            .body("content.name", hasItems("Alex Green","Bob Brown"))
            .body("content.email", hasItems("alex@gmail.com","maria@gmail.com"));
    }

    @Test
    public void findByIdShouldReturnHttpStatus401WhenNoUserIsLogged() throws Exception{
        given()
            .get("/users/1")
        .then()
            .statusCode(401);
    }

    @Test
    public void findByIdReturnHttpStatus200WhenAdminIsLoggedAndIdExists() throws Exception{
        given()
            .header("Content-type","application/json")
            .header("Authorization","Bearer " + bearerTokenAdmin)
            .accept(ContentType.JSON)
        .when()
            .get("/users/1")
        .then()
            .statusCode(200)
            .body("id", equalTo(1))
            .body("name", equalTo("Bob Brown"))
            .body("email", equalTo("maria@gmail.com"))
            .body("roles[0].id", equalTo(1))
            .body("roles[0].authority", equalTo("ROLE_CLIENT"));
    }

    @Test
    public void findByIdShouldReturnHttpStatus404WhenAdminIsLoggedButIdDoesNotExist() throws Exception{
        given()
            .header("Content-type","application/json")
            .header("Authorization","Bearer " + bearerTokenAdmin)
            .accept(ContentType.JSON)
        .when()
            .get("/users/999")
        .then()
            .statusCode(404);
    }

    @Test
    public void insertShouldReturnHttpStatus201WhenDataIsValid() throws Exception{
        JSONObject newUser = new JSONObject(postUserInstance);

        given()
            .body(newUser)
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
        .when()
            .post("/users")
        .then()
            .statusCode(201)
            .body("name", equalTo("Bob Brown"))
            .body("email", equalTo("bob@gmail.com"))
            .body("phone", equalTo("12345678910"))
            .body("birthDate", equalTo("1980-03-20"))
            .body("roles.id", hasItems(1))
            .body("roles.authority", hasItems("ROLE_CLIENT"));
    }

    @Test
    public void insertShouldReturnHttpStatus422WhenNameIsBlank() throws Exception{
        postUserInstance.put("name", "     ");
        postUserInstance.put("email", "william@gmail.com");
        JSONObject newUser = new JSONObject(postUserInstance);

        given()
            .body(newUser)
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
        .when()
            .post("/users")
        .then()
            .statusCode(422)
            .body("errors[0].fieldName", equalTo("name"))
            .body("errors[0].message", equalTo("Campo Requerido"));
    }

    @Test
    public void insertShouldReturnHttpStatus422WhenEmailIsBlank() throws Exception{
        postUserInstance.put("email", "");
        JSONObject newUser = new JSONObject(postUserInstance);

        given()
            .body(newUser)
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
        .when()
            .post("/users")
        .then()
            .statusCode(422)
            .body("errors[0].fieldName", equalTo("email"))
            .body("errors[0].message", equalTo("Campo Requerido"));
    }

    @Test
    public void insertShouldReturnHttpStatus422WhenEmailAlreadyExists() throws Exception{
        JSONObject newUser = new JSONObject(postUserInstance);

        given()
            .body(newUser)
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
        .when()
            .post("/users")
        .then()
            .statusCode(422)
            .body("errors[0].fieldName", equalTo("email"))
            .body("errors[0].message", equalTo("Este email já existe"));
    }

    @Test
    public void insertShouldReturnHttpStatus422WhenEmailIsInvalid() throws Exception{
        postUserInstance.put("email", "shgiashgiah");
        JSONObject newUser = new JSONObject(postUserInstance);

        given()
            .body(newUser)
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
        .when()
            .post("/users")
        .then()
            .statusCode(422)
            .body("errors[0].fieldName", equalTo("email"))
            .body("errors[0].message", equalTo("Email deve ser válido"));
    }

    @Test
    public void insertShouldReturnHttpStatus422WhenPhoneIsBlank() throws Exception{
        postUserInstance.put("phone", " ");
        postUserInstance.put("email", "anita@gmail.com");
        JSONObject newUser = new JSONObject(postUserInstance);

        given()
            .body(newUser)
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
        .when()
            .post("/users")
        .then()
            .statusCode(422)
            .body("errors[0].fieldName", equalTo("phone"))
            .body("errors[0].message", equalTo("Campo Requerido"));
    }

    @Test
    public void insertShouldReturnHttpStatus422WhenBirthDateIsNotInThePast() throws Exception{
        postUserInstance.put("birthDate", "2077-10-28");
        postUserInstance.put("email", "lucas@gmail.com");
        JSONObject newUser = new JSONObject(postUserInstance);

        given()
            .body(newUser)
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
        .when()
            .post("/users")
        .then()
            .statusCode(422)
            .body("errors[0].fieldName", equalTo("birthDate"))
            .body("errors[0].message", equalTo("Data de Nascimento deve ser no passado"));
    }

    @Test
    public void insertShouldReturnHttpStatus422WhenPasswordIsBlank() throws Exception{
        postUserInstance.put("password", "                       ");
        JSONObject newUser = new JSONObject(postUserInstance);

        given()
            .body(newUser)
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
        .when()
            .post("/users")
        .then()
            .statusCode(422)
            .body("errors[0].fieldName", equalTo("password"))
            .body("errors[0].message", equalTo("Campo Obrigatório"));
    }

    @Test
    public void insertShouldReturnHttpStatus422WhenPasswordIsTooShort() throws Exception{
        postUserInstance.put("password", "123");
        postUserInstance.put("email", "fernando@gmail.com");
        JSONObject newUser = new JSONObject(postUserInstance);

        given()
            .body(newUser)
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
        .when()
            .post("/users")
        .then()
            .statusCode(422)
            .body("errors[0].fieldName", equalTo("password"))
            .body("errors[0].message", equalTo("Senha deve conter no mínimo 8 caracteres"));
    }

    @Test
    public void updateShouldReturnHttpStatus201WhenAdminIsLoggedDataIsValid() throws Exception{
        postUserInstance.put("email", "guilherme@gmail.com");

        List<Map<String,Object>> roles = new ArrayList<>();
        Map<String,Object> role1 = new HashMap<>();
        role1.put("id", 1);
        role1.put("authority", "ROLE_CLIENT");
        roles.add(role1);
        postUserInstance.put("roles", roles);

        JSONObject newUser = new JSONObject(postUserInstance);
    
        given()
            .header("Content-type","application/json")
            .header("Authorization","Bearer " + bearerTokenAdmin)
            .body(newUser)
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
        .when()
            .put("/users/1")
        .then()
            .statusCode(200)
            .body("name", equalTo("Bob Brown"))
            .body("email", equalTo("guilherme@gmail.com"))
            .body("phone", equalTo("12345678910"))
            .body("birthDate", equalTo("1980-03-20"))
            .body("roles.id", hasItems(1))
            .body("roles.authority", hasItems("ROLE_CLIENT"));
    }

    @Test
    public void updateShouldReturnHttpStatus201WhenSelfUserIsLoggedAndDataIsValid() throws Exception{
        postUserInstance.put("email", "maria@gmail.com");

        List<Map<String,Object>> roles = new ArrayList<>();
        Map<String,Object> role1 = new HashMap<>();
        role1.put("id", 1);
        role1.put("authority", "ROLE_CLIENT");
        roles.add(role1);
        postUserInstance.put("roles", roles);

        JSONObject newUser = new JSONObject(postUserInstance);
    
        given()
            .header("Content-type","application/json")
            .header("Authorization","Bearer " + bearerTokenClient)
            .body(newUser)
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
        .when()
            .put("/users/1")
        .then()
            .statusCode(200)
            .body("name", equalTo("Bob Brown"))
            .body("email", equalTo("maria@gmail.com"))
            .body("phone", equalTo("12345678910"))
            .body("birthDate", equalTo("1980-03-20"))
            .body("roles.id", hasItems(1))
            .body("roles.authority", hasItems("ROLE_CLIENT"));
    }

    @Test
    public void updateShouldReturnHttpStatus403WhenOtherUserIsLogged() throws Exception{
        postUserInstance.put("email", "davi@gmail.com");
        JSONObject newUser = new JSONObject(postUserInstance);
    
        given()
            .header("Content-type","application/json")
            .header("Authorization","Bearer " + bearerTokenClient)
            .body(newUser)
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
        .when()
            .put("/users/2")
        .then()
            .statusCode(403);
    }

    @Test
    public void updateShouldReturnHttpStatus404WhenIdDoesNotExist() throws Exception{
        postUserInstance.put("email", "andressa@gmail.com");
        JSONObject newUser = new JSONObject(postUserInstance);
    
        given()
            .header("Content-type","application/json")
            .header("Authorization","Bearer " + bearerTokenAdmin)
            .body(newUser)
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
        .when()
            .put("/users/999")
        .then()
            .statusCode(404);
    }
    
    @Test
    public void updateShouldReturnHttpStatus422WhenNameIsBlank() throws Exception{
        postUserInstance.put("name", "     ");
        postUserInstance.put("email", "william@gmail.com");
        JSONObject newUser = new JSONObject(postUserInstance);
    
        given()
            .header("Content-type","application/json")
            .header("Authorization","Bearer " + bearerTokenAdmin)
            .body(newUser)
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
        .when()
            .put("/users/1")
        .then()
            .statusCode(422)
            .body("errors[0].fieldName", equalTo("name"))
            .body("errors[0].message", equalTo("Campo Requerido"));
    }
    
    @Test
    public void updateShouldReturnHttpStatus422WhenEmailIsBlank() throws Exception{
        postUserInstance.put("email", "");
        JSONObject newUser = new JSONObject(postUserInstance);
    
        given()
            .header("Content-type","application/json")
            .header("Authorization","Bearer " + bearerTokenAdmin)
            .body(newUser)
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
        .when()
            .put("/users/1")
        .then()
            .statusCode(422)
            .body("errors[0].fieldName", equalTo("email"))
            .body("errors[0].message", equalTo("Campo Requerido"));
    }
    
    @Test
    public void updateShouldReturnHttpStatus422WhenEmailAlreadyExists() throws Exception{
        JSONObject newUser = new JSONObject(postUserInstance);
    
        given()
            .header("Content-type","application/json")
            .header("Authorization","Bearer " + bearerTokenAdmin)
            .body(newUser)
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
        .when()
            .put("/users/1")
        .then()
            .statusCode(422)
            .body("errors[0].fieldName", equalTo("email"))
            .body("errors[0].message", equalTo("Este email já existe"));
    }
    
    @Test
    public void updateShouldReturnHttpStatus422WhenEmailIsInvalid() throws Exception{
        postUserInstance.put("email", "shgiashgiah");
        JSONObject newUser = new JSONObject(postUserInstance);
    
        given()
            .header("Content-type","application/json")
            .header("Authorization","Bearer " + bearerTokenAdmin)
            .body(newUser)
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
        .when()
            .put("/users/1")
        .then()
            .statusCode(422)
            .body("errors[0].fieldName", equalTo("email"))
            .body("errors[0].message", equalTo("Email deve ser válido"));
    }
    
    @Test
    public void updateShouldReturnHttpStatus422WhenPhoneIsBlank() throws Exception{
        postUserInstance.put("phone", " ");
        postUserInstance.put("email", "anita@gmail.com");
        JSONObject newUser = new JSONObject(postUserInstance);
    
        given()
            .header("Content-type","application/json")
            .header("Authorization","Bearer " + bearerTokenAdmin)
            .body(newUser)
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
        .when()
            .put("/users/1")
        .then()
            .statusCode(422)
            .body("errors[0].fieldName", equalTo("phone"))
            .body("errors[0].message", equalTo("Campo Requerido"));
    }
    
    @Test
    public void updateShouldReturnHttpStatus422WhenBirthDateIsNotInThePast() throws Exception{
        postUserInstance.put("birthDate", "2077-10-28");
        postUserInstance.put("email", "lucas@gmail.com");
        JSONObject newUser = new JSONObject(postUserInstance);
    
        given()
            .header("Content-type","application/json")
            .header("Authorization","Bearer " + bearerTokenAdmin)
            .body(newUser)
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
        .when()
            .put("/users/1")
        .then()
            .statusCode(422)
            .body("errors[0].fieldName", equalTo("birthDate"))
            .body("errors[0].message", equalTo("Data de Nascimento deve ser no passado"));
    }
    
    @Test
    public void updateShouldReturnHttpStatus422WhenPasswordIsBlank() throws Exception{
        postUserInstance.put("password", "                       ");
        postUserInstance.put("email", "lui@gmail.com");
        JSONObject newUser = new JSONObject(postUserInstance);
    
        given()
            .header("Content-type","application/json")
            .header("Authorization","Bearer " + bearerTokenAdmin)
            .body(newUser)
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
        .when()
            .put("/users/1")
        .then()
            .statusCode(422)
            .body("errors[0].fieldName", equalTo("password"))
            .body("errors[0].message", equalTo("Campo Obrigatório"));
    }
    
    @Test
    public void updateShouldReturnHttpStatus422WhenPasswordIsTooShort() throws Exception{
        postUserInstance.put("password", "123");
        postUserInstance.put("email", "fernando@gmail.com");
        JSONObject newUser = new JSONObject(postUserInstance);
    
        given()
            .header("Content-type","application/json")
            .header("Authorization","Bearer " + bearerTokenAdmin)
            .body(newUser)
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
        .when()
            .put("/users/1")
        .then()
            .statusCode(422)
            .body("errors[0].fieldName", equalTo("password"))
            .body("errors[0].message", equalTo("Senha deve conter no mínimo 8 caracteres"));
    }

    @Test
    public void deleteShouldReturnHttpStatus401WhenNoUserIsLogged() throws Exception{
        given()
            .delete("/users/1")
        .then()
            .statusCode(401);
    }

    @Test
    public void deleteShouldReturnHttpStatus403WhenClientIsLogged() throws Exception{
        given()
            .header("Content-type","application/json")
            .header("Authorization","Bearer " + bearerTokenClient)
            .accept(ContentType.JSON)
        .when()
            .delete("/users/1")
        .then()
            .statusCode(403);
    }

    @Test
    public void deleteShouldReturnHttpStatus400WhenAdminIsLoggedAndIdExistsButIdIsDependent() throws Exception{
        given()
            .header("Content-type","application/json")
            .header("Authorization","Bearer " + bearerTokenAdmin)
            .accept(ContentType.JSON)
        .when()
            .delete("/users/1")
        .then()
            .statusCode(400);
    }

    @Test
    public void deleteShouldReturnHttpStatus404WhenAdminIsLoggedButIdDoesNotExist() throws Exception{
        given()
            .header("Content-type","application/json")
            .header("Authorization","Bearer " + bearerTokenAdmin)
            .accept(ContentType.JSON)
        .when()
            .delete("/users/999")
        .then()
            .statusCode(404);
    }

}

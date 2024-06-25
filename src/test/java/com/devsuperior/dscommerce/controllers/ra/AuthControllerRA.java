package com.devsuperior.dscommerce.controllers.ra;

import static io.restassured.RestAssured.baseURI;
import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.json.JSONException;
import org.json.simple.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;


import io.restassured.http.ContentType;

@SpringBootTest
@Transactional
public class AuthControllerRA {

    private String existingEmail;
    private String nonExistingEmail;
    private String invalidEmail;

    private String tokenUUID;
    private String validPassword;
    private String blankPassword;
    private String invalidPassword;

    private Map<String,Object> postEmailDTOInstance;
    private Map<String,Object> postNewPasswordDTOInstance;

    @BeforeEach
    public void setup() throws Exception{
        baseURI = "http://localhost:8080";

        existingEmail = "maria@gmail.com";
        nonExistingEmail = "fernando@gmail.com";
        invalidEmail = "dhajhfahjse";

        postEmailDTOInstance = new HashMap<>();
        postEmailDTOInstance.put("email", existingEmail);

        tokenUUID = UUID.randomUUID().toString();
        blankPassword = "                    ";
        invalidPassword = "123";
        validPassword = "123456789";

        postNewPasswordDTOInstance = new HashMap<>();
        postNewPasswordDTOInstance.put("token", tokenUUID);
        postNewPasswordDTOInstance.put("password", validPassword);
    }

    @Test
    public void createRecoverTokenShouldReturnHttpStatus204WhenEmailIsValidAndExists() throws JSONException{
        JSONObject newEmail = new JSONObject(postEmailDTOInstance);

        given()
            .body(newEmail)
            .contentType(ContentType.JSON)
        .when()
            .post("/auth/recover-token")
        .then()
            .statusCode(204);
    }

    @Test
    public void createRecoverTokenShouldReturnHttpStatus422WhenEmailIsBlank() throws JSONException{
        postEmailDTOInstance.put("email", "                                                     ");
        JSONObject newEmail = new JSONObject(postEmailDTOInstance);

        given()
            .body(newEmail)
            .contentType(ContentType.JSON)
        .then()
            .statusCode(422)
            .body("errors[0].fieldName", equalTo("email"))
            .body("errors[0].message", equalTo("Campo requerido"));
    }

    @Test
    public void createRecoverTokenShouldReturnHttpStatus422WhenEmailIsInvalid() throws JSONException{
        postEmailDTOInstance.put("email", invalidEmail);
        JSONObject newEmail = new JSONObject(postEmailDTOInstance);

        given()
            .body(newEmail)
            .contentType(ContentType.JSON)
        .then()
            .statusCode(422)
            .body("errors[0].fieldName", equalTo("email"))
            .body("errors[0].message", equalTo("Email deve ser válido"));
    }

    @Test
    public void createRecoverTokenShouldReturnHttpStatus404WhenEmailIsValidButDoesNotExistsInDatabase() throws JSONException{
        postEmailDTOInstance.put("email", nonExistingEmail);
        JSONObject newEmail = new JSONObject(postEmailDTOInstance);

        given()
            .body(newEmail)
            .contentType(ContentType.JSON)
        .then()
            .statusCode(404);
    }

    @Test
    public void saveNewPasswordShouldReturnHttpStatus422WhenPasswordIsBlank() throws JSONException{
        postNewPasswordDTOInstance.put("password", blankPassword);
        JSONObject newPassword = new JSONObject(postNewPasswordDTOInstance);

        given()
            .body(newPassword)
            .contentType(ContentType.JSON)
        .when()
            .put("/auth/new-password")
        .then()
            .statusCode(422)
            .body("errors[0].fieldName", equalTo("password"))
            .body("errors[0].message", equalTo("Campo requerido"));
    }

    @Test
    public void saveNewPasswordShouldReturnHttpStatus422WhenPasswordIsInvalid() throws JSONException{
        postNewPasswordDTOInstance.put("password", invalidPassword);
        JSONObject newPassword = new JSONObject(postNewPasswordDTOInstance);

        given()
            .body(newPassword)
            .contentType(ContentType.JSON)
        .when()
            .put("/auth/new-password")
        .then()
            .statusCode(422)
            .body("errors[0].fieldName", equalTo("password"))
            .body("errors[0].message", equalTo("Senha deve conter no mínimo 8 caracteres"));
    }

    @Test
    public void saveNewPasswordShouldReturnHttpStatus422WhenTokenIsBlank() throws JSONException{
        postNewPasswordDTOInstance.put("token", "");
        JSONObject newPassword = new JSONObject(postNewPasswordDTOInstance);

        given()
            .body(newPassword)
            .contentType(ContentType.JSON)
        .when()
            .put("/auth/new-password")
        .then()
            .statusCode(422)
            .body("errors[0].fieldName", equalTo("token"))
            .body("errors[0].message", equalTo("Campo requerido"));
    }

    @Test
    public void saveNewPasswordShouldReturnHttpStatus404WhenTokenDoesNotExist() throws JSONException{
        JSONObject newPassword = new JSONObject(postNewPasswordDTOInstance);

        given()
            .body(newPassword)
            .contentType(ContentType.JSON)
        .when()
            .put("/auth/new-password")
        .then()
            .statusCode(404);
    }
}

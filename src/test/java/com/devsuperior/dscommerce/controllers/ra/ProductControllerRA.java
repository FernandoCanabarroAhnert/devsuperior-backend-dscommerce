package com.devsuperior.dscommerce.controllers.ra;

import static io.restassured.RestAssured.baseURI;
import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.simple.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.devsuperior.dscommerce.util.TokenUtil;

import io.restassured.http.ContentType;

@SpringBootTest
@Transactional
public class ProductControllerRA {

    private String adminUsername,adminPassword;
    private String clientUsername,clientPassword;
    private String bearerTokenClient,bearerTokenAdmin;

    private Map<String, Object> postProductInstance;

    @BeforeEach
    public void setup() throws Exception{
        baseURI = "http://localhost:8080";

        postProductInstance = new HashMap<>();
        postProductInstance.put("name", "Meu produto");
        postProductInstance.put("description", "Lorem ipsum, dolor sit amet consectetur adipisicing elit. Qui ad, adipisci illum ipsam velit et odit eaque reprehenderit ex maxime delectus dolore labore, quisquam quae tempora natus esse aliquam veniam doloremque quam minima culpa alias maiores commodi. Perferendis enim");
        postProductInstance.put("imgUrl", "https://raw.githubusercontent.com/devsuperior/dscatalog-resources/master/backend/img/1-big.jpg");
        postProductInstance.put("price", 200.0);

        List<Map<String, Object>> categories = new ArrayList<>();

        Map<String, Object> category1 = new HashMap<>();
        category1.put("id", 2);
        Map<String, Object> category2 = new HashMap<>();
        category2.put("id", 3);

        categories.add(category1);
        categories.add(category2);

        postProductInstance.put("categories", categories);

        adminUsername = "alex@gmail.com";
        adminPassword = "123456";

        clientUsername = "maria@gmail.com";
        clientPassword = "123456";

        bearerTokenClient = TokenUtil.obtainAccessTokenRestAssured(clientUsername, clientPassword);
        bearerTokenAdmin = TokenUtil.obtainAccessTokenRestAssured(adminUsername, adminPassword);
    }

    @Test
    public void findByIdShouldReturnHttpStatus200WhenIdExists() throws JSONException{
        given().get("/products/1")
            .then().statusCode(200)
            .body("id",equalTo(1))
            .body("name",equalTo("The Lord of the Rings"))
            .body("price",equalTo(90.5F))
            .body("imgUrl",equalTo("https://raw.githubusercontent.com/devsuperior/dscatalog-resources/master/backend/img/1-big.jpg"))
            .body("categories.id",hasItems(1))
            .body("categories.name",hasItems("Livros"));
    }

    @Test
    public void findByIdShouldReturnHttpStatus404WhenIdDoesNotExist() throws JSONException{
        given().get("/products/999")
            .then().statusCode(404);
    }

    @Test
    public void findAllByNameShouldReturnAllProductsPagedWhenNameIsBlank() {
        given().get("/products")
            .then().statusCode(200)
            .body("totalElements", equalTo(25))
            .body("content.name", hasItems("Macbook Pro","PC Gamer"));
    }

    @Test
    public void findAllByNameShouldReturnAllProductsPagedWhenNameIsNotBlank() throws JSONException{
        given().get("/products?name=ma")
            .then().statusCode(200)
            .body("totalElements", equalTo(3))
            .body("content.name", hasItems("Smart TV","Macbook Pro","PC Gamer Max"));
    }

    @Test
    public void findAllByNameShouldReturnAllProductsPagedFilteringProductsWithPriceGreaterThan2000() throws JSONException{
        given().get("/products")
            .then().statusCode(200)
            .body("content.findAll { it.price > 2000 }.name", hasItems("PC Gamer Weed","PC Gamer Max"));
    }

    @Test
    public void insertShouldReturnHttpStatus201WhenAdminIsLoggedAndDataIsValid() throws JSONException {
        JSONObject newProduct = new JSONObject(postProductInstance);

        given()
            .header("Content-type", "application/json")
            .header("Authorization", "Bearer " + bearerTokenAdmin)
            .body(newProduct)
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
        .when()
            .post("/products")
        .then()
            .statusCode(201)
            .body("name", equalTo("Meu produto"))
            .body("price", is(200.0F))
            .body("imgUrl", equalTo("https://raw.githubusercontent.com/devsuperior/dscatalog-resources/master/backend/img/1-big.jpg"))
            .body("categories.id", hasItems(2, 3));
    }

    @Test
    public void insertShouldReturnHttpStatus403WhenDataIsValidButClientIsLogged() throws JSONException {
        JSONObject newProduct = new JSONObject(postProductInstance);

        given()
            .header("Content-type", "application/json")
            .header("Authorization", "Bearer " + bearerTokenClient)
            .body(newProduct)
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
        .when()
            .post("/products")
        .then()
            .statusCode(403);
    }

    @Test
    public void insertShouldReturnHttpStatus401WhenDataIsValidButNoUserIsLogged() throws JSONException {
        JSONObject newProduct = new JSONObject(postProductInstance);

        given()
            .body(newProduct)
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
        .when()
            .post("/products")
        .then()
            .statusCode(401);
    }

    @Test
    public void insertShouldReturnHttpStatus422WhenAdminIsLoggedButNameIsBlank() throws JSONException {
        postProductInstance.put("name", "        ");
        JSONObject newProduct = new JSONObject(postProductInstance);

        given()
            .header("Content-type", "application/json")
            .header("Authorization", "Bearer " + bearerTokenAdmin)
            .body(newProduct)
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
        .when()
            .post("/products")
        .then()
            .statusCode(422)
            .body("errors[0].fieldName", equalTo("name"))
            .body("errors[0].message", equalTo("Campo Obrigatório"));
    }

    
    @Test
    public void insertShouldReturnHttpStatus422WhenAdminIsLoggedButNameIsTooShort() throws JSONException {
        postProductInstance.put("name", "me");
        JSONObject newProduct = new JSONObject(postProductInstance);

        given()
            .header("Content-type", "application/json")
            .header("Authorization", "Bearer " + bearerTokenAdmin)
            .body(newProduct)
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
        .when()
            .post("/products")
        .then()
            .statusCode(422)
            .body("errors[0].fieldName", equalTo("name"))
            .body("errors[0].message", equalTo("Nome deve ter entre 3 a 80 caracteres"));
    }

    @Test
    public void insertShouldReturnHttpStatus422WhenAdminIsLoggedButDescriptionIsBlank() throws JSONException {
        postProductInstance.put("description", "                ");
        JSONObject newProduct = new JSONObject(postProductInstance);

        given()
            .header("Content-type", "application/json")
            .header("Authorization", "Bearer " + bearerTokenAdmin)
            .body(newProduct)
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
        .when()
            .post("/products")
        .then()
            .statusCode(422)
            .body("errors[0].fieldName", equalTo("description"))
            .body("errors[0].message", equalTo("Campo Obrigatório"));
    }

    @Test
    public void insertShouldReturnHttpStatus422WhenAdminIsLoggedButDescriptionIsTooShort() throws JSONException {
        postProductInstance.put("description", "desc");
        JSONObject newProduct = new JSONObject(postProductInstance);

        given()
            .header("Content-type", "application/json")
            .header("Authorization", "Bearer " + bearerTokenAdmin)
            .body(newProduct)
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
        .when()
            .post("/products")
        .then()
            .statusCode(422)
            .body("errors[0].fieldName", equalTo("description"))
            .body("errors[0].message", equalTo("Descrição deve ter no mínimo 10 caracteres"));
    }

    @Test
    public void insertShouldReturnHttpStatus422WhenAdminIsLoggedButPriceIsNegative() throws JSONException {
        postProductInstance.put("price",-10.0);
        JSONObject newProduct = new JSONObject(postProductInstance);

        given()
            .header("Content-type", "application/json")
            .header("Authorization", "Bearer " + bearerTokenAdmin)
            .body(newProduct)
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
        .when()
            .post("/products")
        .then()
            .statusCode(422)
            .body("errors[0].fieldName", equalTo("price"))
            .body("errors[0].message", equalTo("Preço deve ser positivo"));
    }

    @Test
    public void insertShouldReturnHttpStatus422WhenAdminIsLoggedButPriceIsZero() throws JSONException {
        postProductInstance.put("price",0.0);
        JSONObject newProduct = new JSONObject(postProductInstance);

        given()
            .header("Content-type", "application/json")
            .header("Authorization", "Bearer " + bearerTokenAdmin)
            .body(newProduct)
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
        .when()
            .post("/products")
        .then()
            .statusCode(422)
            .body("errors[0].fieldName", equalTo("price"))
            .body("errors[0].message", equalTo("Preço deve ser positivo"));
    }

    @Test
    public void insertShouldReturnHttpStatus422WhenAdminIsLoggedButPriceIsBlank() throws JSONException {
        postProductInstance.put("price", null);
        JSONObject newProduct = new JSONObject(postProductInstance);

        given()
            .header("Content-type", "application/json")
            .header("Authorization", "Bearer " + bearerTokenAdmin)
            .body(newProduct)
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
        .when()
            .post("/products")
        .then()
            .statusCode(422)
            .body("errors[0].fieldName", equalTo("price"))
            .body("errors[0].message", equalTo("Campo Obrigatório"));
    }

    @Test
    public void insertShouldReturnHttpStatus422WhenAdminIsLoggedButCategoriesIsBlank() throws JSONException {
        postProductInstance.remove("categories");
        JSONObject newProduct = new JSONObject(postProductInstance);

        given()
            .header("Content-type", "application/json")
            .header("Authorization", "Bearer " + bearerTokenAdmin)
            .body(newProduct)
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
        .when()
            .post("/products")
        .then()
            .statusCode(422)
            .body("errors[0].fieldName", equalTo("categories"))
            .body("errors[0].message", equalTo("O produto deve ter pelo menos 1 categoria"));
    }

    @Test
    public void updateShouldReturnnHttpStatus401WhenNoUserIsLogged() throws JSONException{
        JSONObject newProduct = new JSONObject(postProductInstance);

        given()
            .body(newProduct)
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
        .when()
           .put("/products/1")
        .then()
            .statusCode(401);
    }

    @Test
    public void updateShouldReturnnHttpStatus403WhenClientIsLogged() throws JSONException{
        JSONObject newProduct = new JSONObject(postProductInstance);

        given()
            .header("Content_type","application/json")
            .header("Authorization","Bearer " + bearerTokenClient)
            .body(newProduct)
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
        .when()
           .put("/products/1")
        .then()
            .statusCode(403);
    }

    @Test
    public void updateShouldReturnHttpStatus201WhenAdminIsLoggedAndDataIsValid() throws JSONException{
        JSONObject newProduct = new JSONObject(postProductInstance);

        given()
            .header("Content-type","application/json")
            .header("Authorization", "Bearer " + bearerTokenAdmin)
            .body(newProduct)
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
        .when()
            .put("/products/1")
        .then()
            .statusCode(200)
            .body("name", equalTo("Meu produto"))
            .body("price", is(200.0F))
            .body("imgUrl", equalTo("https://raw.githubusercontent.com/devsuperior/dscatalog-resources/master/backend/img/1-big.jpg"))
            .body("categories.id", hasItems(2, 3));
    }

    @Test
    public void updateShouldReturnHttpStatus404WhenAdminIsLoggedButIdDoesNotExist() throws JSONException{
        JSONObject newProduct = new JSONObject(postProductInstance);

        given()
            .header("Content-type","application/json")
            .header("Authorization", "Bearer " + bearerTokenAdmin)
            .body(newProduct)
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
        .when()
            .put("/products/999")
        .then()
            .statusCode(404);
    }

    @Test
    public void updateShouldReturnHttpStatus422WhenAdminIsLoggedButNameIsBlank() throws JSONException {
        postProductInstance.put("name", "        ");
        JSONObject newProduct = new JSONObject(postProductInstance);

        given()
            .header("Content-type", "application/json")
            .header("Authorization", "Bearer " + bearerTokenAdmin)
            .body(newProduct)
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
        .when()
            .put("/products/1")
        .then()
            .statusCode(422)
            .body("errors[0].fieldName", equalTo("name"))
            .body("errors[0].message", equalTo("Campo Obrigatório"));
    }

    
    @Test
    public void updateShouldReturnHttpStatus422WhenAdminIsLoggedButNameIsTooShort() throws JSONException {
        postProductInstance.put("name", "me");
        JSONObject newProduct = new JSONObject(postProductInstance);

        given()
            .header("Content-type", "application/json")
            .header("Authorization", "Bearer " + bearerTokenAdmin)
            .body(newProduct)
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
        .when()
            .put("/products/1")
        .then()
            .statusCode(422)
            .body("errors[0].fieldName", equalTo("name"))
            .body("errors[0].message", equalTo("Nome deve ter entre 3 a 80 caracteres"));
    }

    @Test
    public void updateShouldReturnHttpStatus422WhenAdminIsLoggedButDescriptionIsBlank() throws JSONException {
        postProductInstance.put("description", "                ");
        JSONObject newProduct = new JSONObject(postProductInstance);

        given()
            .header("Content-type", "application/json")
            .header("Authorization", "Bearer " + bearerTokenAdmin)
            .body(newProduct)
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
        .when()
            .put("/products/1")
        .then()
            .statusCode(422)
            .body("errors[0].fieldName", equalTo("description"))
            .body("errors[0].message", equalTo("Campo Obrigatório"));
    }

    @Test
    public void updateShouldReturnHttpStatus422WhenAdminIsLoggedButDescriptionIsTooShort() throws JSONException {
        postProductInstance.put("description", "desc");
        JSONObject newProduct = new JSONObject(postProductInstance);

        given()
            .header("Content-type", "application/json")
            .header("Authorization", "Bearer " + bearerTokenAdmin)
            .body(newProduct)
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
        .when()
            .put("/products/1")
        .then()
            .statusCode(422)
            .body("errors[0].fieldName", equalTo("description"))
            .body("errors[0].message", equalTo("Descrição deve ter no mínimo 10 caracteres"));
    }

    @Test
    public void updateShouldReturnHttpStatus422WhenAdminIsLoggedButPriceIsNegative() throws JSONException {
        postProductInstance.put("price",-10.0);
        JSONObject newProduct = new JSONObject(postProductInstance);

        given()
            .header("Content-type", "application/json")
            .header("Authorization", "Bearer " + bearerTokenAdmin)
            .body(newProduct)
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
        .when()
            .put("/products/1")
        .then()
            .statusCode(422)
            .body("errors[0].fieldName", equalTo("price"))
            .body("errors[0].message", equalTo("Preço deve ser positivo"));
    }

    @Test
    public void updateShouldReturnHttpStatus422WhenAdminIsLoggedButPriceIsZero() throws JSONException {
        postProductInstance.put("price",0.0);
        JSONObject newProduct = new JSONObject(postProductInstance);

        given()
            .header("Content-type", "application/json")
            .header("Authorization", "Bearer " + bearerTokenAdmin)
            .body(newProduct)
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
        .when()
            .put("/products/1")
        .then()
            .statusCode(422)
            .body("errors[0].fieldName", equalTo("price"))
            .body("errors[0].message", equalTo("Preço deve ser positivo"));
    }

    @Test
    public void updateShouldReturnHttpStatus422WhenAdminIsLoggedButPriceIsBlank() throws JSONException {
        postProductInstance.put("price", null);
        JSONObject newProduct = new JSONObject(postProductInstance);

        given()
            .header("Content-type", "application/json")
            .header("Authorization", "Bearer " + bearerTokenAdmin)
            .body(newProduct)
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
        .when()
            .put("/products/1")
        .then()
            .statusCode(422)
            .body("errors[0].fieldName", equalTo("price"))
            .body("errors[0].message", equalTo("Campo Obrigatório"));
    }

    @Test
    public void updateShouldReturnHttpStatus422WhenAdminIsLoggedButCategoriesIsBlank() throws JSONException {
        postProductInstance.remove("categories");
        JSONObject newProduct = new JSONObject(postProductInstance);

        given()
            .header("Content-type", "application/json")
            .header("Authorization", "Bearer " + bearerTokenAdmin)
            .body(newProduct)
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
        .when()
            .put("/products/1")
        .then()
            .statusCode(422)
            .body("errors[0].fieldName", equalTo("categories"))
            .body("errors[0].message", equalTo("O produto deve ter pelo menos 1 categoria"));
    }

    @Test
    public void deleteShouldReturnHttpStatus403WhenClientIsLogged() throws JSONException {
        given()
            .header("Content-type", "application/json")
            .header("Authorization", "Bearer " + bearerTokenClient)
        .when()
            .delete("/products/1")
        .then()
            .statusCode(403);
    }

    @Test
    public void deleteShouldReturnHttpStatus401WhenIdExistsButNoUserIsLogged() throws JSONException {
        given()
            .delete("/products/1")
        .then()
            .statusCode(401);
    }

    @Test
    public void deleteShouldReturnHttpStatus204WhenAdminIsLoggedAndIdExists() throws JSONException {
        given()
            .header("Content-type", "application/json")
            .header("Authorization", "Bearer " + bearerTokenAdmin)
        .when()
            .delete("/products/2")
        .then()
            .statusCode(204);
    }

    @Test
    public void deleteShouldReturnHttpStatus404WhenAdminIsLoggedButIdDoesNotExist() throws JSONException {
        given()
            .header("Content-type", "application/json")
            .header("Authorization", "Bearer " + bearerTokenAdmin)
        .when()
            .delete("/products/999")
        .then()
            .statusCode(404);
    }

    @Test
    @Transactional(propagation = Propagation.SUPPORTS)
    public void deleteShouldReturnHttpStatus400WhenAdminIsLoggedButIdIsDependent() throws JSONException {
        given()
            .header("Content-type", "application/json")
            .header("Authorization", "Bearer " + bearerTokenAdmin)
        .when()
            .delete("/products/3")
        .then()
            .statusCode(400);
    }
}

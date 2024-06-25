package com.devsuperior.dscommerce.controllers.ra;

import static io.restassured.RestAssured.baseURI;
import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItems;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.simple.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.devsuperior.dscommerce.util.TokenUtil;

import io.restassured.http.ContentType;

@SpringBootTest
@Transactional
public class OrderControllerRA {

    private String adminUsername,adminPassword;
    private String clientUsername,clientPassword;
    private String bearerTokenClient,bearerTokenAdmin;

    private Map<String,Object> postOrderInstance;

    @BeforeEach
    public void setup() throws Exception{
        baseURI = "http://localhost:8080";

        adminUsername = "alex@gmail.com";
        adminPassword = "123456";

        clientUsername = "maria@gmail.com";
        clientPassword = "123456";

        bearerTokenClient = TokenUtil.obtainAccessTokenRestAssured(clientUsername, clientPassword);
        bearerTokenAdmin = TokenUtil.obtainAccessTokenRestAssured(adminUsername, adminPassword);

        postOrderInstance = new HashMap<>();

        List<Map<String,Object>> items = new ArrayList<>();

        Map<String,Object> item1 = new HashMap<>();
        item1.put("productId", 15);
        item1.put("quantity", 3);
        Map<String,Object> item2 = new HashMap<>();
        item2.put("productId", 21);
        item2.put("quantity", 2);

        items.add(item1);
        items.add(item2);

        postOrderInstance.put("items", items);
    }

    @Test
    public void findByIdShouldReturnHttpStatus401WhenNoUserIsLogged() throws JSONException{
        given()
            .get("/orders/1")
        .then()
            .statusCode(401);
    }

    @Test
    public void findByIdShouldReturnHttpStatus403WhenOtherUserIsLogged() throws JSONException{
        given()
            .header("Content-type", "application/json")
            .header("Authorization", "Bearer " + bearerTokenClient)
            .accept(ContentType.JSON)
        .when()
            .get("/orders/2")
        .then()
            .statusCode(403);
    }

    @Test
    public void findByIdShouldReturnHttpStatus200WhenOrderBelongsToLoggedUser() throws JSONException{
        given()
            .header("Content-type", "application/json")
            .header("Authorization", "Bearer " + bearerTokenClient)
            .accept(ContentType.JSON)
        .when()
            .get("/orders/1")
        .then()
            .statusCode(200)
            .body("id", equalTo(1))
            .body("status",equalTo("PAID"))
            .body("client.id",equalTo(1))
            .body("client.name",equalTo("Maria Brown"))
            .body("payment.id",equalTo(1))
            .body("items.name",hasItems("The Lord of the Rings","Macbook Pro"));
    }

    @Test
    public void findByIdShouldReturnHttpStatus404WhenClientIsLoggedButIdDoesNotExist() throws JSONException{
        given()
            .header("Content-type", "application/json")
            .header("Authorization", "Bearer " + bearerTokenClient)
            .accept(ContentType.JSON)
        .when()
            .get("/orders/999")
        .then()
            .statusCode(404);
    }

    @Test
    public void findByIdShouldReturnHttpStatus200WhenAdminIsLoggedAndIdExists() throws JSONException{
        given()
            .header("Content-type", "application/json")
            .header("Authorization", "Bearer " + bearerTokenAdmin)
            .accept(ContentType.JSON)
        .when()
            .get("/orders/1")
        .then()
            .statusCode(200)
            .body("id", equalTo(1))
            .body("status",equalTo("PAID"))
            .body("client.id",equalTo(1))
            .body("client.name",equalTo("Maria Brown"))
            .body("payment.id",equalTo(1))
            .body("items.name",hasItems("The Lord of the Rings","Macbook Pro"));
    }

    @Test
    public void findByIdShouldReturnHttpStatus404WhenAdminIsLoggedButIdDoesNotExist() throws JSONException{
        given()
            .header("Content-type", "application/json")
            .header("Authorization", "Bearer " + bearerTokenAdmin)
            .accept(ContentType.JSON)
        .when()
            .get("/orders/999")
        .then()
            .statusCode(404);
    }

    @Test
    public void insertShouldReturnHttpStatus401WhenNoUserIsLogged() throws JSONException{
        JSONObject newOrder = new JSONObject(postOrderInstance);

        given()
            .body(newOrder)
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
        .when()
            .post("/orders")
        .then()
            .statusCode(401);
    }

    @Test
    public void insertShouldReturnHttpStatus201WhenClientIsLogged() throws Exception{
        JSONObject newOrder = new JSONObject(postOrderInstance);

        given()
            .header("Content-type", "application/json")
            .header("Authorization", "Bearer " + bearerTokenClient)
            .body(newOrder)
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
        .when()
            .post("/orders")
        .then()
            .statusCode(201)
            .body("status", equalTo("WAITING_PAYMENT"))
            .body("client.id", equalTo(1))
            .body("client.name", equalTo("Maria Brown"))
            .body("items.productId",hasItems(15,21))
            .body("items.name",hasItems("PC Gamer Weed","PC Gamer Tx"));
    }

    @Test
    public void insertShouldReturnHttpStatus422WhenClientIsLoggedButItemsIsEmpty() throws Exception{
        postOrderInstance.put("items", Arrays.asList());
        JSONObject newOrder = new JSONObject(postOrderInstance);

        given()
            .header("Content-type", "application/json")
            .header("Authorization", "Bearer " + bearerTokenClient)
            .body(newOrder)
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
        .when()
            .post("/orders")
        .then()
            .statusCode(422)
            .body("errors[0].fieldName", equalTo("items"))
            .body("errors[0].message", equalTo("O pedido deve ter pelo menos 1 item"));
    }
}

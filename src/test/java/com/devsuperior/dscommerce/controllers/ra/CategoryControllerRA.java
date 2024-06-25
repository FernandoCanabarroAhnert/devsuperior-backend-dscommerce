package com.devsuperior.dscommerce.controllers.ra;

import static io.restassured.RestAssured.baseURI;
import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItems;

import org.json.JSONException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
public class CategoryControllerRA {

    @BeforeEach
    public void setup() throws Exception{
        baseURI = "http://localhost:8080";
    }

    @Test
    public void findAllCategoryShouldReturnHttpStatus200AndAllCategories() throws JSONException{
        given()
            .get("/categories")
        .then()
            .statusCode(200)
            .body("id", hasItems(1,2,3))
            .body("name", hasItems("Livros","Eletrônicos","Computadores"));
    }

    @Test
    public void findProductsByCategoryShouldReturnHttpStatus200AndProducsDTOPaged() throws JSONException{
        given()
            .get("/categories/3/products")
        .then()
            .statusCode(200)
            .body("totalElements", equalTo(24))
            .body("content.id", hasItems(4,6,7,8,9,10))
            .body("content.name", hasItems("PC Gamer","PC Gamer Ex","PC Gamer Tera","PC Gamer Weed"));
    }

}

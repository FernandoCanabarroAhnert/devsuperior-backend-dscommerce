package com.devsuperior.dscommerce.controllers.it;

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

import com.devsuperior.dscommerce.dtos.ProductDTO;
import com.devsuperior.dscommerce.factories.ProductFactory;
import com.devsuperior.dscommerce.util.TokenUtil;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Transactional
public class ProductControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TokenUtil tokenUtil;

    private ProductDTO dto;

    private String productName;

    private String adminUsername,adminPassword;
    private String clientUsername,clientPassword;
    private String bearerTokenClient,bearerTokenAdmin;

    private Long existingId, nonExistingId, dependentId;

    @BeforeEach
    public void setup() throws Exception{
        dto = new ProductDTO(ProductFactory.PROD());
        productName = "macbook";
        
        adminUsername = "alex@gmail.com";
        adminPassword = "123456";

        clientUsername = "maria@gmail.com";
        clientPassword = "123456";

        bearerTokenClient = tokenUtil.obtainAccessToken(mockMvc, clientUsername, clientPassword);
        bearerTokenAdmin = tokenUtil.obtainAccessToken(mockMvc, adminUsername, adminPassword);

        existingId = 23L;
        nonExistingId = 999L;
        dependentId = 3L;
    }

    @Test
    public void findAllByNameShouldReturnAllProductsPagedWhenNameIsBlank() throws Exception{
        mockMvc.perform(get("/products")
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").exists())
            .andExpect(jsonPath("$.totalElements").value(25))
            .andExpect(jsonPath("$.content[0].id").value(1L))
            .andExpect(jsonPath("$.content[0].name").value("The Lord of the Rings"))
            .andExpect(jsonPath("$.content[0].price").value(90.5))
            .andExpect(jsonPath("$.content[0].imgUrl").value("https://raw.githubusercontent.com/devsuperior/dscatalog-resources/master/backend/img/1-big.jpg"));
    }

    @Test
    public void findAllByNameShouldReturnPageWhenNameIsNotBlank() throws Exception{
        mockMvc.perform(get("/products?name={productName}",productName)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").exists())
            .andExpect(jsonPath("$.content[0].id").value(3L))
            .andExpect(jsonPath("$.content[0].name").value("Macbook Pro"))
            .andExpect(jsonPath("$.content[0].price").value(1250.0))
            .andExpect(jsonPath("$.content[0].imgUrl").value("https://raw.githubusercontent.com/devsuperior/dscatalog-resources/master/backend/img/3-big.jpg"));
    }

    @Test
    public void findAllWithCategoriesShouldReturnPageWhenCategoryIdsAndNameAndPageableIsNotBlank() throws Exception{
        mockMvc.perform(get("/products/categories?page=0&size=12&name=gamer&categoryIds=1,3&sort=name")
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").exists())
            .andExpect(jsonPath("$.totalElements").value(21))
            .andExpect(jsonPath("$.content[0].id").value(4L))
            .andExpect(jsonPath("$.content[0].name").value("PC Gamer"))
            .andExpect(jsonPath("$.content[0].price").value(1200.0))
            .andExpect(jsonPath("$.content[0].imgUrl").value("https://raw.githubusercontent.com/devsuperior/dscatalog-resources/master/backend/img/4-big.jpg"))
            .andExpect(jsonPath("$.content[0].categories[0].id").value(3L))
            .andExpect(jsonPath("$.content[0].categories[0].name").value("Computadores"));
    }

    @Test
    public void insertShouldReturnHttpStatus401WhenNoUserIsLogged() throws Exception{
        mockMvc.perform(post("/products")
            .content(objectMapper.writeValueAsString(dto))
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnauthorized());   
    }

    @Test
    public void insertShouldReturnHttpStatus403WhenClientIsLogged() throws Exception{
        mockMvc.perform(post("/products")
            .header("Authorization", "Bearer " + bearerTokenClient)
            .content(objectMapper.writeValueAsString(dto))
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden());
    }

   @Test
   public void insertShouldReturnHttpStatus201WhenAdminIsLoggedAndDataIsValid() throws Exception{
        mockMvc.perform(post("/products")
            .header("Authorization", "Bearer " + bearerTokenAdmin)
            .content(objectMapper.writeValueAsString(dto))
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(26L))
            .andExpect(jsonPath("$.name").value(dto.getName()))
            .andExpect(jsonPath("$.price").value(dto.getPrice()))
            .andExpect(jsonPath("$.imgUrl").value(dto.getImgUrl())); 
    }

    @Test
    public void insertShouldReturnHttpStatus422WhenAdminIsLoggedButNameIsBlank() throws Exception{
        dto.setName("      ");

        mockMvc.perform(post("/products")
            .header("Authorization", "Bearer " + bearerTokenAdmin)
            .content(objectMapper.writeValueAsString(dto))
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.errors[0].fieldName").value("name"))
	        .andExpect(jsonPath("$.errors[0].message").value("Campo Obrigatório"));
    }

    @Test
    public void insertShouldReturnHttpStatus422WhenAdminIsLoggedButNameIsTooShort() throws Exception{
        dto.setName("me");

        mockMvc.perform(post("/products")
            .header("Authorization", "Bearer " + bearerTokenAdmin)
            .content(objectMapper.writeValueAsString(dto))
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.errors[0].fieldName").value("name"))
	        .andExpect(jsonPath("$.errors[0].message").value("Nome deve ter entre 3 a 80 caracteres"));
    }

    @Test
    public void insertShouldReturnHttpStatus422WhenAdminIsLoggedButDescriptionIsBlank() throws Exception{
        dto.setDescription("                     ");

        mockMvc.perform(post("/products")
            .header("Authorization", "Bearer " + bearerTokenAdmin)
            .content(objectMapper.writeValueAsString(dto))
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.errors[0].fieldName").value("description"))
	        .andExpect(jsonPath("$.errors[0].message").value("Campo Obrigatório"));
    }

    @Test
    public void insertShouldReturnHttpStatus422WhenAdminIsLoggedButDescriptionIsTooShort() throws Exception{
        dto.setDescription("me");

        mockMvc.perform(post("/products")
            .header("Authorization", "Bearer " + bearerTokenAdmin)
            .content(objectMapper.writeValueAsString(dto))
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.errors[0].fieldName").value("description"))
	        .andExpect(jsonPath("$.errors[0].message").value("Descrição deve ter no mínimo 10 caracteres"));
    }

    @Test
    public void insertShouldReturnHttpStatus422WhenAdminIsLoggedButPriceIsNegative() throws Exception{
        dto.setPrice(-10.0);

        mockMvc.perform(post("/products")
            .header("Authorization", "Bearer " + bearerTokenAdmin)
            .content(objectMapper.writeValueAsString(dto))
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.errors[0].fieldName").value("price"))
	        .andExpect(jsonPath("$.errors[0].message").value("Preço deve ser positivo"));
    }

    @Test
    public void insertShouldReturnHttpStatus422WhenAdminIsLoggedButPriceIsZero() throws Exception{
        dto.setPrice(0.0);

        mockMvc.perform(post("/products")
            .header("Authorization", "Bearer " + bearerTokenAdmin)
            .content(objectMapper.writeValueAsString(dto))
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.errors[0].fieldName").value("price"))
	        .andExpect(jsonPath("$.errors[0].message").value("Preço deve ser positivo"));
    }

    @Test
    public void insertShouldReturnHttpStatus422WhenAdminIsLoggedButPriceIsBlank() throws Exception{
        dto.setPrice(null);

        mockMvc.perform(post("/products")
            .header("Authorization", "Bearer " + bearerTokenAdmin)
            .content(objectMapper.writeValueAsString(dto))
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.errors[0].fieldName").value("price"))
	        .andExpect(jsonPath("$.errors[0].message").value("Campo Obrigatório"));
    }

    @Test
    public void insertShouldReturnHttpStatus422WhenAdminIsLoggedButCategoriesIsEmpty() throws Exception{
        dto.getCategories().clear();

        mockMvc.perform(post("/products")
            .header("Authorization", "Bearer " + bearerTokenAdmin)
            .content(objectMapper.writeValueAsString(dto))
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.errors[0].fieldName").value("categories"))
	        .andExpect(jsonPath("$.errors[0].message").value("O produto deve ter pelo menos 1 categoria"));
    }

    @Test
    public void updateShouldReturnHttpStatus401WhenNoUserIsLogged() throws Exception{
        mockMvc.perform(put("/products/{id}",existingId)
            .content(objectMapper.writeValueAsString(dto))
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnauthorized());   
    }

    @Test
    public void updateShouldReturnHttpStatus403WhenClientIsLogged() throws Exception{
        mockMvc.perform(put("/products/{id}",existingId)
            .header("Authorization", "Bearer " + bearerTokenClient)
            .content(objectMapper.writeValueAsString(dto))
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden());
    }

    @Test
    public void updateShouldReturnHttpStatus204WhenAdminIsLoggedButIdDoesNotExist() throws Exception{
        mockMvc.perform(put("/products/{id}",nonExistingId)
            .header("Authorization", "Bearer " + bearerTokenAdmin)
            .content(objectMapper.writeValueAsString(dto))
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    @Test
    public void updateShouldReturnHttpStatus200WhenAdminIsLoggedAndIdExistsAndDataIsValid() throws Exception{
        mockMvc.perform(put("/products/{id}",existingId)
        .header("Authorization", "Bearer " + bearerTokenAdmin)
            .content(objectMapper.writeValueAsString(dto))
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(existingId))
            .andExpect(jsonPath("$.name").value(dto.getName()))
            .andExpect(jsonPath("$.price").value(dto.getPrice()))
            .andExpect(jsonPath("$.imgUrl").value(dto.getImgUrl()))
            .andExpect(jsonPath("$.categories[0].id").value(dto.getCategories().get(0).getId()))
            .andExpect(jsonPath("$.categories[0].name").value(dto.getCategories().get(0).getName()));
    }

    @Test
    public void updateShouldReturnHttpStatus422WhenAdminIsLoggedButNameIsBlank() throws Exception{
        dto.setName("      ");

        mockMvc.perform(put("/products/{id}",existingId)
            .header("Authorization", "Bearer " + bearerTokenAdmin)
            .content(objectMapper.writeValueAsString(dto))
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.errors[0].fieldName").value("name"))
	        .andExpect(jsonPath("$.errors[0].message").value("Campo Obrigatório"));
    }

    @Test
    public void updateShouldReturnHttpStatus422WhenAdminIsLoggedButNameIsTooShort() throws Exception{
        dto.setName("me");

        mockMvc.perform(put("/products/{id}",existingId)
            .header("Authorization", "Bearer " + bearerTokenAdmin)
            .content(objectMapper.writeValueAsString(dto))
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.errors[0].fieldName").value("name"))
	        .andExpect(jsonPath("$.errors[0].message").value("Nome deve ter entre 3 a 80 caracteres"));
    }

    @Test
    public void updateShouldReturnHttpStatus422WhenAdminIsLoggedButDescriptionIsBlank() throws Exception{
        dto.setDescription("                     ");

        mockMvc.perform(put("/products/{id}",existingId)
            .header("Authorization", "Bearer " + bearerTokenAdmin)
            .content(objectMapper.writeValueAsString(dto))
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.errors[0].fieldName").value("description"))
	        .andExpect(jsonPath("$.errors[0].message").value("Campo Obrigatório"));
    }

    @Test
    public void updateShouldReturnHttpStatus422WhenAdminIsLoggedButDescriptionIsTooShort() throws Exception{
        dto.setDescription("me");

        mockMvc.perform(put("/products/{id}",existingId)
            .header("Authorization", "Bearer " + bearerTokenAdmin)
            .content(objectMapper.writeValueAsString(dto))
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.errors[0].fieldName").value("description"))
	        .andExpect(jsonPath("$.errors[0].message").value("Descrição deve ter no mínimo 10 caracteres"));
    }

    @Test
    public void updateShouldReturnHttpStatus422WhenAdminIsLoggedButPriceIsNegative() throws Exception{
        dto.setPrice(-10.0);

        mockMvc.perform(put("/products/{id}",existingId)
            .header("Authorization", "Bearer " + bearerTokenAdmin)
            .content(objectMapper.writeValueAsString(dto))
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.errors[0].fieldName").value("price"))
	        .andExpect(jsonPath("$.errors[0].message").value("Preço deve ser positivo"));
    }

    @Test
    public void updateShouldReturnHttpStatus422WhenAdminIsLoggedButPriceIsZero() throws Exception{
        dto.setPrice(0.0);

        mockMvc.perform(put("/products/{id}",existingId)
            .header("Authorization", "Bearer " + bearerTokenAdmin)
            .content(objectMapper.writeValueAsString(dto))
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.errors[0].fieldName").value("price"))
	        .andExpect(jsonPath("$.errors[0].message").value("Preço deve ser positivo"));
    }

    @Test
    public void updateShouldReturnHttpStatus422WhenAdminIsLoggedButPriceIsBlank() throws Exception{
        dto.setPrice(null);

        mockMvc.perform(put("/products/{id}",existingId)
            .header("Authorization", "Bearer " + bearerTokenAdmin)
            .content(objectMapper.writeValueAsString(dto))
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.errors[0].fieldName").value("price"))
	        .andExpect(jsonPath("$.errors[0].message").value("Campo Obrigatório"));
    }

    @Test
    public void updateShouldReturnHttpStatus422WhenAdminIsLoggedButCategoriesIsEmpty() throws Exception{
        dto.getCategories().clear();

        mockMvc.perform(put("/products/{id}",existingId)
            .header("Authorization", "Bearer " + bearerTokenAdmin)
            .content(objectMapper.writeValueAsString(dto))
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.errors[0].fieldName").value("categories"))
	        .andExpect(jsonPath("$.errors[0].message").value("O produto deve ter pelo menos 1 categoria"));
    }

    @Test
    public void deleteShouldReturnHttpStatus401WhenNoUserIsLogged() throws Exception{
        mockMvc.perform(delete("/products/1")
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnauthorized());   
    }

    @Test
    public void deleteShouldReturnHttpStatus403WhenClientIsLogged() throws Exception{
        mockMvc.perform(delete("/products/1")
            .header("Authorization", "Bearer " + bearerTokenClient)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden());
    }

    @Test
    public void deleteShouldReturnHttpStatus204WhenAdminIsLoggedAndIdExists() throws Exception{
        mockMvc.perform(delete("/products/{id}",existingId)
            .header("Authorization", "Bearer " + bearerTokenAdmin)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());
    }

    @Test
    public void deleteShouldReturnHttpStatus404WhenAdminIsLoggedButIdDoesNotExist() throws Exception{
        mockMvc.perform(delete("/products/{id}",nonExistingId)
            .header("Authorization", "Bearer " + bearerTokenAdmin)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional(propagation = Propagation.SUPPORTS)
    public void deleteShouldReturnHttpStatus400WhenAdminIsLoggedButIdIsDependent() throws Exception{
        mockMvc.perform(delete("/products/{id}",dependentId)
            .header("Authorization", "Bearer " + bearerTokenAdmin)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }
}


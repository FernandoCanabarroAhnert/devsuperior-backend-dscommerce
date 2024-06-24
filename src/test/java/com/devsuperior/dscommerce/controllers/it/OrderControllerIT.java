package com.devsuperior.dscommerce.controllers.it;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.devsuperior.dscommerce.dtos.OrderDTO;
import com.devsuperior.dscommerce.entities.Order;
import com.devsuperior.dscommerce.entities.OrderItem;
import com.devsuperior.dscommerce.entities.OrderStatus;
import com.devsuperior.dscommerce.entities.Product;
import com.devsuperior.dscommerce.entities.User;
import com.devsuperior.dscommerce.factories.ProductFactory;
import com.devsuperior.dscommerce.util.TokenUtil;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Transactional
public class OrderControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TokenUtil tokenUtil;

    private String adminUsername,adminPassword;
    private String clientUsername,clientPassword;
    private String bearerTokenClient,bearerTokenAdmin;

    private Long existingId, nonExistingId, otherClientOrderId;

    private User user;
    private Order order;
    private OrderDTO dto;
    private Product product;
    private OrderItem orderItem;

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
        otherClientOrderId = 2L;

        user = new User(1L, "Maria Brown", clientUsername, "988888888", LocalDate.parse("2001-07-25"), clientPassword);
        order = new Order(null, Instant.now(), OrderStatus.WAITING_PAYMENT, user, null);
        product = ProductFactory.PROD();
        orderItem = new OrderItem(order, product, 1, product.getPrice());
        order.getItems().add(orderItem);

        dto = new OrderDTO(order);
    }

    @Test
    public void findByIdShouldReturnHttpStatus401WhenNoUserIsLogged() throws Exception{
        mockMvc.perform(get("/orders/{id}",existingId)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnauthorized());
    }

    @Test
    public void findByIdShouldReturnHttpStatus404WhenClientIsLoggedButIdDoesNotExist() throws Exception{
        mockMvc.perform(get("/orders/{id}",nonExistingId)
            .header("Authorization", "Bearer " + bearerTokenClient)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    @Test
    public void findByIdShouldReturnHttpStatus404WhenAdminIsLoggedButIdDoesNotExist() throws Exception{
        mockMvc.perform(get("/orders/{id}",nonExistingId)
            .header("Authorization", "Bearer " + bearerTokenAdmin)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    @Test
    public void findByIdShouldReturnHttpStatus200WhenAdminIsLoggedAndIdExists() throws Exception{
        mockMvc.perform(get("/orders/{id}",existingId)
            .header("Authorization", "Bearer " + bearerTokenAdmin)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(existingId))
            .andExpect(jsonPath("$.client").exists())
            .andExpect(jsonPath("$.payment").exists())
            .andExpect(jsonPath("$.items").isNotEmpty())
            .andExpect(jsonPath("$.items[0].productId").value(1L))
            .andExpect(jsonPath("$.items[1].productId").value(3L))
            .andExpect(jsonPath("$.total").value(1431.0));
    }

    @Test
    public void findByIdShouldReturnHttpStatus200WhenClientIsLoggedAndOrderBelongsToLoggedClient() throws Exception{
        mockMvc.perform(get("/orders/{id}",existingId)
            .header("Authorization", "Bearer " + bearerTokenClient)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(existingId))
            .andExpect(jsonPath("$.client").exists())
            .andExpect(jsonPath("$.payment").exists())
            .andExpect(jsonPath("$.items").isNotEmpty())
            .andExpect(jsonPath("$.items[0].productId").value(1L))
            .andExpect(jsonPath("$.items[1].productId").value(3L))
            .andExpect(jsonPath("$.total").value(1431.0));
    }

    @Test
    public void findByIdShouldReturnHttpStatus403WhenClientIsLoggedButOrderBelongsToOtherClient() throws Exception{
        mockMvc.perform(get("/orders/{id}",otherClientOrderId)
            .header("Authorization", "Bearer " + bearerTokenClient)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden());
    }

    @Test
    public void insertShouldReturnHttpStatus401WhenNoUserIsLogged() throws Exception{
        mockMvc.perform(post("/orders")
            .content(objectMapper.writeValueAsString(dto))
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnauthorized());
    }

    @Test
    public void insertShouldReturnHttpStatus201WhenClientIsLoggedAndDataIsValid() throws Exception{

            mockMvc.perform(post("/orders")
                .header("Authorization", "Bearer " + bearerTokenClient)
                .content(objectMapper.writeValueAsString(dto))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(4L))
                .andExpect(jsonPath("$.status").value("WAITING_PAYMENT"))
                .andExpect(jsonPath("$.client.id").value(1L))
                .andExpect(jsonPath("$.client.name").value("Maria Brown"))
                .andExpect(jsonPath("$.items[0].productId").value(1L));
    }

    @Test
    public void insertShouldReturnHttpStatus422WhenUserIsLoggedButItemsIsEmpty() throws Exception{
        dto.getItems().clear();

        mockMvc.perform(post("/orders")
            .header("Authorization", "Bearer " + bearerTokenClient)
            .content(objectMapper.writeValueAsString(dto))
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.errors[0].fieldName").value("items"))
            .andExpect(jsonPath("$.errors[0].message").value("O pedido deve ter pelo menos 1 item"));
    }
}

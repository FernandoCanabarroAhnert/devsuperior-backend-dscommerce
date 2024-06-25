package com.devsuperior.dscommerce.controllers.it;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Transactional
public class CategoryControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void findAllCategoryShouldReturnHttpStatus200AndAllCategories() throws Exception{
        mockMvc.perform(get("/categories")
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(1L))
            .andExpect(jsonPath("$[0].name").value("Livros"))
            .andExpect(jsonPath("$[1].id").value(2L))
            .andExpect(jsonPath("$[1].name").value("Eletrônicos"))
            .andExpect(jsonPath("$[2].id").value(3L))
            .andExpect(jsonPath("$[2].name").value("Computadores"));
    }

    @Test
    public void findProductsByCategoryShouldReturnHttpStatus200AndProducsDTOPaged() throws Exception{
        mockMvc.perform(get("/categories/3/products")
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalElements").value(23))
            .andExpect(jsonPath("$.content[0].id").value(2L))
            .andExpect(jsonPath("$.content[0].name").value("Smart TV"))
            .andExpect(jsonPath("$.content[1].id").value(3L))
            .andExpect(jsonPath("$.content[1].name").value("Macbook Pro"))
            .andExpect(jsonPath("$.content[2].id").value(4L))
            .andExpect(jsonPath("$.content[2].name").value("PC Gamer"));
    }
}

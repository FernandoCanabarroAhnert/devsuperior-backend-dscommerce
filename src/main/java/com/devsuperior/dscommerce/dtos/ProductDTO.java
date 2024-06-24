package com.devsuperior.dscommerce.dtos;

import java.util.ArrayList;
import java.util.List;

import com.devsuperior.dscommerce.entities.Category;
import com.devsuperior.dscommerce.entities.Product;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class ProductDTO{
    
    private Long id;

    @Size(min = 3,max = 80,message = "Nome deve ter entre 3 a 80 caracteres")
    @NotBlank(message = "Campo Obrigatório")
    private String name;

    @Size(min = 10,message = "Descrição deve ter no mínimo 10 caracteres")
    @NotBlank(message = "Campo Obrigatório")
    @Column(columnDefinition = "TEXT")
    private String description;

    @NotNull(message = "Campo Obrigatório")
    @Positive(message = "Preço deve ser positivo")
    private Double price;

    private String imgUrl;

    @NotEmpty(message = "O produto deve ter pelo menos 1 categoria")
    private List<CategoryDTO> categories = new ArrayList<>();

    public ProductDTO(Long id,
            @Size(min = 3, max = 80, message = "Nome deve ter entre 3 a 80 caracteres") @NotBlank(message = "Campo Obrigatório") String name,
            @Size(min = 10, message = "Descrição deve ter no mínimo 10 caracteres") @NotBlank(message = "Campo Obrigatório") String description,
            @Positive(message = "Preço deve ser positivo") Double price, String imgUrl) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.imgUrl = imgUrl;
    }

    public ProductDTO(Product entity){
        id = entity.getId();
        name = entity.getName();
        description = entity.getDescription();
        price = entity.getPrice();
        imgUrl = entity.getImgUrl();
        for (Category c : entity.getCategories()){
            categories.add(new CategoryDTO(c));
        }
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Double getPrice() {
        return price;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public List<CategoryDTO> getCategories() {
        return categories;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public void setCategories(List<CategoryDTO> categories) {
        this.categories = categories;
    }

    

    
    
}

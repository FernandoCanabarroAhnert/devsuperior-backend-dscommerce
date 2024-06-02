package com.devsuperior.dscommerce.dtos;

import com.devsuperior.dscommerce.entities.Product;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record ProductDTO(
    
    Long id,

    @Size(min = 3,max = 80,message = "Nome deve ter entre 3 a 80 caracteres")
    @NotBlank(message = "Campo Obrigatório")
    String name,

    @Size(min = 10,message = "Descrição deve ter no mínimo 10 caracteres")
    @NotBlank(message = "Campo Obrigatório")
    @Column(columnDefinition = "TEXT")
    String description,

    @Positive(message = "Preço deve ser positivo")
    Double price,

    String imgUrl) {

    public ProductDTO(Product product){
        this(product.getId(),product.getName(),product.getDescription(),product.getPrice(),product.getImgUrl());
    }
}

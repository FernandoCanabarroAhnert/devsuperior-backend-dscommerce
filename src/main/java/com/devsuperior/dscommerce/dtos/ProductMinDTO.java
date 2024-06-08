package com.devsuperior.dscommerce.dtos;

import com.devsuperior.dscommerce.entities.Product;
import com.devsuperior.dscommerce.projections.ProductMinProjection;

public class ProductMinDTO {

    private Long id;
    private String name;
    private Double price;
    private String imgUrl;

    public ProductMinDTO(Long id, String name, Double price, String imgUrl) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.imgUrl = imgUrl;
    }

    public ProductMinDTO(Product entity){
        id = entity.getId();
        name = entity.getName();
        price = entity.getPrice();
        imgUrl = entity.getImgUrl();
    }

    public ProductMinDTO(ProductMinProjection projection){
        id = projection.getId();
        name = projection.getName();
        price = projection.getPrice();
        imgUrl = projection.getImgUrl();
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Double getPrice() {
        return price;
    }

    public String getImgUrl() {
        return imgUrl;
    }
    

    

}

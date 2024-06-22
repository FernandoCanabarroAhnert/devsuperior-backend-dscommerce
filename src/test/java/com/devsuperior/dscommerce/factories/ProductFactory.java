package com.devsuperior.dscommerce.factories;

import static com.devsuperior.dscommerce.factories.CategoryFactory.CAT;

import com.devsuperior.dscommerce.entities.Product;

public class ProductFactory {

    public static Product PROD(){
        Product prod = new Product(1L, "product", "description", 10.0, "url");
        prod.getCategories().add(CAT());
        return prod;
    }
}

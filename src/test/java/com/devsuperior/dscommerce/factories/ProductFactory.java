package com.devsuperior.dscommerce.factories;

import static com.devsuperior.dscommerce.factories.CategoryFactory.CAT;

import com.devsuperior.dscommerce.entities.Product;

public class ProductFactory {

    public static Product PROD(){
        Product prod = new Product(1L, "The Lord of the Rings", "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.", 90.5, "https://raw.githubusercontent.com/devsuperior/dscatalog-resources/master/backend/img/1-big.jpg");
        prod.getCategories().add(CAT());
        return prod;
    }
}

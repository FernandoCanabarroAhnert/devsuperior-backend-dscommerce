package com.devsuperior.dscommerce.factories;

import com.devsuperior.dscommerce.entities.Category;

public class CategoryFactory {

    public static Category CAT(){
        return new Category(1L,"cat");
    }
}

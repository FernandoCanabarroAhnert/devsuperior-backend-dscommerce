package com.devsuperior.dscommerce.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.devsuperior.dscommerce.dtos.CategoryDTO;
import com.devsuperior.dscommerce.dtos.ProductMinDTO;
import com.devsuperior.dscommerce.services.CategoryService;

@RestController
@RequestMapping("/categories")
public class CategoryController {

    @Autowired
    private CategoryService service;

    @GetMapping
    public ResponseEntity<List<CategoryDTO>> findAll(){
        List<CategoryDTO> list = service.findAll();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}/products")
    public ResponseEntity<Page<ProductMinDTO>> findProductsByCategory(@PathVariable Long id,Pageable pageable){
        Page<ProductMinDTO> list = service.findProductsByCategory(id, pageable);
        return ResponseEntity.ok(list);
    }
}

package com.devsuperior.dscommerce.services;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.devsuperior.dscommerce.controllers.CategoryController;
import com.devsuperior.dscommerce.controllers.ProductController;
import com.devsuperior.dscommerce.dtos.CategoryDTO;
import com.devsuperior.dscommerce.dtos.ProductMinDTO;
import com.devsuperior.dscommerce.repositories.CategoryRepository;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository repository;

    @Transactional(readOnly = true)
    public List<CategoryDTO> findAll(){
        return repository.findAll().stream().map(x -> new CategoryDTO(x).
            add(linkTo(methodOn(CategoryController.class).
            findProductsByCategory(x.getId(),null)).withRel("Consultar Produtos da Categoria " + x.getName()))
            .add(linkTo(methodOn(CategoryController.class).findAll()).withSelfRel())).toList();
    }

    @Transactional(readOnly = true)
    public Page<ProductMinDTO> findProductsByCategory(Long id,Pageable pageable){
        return repository.findProductsByCategory(id, pageable).map(x -> new ProductMinDTO(x).
            add(linkTo(methodOn(ProductController.class).
            findById(x.getId())).withRel("Consultar Produto por Id"))
            .add(linkTo(methodOn(CategoryController.class).findProductsByCategory(id, pageable))
            .withSelfRel()));
    }
}

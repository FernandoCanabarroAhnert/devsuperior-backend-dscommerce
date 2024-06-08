package com.devsuperior.dscommerce.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.devsuperior.dscommerce.dtos.CategoryDTO;
import com.devsuperior.dscommerce.dtos.ProductMinDTO;
import com.devsuperior.dscommerce.repositories.CategoryRepository;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository repository;

    @Transactional(readOnly = true)
    public List<CategoryDTO> findAll(){
        return repository.findAll().stream().map(CategoryDTO::new).toList();
    }

    @Transactional(readOnly = true)
    public Page<ProductMinDTO> findProductsByCategory(Long id,Pageable pageable){
        return repository.findProductsByCategory(id, pageable).map(ProductMinDTO::new);
    }
}

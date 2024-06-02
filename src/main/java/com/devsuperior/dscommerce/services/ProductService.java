package com.devsuperior.dscommerce.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.devsuperior.dscommerce.dtos.ProductDTO;
import com.devsuperior.dscommerce.entities.Product;
import com.devsuperior.dscommerce.repositories.ProductRepository;

@Service
public class ProductService {

    @Autowired
    private ProductRepository repository;

    @Transactional(readOnly = true)
    public ProductDTO findById(Long id){
        Product obj = repository.findById(id).get();
        return new ProductDTO(obj);
    }

    @Transactional(readOnly = true)
    public Page<ProductDTO> findAll(Pageable pageable){
        return repository.findAll(pageable).map(ProductDTO::new);
    }

    @Transactional
    public ProductDTO insert(ProductDTO obj){
        Product product = repository.save(new Product(obj));
        return new ProductDTO(product);
    }

    @Transactional
    public ProductDTO update(Long id,ProductDTO obj){
        Product entity = repository.getReferenceById(id);
        updateData(entity,obj);
        repository.save(entity);
        return new ProductDTO(entity);
    }

    private void updateData(Product entity, ProductDTO obj) {
        entity.setName(obj.name());
        entity.setDescription(obj.description());
        entity.setPrice(obj.price());
        entity.setImgUrl(obj.imgUrl());
    }

    @Transactional
    public void deleteById(Long id){
        repository.deleteById(id);
    }
}

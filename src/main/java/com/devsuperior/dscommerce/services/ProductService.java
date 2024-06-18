package com.devsuperior.dscommerce.services;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.devsuperior.dscommerce.dtos.CategoryDTO;
import com.devsuperior.dscommerce.dtos.ProductDTO;
import com.devsuperior.dscommerce.dtos.ProductMinDTO;
import com.devsuperior.dscommerce.entities.Category;
import com.devsuperior.dscommerce.entities.Product;
import com.devsuperior.dscommerce.projections.ProductProjection;
import com.devsuperior.dscommerce.repositories.CategoryRepository;
import com.devsuperior.dscommerce.repositories.ProductRepository;
import com.devsuperior.dscommerce.services.exceptions.DatabaseException;
import com.devsuperior.dscommerce.services.exceptions.ResourceNotFoundException;
import com.devsuperior.dscommerce.util.Utills;

import jakarta.persistence.EntityNotFoundException;

@Service
public class ProductService {

    @Autowired
    private ProductRepository repository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public ProductDTO findById(Long id){
        Product obj = repository.findById(id).orElseThrow(() ->
        new ResourceNotFoundException(id));
        return new ProductDTO(obj);
    }

    @Transactional(readOnly = true)
    public Page<ProductMinDTO> findAll(String name,Pageable pageable){
        return repository.searchByName(name,pageable).map(ProductMinDTO::new);
    }

    @SuppressWarnings("unchecked")
    @Transactional(readOnly = true)
    public Page<ProductDTO> searchProductsWithCategories(String categoryIds,String name,Pageable pageable){
        List<Long> ids = (!categoryIds.equals("0")) ? Arrays.stream(categoryIds.split(",")).map(Long::parseLong).toList()
                                                    : Arrays.asList();
        Page<ProductProjection> page = repository.searchProducts(ids, name.trim(), pageable);

        List<Long> productIds = page.map(ProductProjection::getId).toList();
        List<Product> entities = repository.searchProductsWithCategories(productIds);
        entities = (List<Product>) Utills.replace(page.getContent(), entities);

        List<ProductDTO> dtos = entities.stream().map(ProductDTO::new).toList();
        return new PageImpl<>(dtos,page.getPageable(),page.getTotalElements());
    }

    @Transactional
    public ProductDTO insert(ProductDTO obj){
        Product entity = new Product();
        entity.setName(obj.getName());
        entity.setDescription(obj.getDescription());
        entity.setPrice(obj.getPrice());
        entity.setImgUrl(obj.getImgUrl());
        for (CategoryDTO c : obj.getCategories()){
            Category cat = categoryRepository.getReferenceById(c.getId());
            entity.getCategories().add(cat);
        }
        entity = repository.save(entity);
        return new ProductDTO(entity);
    }

    @Transactional
    public ProductDTO update(Long id,ProductDTO obj){
        try{
            Product entity = repository.getReferenceById(id);
            updateData(entity,obj);
            repository.save(entity);
            return new ProductDTO(entity);
        }
        catch(EntityNotFoundException e){
            throw new ResourceNotFoundException(id);
        }
    }

    private void updateData(Product entity, ProductDTO obj) {
        entity.setName(obj.getName());
        entity.setDescription(obj.getDescription());
        entity.setPrice(obj.getPrice());
        entity.setImgUrl(obj.getImgUrl());
        entity.getCategories().clear();
        for (CategoryDTO c : obj.getCategories()){
            Category cat = new Category();
            cat.setId(c.getId());
            cat.setName(c.getName());
            entity.getCategories().add(cat);
        }
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    public void delete(Long id) {
	    if (!repository.existsById(id)) {
		    throw new ResourceNotFoundException(id);
	    }
	    try {
        	repository.deleteById(id);    		
	    }
    	catch (DataIntegrityViolationException e) {
        	throw new DatabaseException("Falha de integridade referencial");
   	    }
    }
}

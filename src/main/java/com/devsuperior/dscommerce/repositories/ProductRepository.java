package com.devsuperior.dscommerce.repositories;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.devsuperior.dscommerce.entities.Product;
import com.devsuperior.dscommerce.projections.ProductProjection;

@Repository
public interface ProductRepository extends JpaRepository<Product,Long>{

    @Query("SELECT obj FROM Product obj WHERE UPPER(obj.name) LIKE UPPER(CONCAT('%',:name,'%'))")
    Page<Product> searchByName(String name,Pageable pageable);

    @Query(nativeQuery = true,value = 
        "SELECT * FROM ( "
        + "SELECT DISTINCT p.id,p.name "
        + "FROM products AS p "
        + "INNER JOIN product_category AS pc ON pc.product_id = p.id "
        + "WHERE (:categoryIds IS NULL OR pc.category_id IN :categoryIds) "
        + "AND LOWER(p.name) LIKE LOWER(CONCAT('%',:name,'%')) "
        + ") AS result ",
        countQuery = 
        "SELECT COUNT(*) FROM ( "
        + "SELECT DISTINCT p.id,p.name "
        + "FROM products AS p "
        + "INNER JOIN product_category AS pc ON pc.product_id = p.id "
        + "WHERE (:categoryIds IS NULL OR pc.category_id IN :categoryIds) "
        + "AND LOWER(p.name) LIKE LOWER(CONCAT('%',:name,'%')) "
        + ") AS result "
    )
    Page<ProductProjection> searchProducts(List<Long> categoryIds,String name,Pageable pageable);

    @Query("SELECT obj FROM Product obj JOIN FETCH obj.categories WHERE obj.id IN :productIds")
    List<Product> searchProductsWithCategories(List<Long> productIds);
}

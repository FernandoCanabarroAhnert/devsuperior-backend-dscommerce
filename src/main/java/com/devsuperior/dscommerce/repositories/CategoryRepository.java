package com.devsuperior.dscommerce.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.devsuperior.dscommerce.entities.Category;
import com.devsuperior.dscommerce.projections.ProductMinProjection;

@Repository
public interface CategoryRepository extends JpaRepository<Category,Long>{

    @Query(nativeQuery = true,value = """
            SELECT p.id,p.name,p.price,p.img_url AS imgUrl
            FROM products AS p
            INNER JOIN product_category AS pc ON pc.product_id = p.id
            INNER JOIN categories AS c ON pc.category_id = c.id
            WHERE c.id = :id
            """)
    Page<ProductMinProjection> findProductsByCategory(Long id,Pageable pageable);
}

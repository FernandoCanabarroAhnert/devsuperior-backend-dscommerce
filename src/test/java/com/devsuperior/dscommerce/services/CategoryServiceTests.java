package com.devsuperior.dscommerce.services;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.devsuperior.dscommerce.dtos.CategoryDTO;
import com.devsuperior.dscommerce.dtos.ProductMinDTO;
import com.devsuperior.dscommerce.entities.Category;
import com.devsuperior.dscommerce.projections.ProductMinProjection;
import com.devsuperior.dscommerce.repositories.CategoryRepository;

import static com.devsuperior.dscommerce.factories.CategoryFactory.CAT;


@ExtendWith(MockitoExtension.class)
public class CategoryServiceTests {

    @InjectMocks
    private CategoryService service;

    @Mock
    private CategoryRepository repository;

    private Category cat;

    private ProductMinProjection projection;

    @BeforeEach
    public void setup() throws Exception{
        cat = CAT();

        projection = new ProductMinProjection() {
            @Override
            public Long getId() {
                return 1L;
            }

            @Override
            public String getName() {
                return "product";
            }

            @Override
            public Double getPrice() {
                return 10.0;
            }

            @Override
            public String getImgUrl() {
                return "url";
            }
        };
    }

    @Test
    public void findAllShouldReturnCategoryDTOList(){
        when(repository.findAll()).thenReturn(List.of(cat));

        List<CategoryDTO> sut = service.findAll();

        assertThat(sut).isNotEmpty();
        assertThat(sut.get(0).getName()).isEqualTo(cat.getName());
    }

    @Test
    public void testFindProductsByCategory() {
        Page<ProductMinProjection> productPage = new PageImpl<>(List.of(projection));
        Pageable pageable = PageRequest.of(0, 10);

        when(repository.findProductsByCategory(eq(1L), any(Pageable.class))).thenReturn(productPage);

        Page<ProductMinDTO> result = service.findProductsByCategory(1L, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("product");
        assertThat(result.getContent().get(0).getPrice()).isEqualTo(10.0);
        assertThat(result.getContent().get(0).getImgUrl()).isEqualTo("url");
    }

}

package com.devsuperior.dscommerce.services;

import static com.devsuperior.dscommerce.factories.CategoryFactory.CAT;
import static com.devsuperior.dscommerce.factories.ProductFactory.PROD;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.devsuperior.dscommerce.dtos.ProductDTO;
import com.devsuperior.dscommerce.dtos.ProductMinDTO;
import com.devsuperior.dscommerce.entities.Category;
import com.devsuperior.dscommerce.entities.Product;
import com.devsuperior.dscommerce.projections.ProductProjection;
import com.devsuperior.dscommerce.repositories.CategoryRepository;
import com.devsuperior.dscommerce.repositories.ProductRepository;
import com.devsuperior.dscommerce.services.exceptions.DatabaseException;
import com.devsuperior.dscommerce.services.exceptions.ResourceNotFoundException;

import jakarta.persistence.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTests {

    @InjectMocks
    private ProductService service;

    @Mock
    private ProductRepository repository;

    @Mock
    private CategoryRepository categoryRepository;

    private Long existingId;
    private Long nonExistingId;
    private Long dependentId;

    private Product prod;
    private ProductDTO dto;
    private Category cat;

    private ProductProjection projection;

    @BeforeEach
    public void setup() throws Exception{
        existingId = 1L;
        nonExistingId = 2L;
        dependentId = 3L;

        prod = PROD();
        dto = new ProductDTO(prod);
        cat = CAT();

        projection = new ProductProjection() {
            @Override
            public Long getId(){
                return 1L;
            }

            @Override
            public String getName(){
                return "projection";
            }
        };
    }

    @Test
    public void findByIdShouldReturnProductDTOWhenIdExists(){
        when(repository.findById(existingId)).thenReturn(Optional.of(prod));

        ProductDTO sut = service.findById(existingId);

        assertThat(sut).isNotNull();
        assertThat(sut.getId()).isEqualTo(existingId);
        assertThat(sut.getName()).isEqualTo(prod.getName());
        assertThat(sut.getDescription()).isEqualTo(prod.getDescription());
        assertThat(sut.getPrice()).isEqualTo(prod.getPrice());
        assertThat(sut.getImgUrl()).isEqualTo(prod.getImgUrl());
    }

    @Test
    public void findByIdShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist(){
        when(repository.findById(nonExistingId)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.findById(nonExistingId)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    public void findAllShouldReturnAllProductMinDTOPaged(){
        Page<Product> page = new PageImpl<>(List.of(prod));
        Pageable pageable = PageRequest.of(0, 10);
        String name = "prod";

        when(repository.searchByName(name,pageable)).thenReturn(page);

        Page<ProductMinDTO> sut = service.findAll(name, pageable);

        assertThat(sut).isNotEmpty();
        assertThat(sut.getContent().get(0).getId()).isEqualTo(prod.getId());
        assertThat(sut.getContent().get(0).getName()).isEqualTo(prod.getName());
        assertThat(sut.getContent().get(0).getPrice()).isEqualTo(prod.getPrice());
        assertThat(sut.getContent().get(0).getImgUrl()).isEqualTo(prod.getImgUrl());
    }

    @Test
    public void findAllShouldReturnEmptyPageWhenNameDoesNotExist(){
        Page<Product> page = new PageImpl<>(List.of());
        Pageable pageable = PageRequest.of(0, 10);
        String name = "isdfhoifhasiohgdsiogai";

        when(repository.searchByName(name,pageable)).thenReturn(page);

        Page<ProductMinDTO> sut = service.findAll(name, pageable);

        assertThat(sut).isEmpty();
    }

    @Test
    public void searchProductsWithCategoriesShouldReturnPagedProductDTOWhenCategoryIdIsNotBlank(){
        List<Long> ids = Arrays.asList(1L,2L,3L);
        Pageable pageable = PageRequest.of(0, 10);
        String name = "ma";

        Page<ProductProjection> page = new PageImpl<>(List.of(projection));

        when(repository.searchProducts(ids, name.trim(), pageable)).thenReturn(page);
        when(repository.searchProductsWithCategories(Arrays.asList(1L))).thenReturn(List.of(prod));

        Page<ProductDTO> sut = service.searchProductsWithCategories("1,2,3", name, pageable);

        assertThat(sut).isNotEmpty();
        assertThat(sut.getContent().get(0).getName()).isEqualTo(dto.getName());
        assertThat(sut.getContent().get(0).getPrice()).isEqualTo(dto.getPrice());
        assertThat(sut.getContent().get(0).getDescription()).isEqualTo(dto.getDescription());
        assertThat(sut.getContent().get(0).getImgUrl()).isEqualTo(dto.getImgUrl());
        assertThat(sut.getContent().get(0).getCategories().get(0).getName()).isEqualTo(dto.getCategories().get(0).getName());
    }

    @Test
    public void searchProductsWithCategoriesShouldReturnPagedProductDTOWhenCategoryIdsIsBlank(){
        List<Long> ids = Arrays.asList();
        Pageable pageable = PageRequest.of(0, 10);
        String name = "ma";

        Page<ProductProjection> page = new PageImpl<>(List.of(projection));

        when(repository.searchProducts(ids, name.trim(), pageable)).thenReturn(page);
        when(repository.searchProductsWithCategories(Arrays.asList(1L))).thenReturn(List.of(prod));

        Page<ProductDTO> sut = service.searchProductsWithCategories("0", name.trim(), pageable);

        assertThat(sut).isNotEmpty();
        assertThat(sut.getContent().get(0).getName()).isEqualTo(dto.getName());
        assertThat(sut.getContent().get(0).getPrice()).isEqualTo(dto.getPrice());
        assertThat(sut.getContent().get(0).getDescription()).isEqualTo(dto.getDescription());
        assertThat(sut.getContent().get(0).getImgUrl()).isEqualTo(dto.getImgUrl());
        assertThat(sut.getContent().get(0).getCategories().get(0).getName()).isEqualTo(dto.getCategories().get(0).getName());
    }

    @Test
    public void insertShouldReturnNewProductDTOWhenDataIsValid(){
        when(repository.save(any(Product.class))).thenReturn(prod);
        when(categoryRepository.getReferenceById(existingId)).thenReturn(cat);

        ProductDTO sut = service.insert(dto);

        assertThat(sut).isNotNull();
        assertThat(sut.getId()).isEqualTo(existingId);
        assertThat(sut.getName()).isEqualTo(dto.getName());
        assertThat(sut.getDescription()).isEqualTo(dto.getDescription());
        assertThat(sut.getPrice()).isEqualTo(dto.getPrice());
        assertThat(sut.getImgUrl()).isEqualTo(dto.getImgUrl());
        assertThat(sut.getCategories().get(0).getId()).isEqualTo(cat.getId());
        assertThat(sut.getCategories().get(0).getName()).isEqualTo(cat.getName());
    }

    @Test
    public void updateShouldReturnUpdatedProductDTOWhenIdExists(){
        Product update = new Product(null, "update", "updateprod", 15.0, "updateurl");
        Category updateCat = new Category(2L, "cat2");
        update.getCategories().add(updateCat);
        ProductDTO updateDTO = new ProductDTO(update);

        when(repository.getReferenceById(existingId)).thenReturn(prod);
        when(repository.save(any(Product.class))).thenReturn(update);

        ProductDTO result = service.update(existingId, updateDTO);

        assertThat(result.getId()).isEqualTo(existingId);
        assertThat(result.getName()).isEqualTo(update.getName());
        assertThat(result.getDescription()).isEqualTo(update.getDescription());
        assertThat(result.getPrice()).isEqualTo(update.getPrice());
        assertThat(result.getImgUrl()).isEqualTo(update.getImgUrl());
        assertThat(result.getCategories().get(0).getId()).isEqualTo(updateCat.getId());
        assertThat(result.getCategories().get(0).getName()).isEqualTo(updateCat.getName());
    }

    @Test
    public void updateShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist(){
        when(repository.getReferenceById(nonExistingId)).thenThrow(new EntityNotFoundException());

        assertThrows(ResourceNotFoundException.class, () -> {
            service.update(nonExistingId, dto);
        });
    }

    @Test
    public void deleteByIdShouldThrowNoExceptionWhenIdExists(){
        when(repository.existsById(existingId)).thenReturn(true);
        doNothing().when(repository).deleteById(existingId);

        assertThatCode(() -> service.delete(existingId)).doesNotThrowAnyException();
    }

    @Test
    public void deleteByIdShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist(){
        when(repository.existsById(nonExistingId)).thenReturn(false);

        assertThatThrownBy(() -> service.delete(nonExistingId)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    public void deleteShouldThrowDatabaseExceptionWhenIdIsDependent(){
        when(repository.existsById(dependentId)).thenReturn(true);
        doThrow(DataIntegrityViolationException.class).when(repository).deleteById(dependentId);

        assertThatThrownBy(() -> service.delete(dependentId)).isInstanceOf(DatabaseException.class);
    }


}


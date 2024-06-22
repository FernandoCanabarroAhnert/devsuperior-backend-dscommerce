package com.devsuperior.dscommerce.services;

import static com.devsuperior.dscommerce.factories.UserFactory.ADMIN;
import static com.devsuperior.dscommerce.factories.UserFactory.CLIENT;
import static com.devsuperior.dscommerce.factories.OrderFactory.ORDER;
import static com.devsuperior.dscommerce.factories.ProductFactory.PROD;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.devsuperior.dscommerce.dtos.OrderDTO;
import com.devsuperior.dscommerce.entities.Order;
import com.devsuperior.dscommerce.entities.OrderItem;
import com.devsuperior.dscommerce.entities.Product;
import com.devsuperior.dscommerce.entities.User;
import com.devsuperior.dscommerce.repositories.OrderItemRepository;
import com.devsuperior.dscommerce.repositories.OrderRepository;
import com.devsuperior.dscommerce.repositories.ProductRepository;
import com.devsuperior.dscommerce.services.exceptions.ForbiddenException;
import com.devsuperior.dscommerce.services.exceptions.ResourceNotFoundException;

import jakarta.persistence.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTests {

    @InjectMocks
    private OrderService service;

    @Mock
    private OrderRepository repository;

    @Mock
    private AuthService authService;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private UserService userService;

    private Long existingId;
    private Long nonExistingId;

    private Order order;
    private OrderDTO dto;

    private User client;
    private User admin;

    private Product product;

    @BeforeEach
    public void setup() throws Exception{
        existingId = 1L;
        nonExistingId = 2L;

        client = CLIENT();
        admin = ADMIN();

        order = ORDER();
        dto = new OrderDTO(order);

        product = PROD();
    }

    @Test
    public void findByIdShouldReturnOrderDTOWhenIdExistsAndAdminIsLogged(){
        when(repository.findById(existingId)).thenReturn(Optional.of(order));
        doNothing().when(authService).validateSelfOrAdmin(anyLong());

        OrderDTO result = service.findById(existingId);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(existingId);
        assertThat(result.getClient().getId()).isEqualTo(dto.getClient().getId());
    }

    @Test
    public void findByIdShouldDoNothingWhenIdExistsAndSelfClientIsLogged(){
        when(repository.findById(existingId)).thenReturn(Optional.of(order));
        doNothing().when(authService).validateSelfOrAdmin(anyLong());

        OrderDTO result = service.findById(existingId);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(existingId);
    }

    @Test
    public void findByIdShouldThrowForbiddenExceptionWhenIdExistsButLoggedUserIsNotAdminOrSelf(){
        when(repository.findById(existingId)).thenReturn(Optional.of(order));
        doThrow(ForbiddenException.class).when(authService).validateSelfOrAdmin(anyLong());

        assertThatThrownBy(() -> service.findById(existingId)).isInstanceOf(ForbiddenException.class);
    }

    @Test
    public void findByIdShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist(){
        when(repository.findById(nonExistingId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(nonExistingId)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    public void insertShouldReturnOrderDTOWhenClientIsLogged(){
        when(userService.authenticated()).thenReturn(client);
        when(productRepository.getReferenceById(existingId)).thenReturn(product);
        when(repository.save(any(Order.class))).thenReturn(order);
        when(orderItemRepository.saveAll(any())).thenReturn(new ArrayList<>(order.getItems()));
        
        OrderDTO result = service.insert(dto);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(dto.getId());
        assertThat(result.getClient().getId()).isEqualTo(dto.getClient().getId());
    }

    @Test
    public void insertShouldReturnOrderDTOWhenAdminIsLogged(){
        when(userService.authenticated()).thenReturn(admin);
        when(productRepository.getReferenceById(existingId)).thenReturn(product);
        when(repository.save(any(Order.class))).thenReturn(order);
        when(orderItemRepository.saveAll(any())).thenReturn(new ArrayList<>(order.getItems()));
        
        OrderDTO result = service.insert(dto);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(dto.getId());
        assertThat(result.getClient().getId()).isEqualTo(dto.getClient().getId());
    }

    @Test
    public void insertShouldReturnUsernameNotFoundExceptionWhenNoUserIsLogged(){
        doThrow(UsernameNotFoundException.class).when(userService).authenticated();

        assertThatThrownBy(() -> service.insert(dto)).isInstanceOf(UsernameNotFoundException.class);
    }

    @Test
    public void insertShouldThrowEntityNotFoundExceptionWhenProductIdDoesNotExist(){
        when(userService.authenticated()).thenReturn(client);
        when(productRepository.getReferenceById(nonExistingId)).thenThrow(EntityNotFoundException.class);

        product.setId(nonExistingId);
        OrderItem oi = new OrderItem(order, product, 1, product.getPrice());
        order.getItems().clear();
        order.getItems().add(oi);
        dto = new OrderDTO(order);

        assertThatThrownBy(() -> service.insert(dto)).isInstanceOf(EntityNotFoundException.class);
    }

}

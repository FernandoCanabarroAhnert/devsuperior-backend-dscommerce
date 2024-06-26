package com.devsuperior.dscommerce.dtos;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.devsuperior.dscommerce.entities.Order;
import com.devsuperior.dscommerce.entities.OrderItem;
import com.devsuperior.dscommerce.entities.OrderStatus;

import jakarta.validation.constraints.NotEmpty;

public class OrderDTO {

    private Long id;

    private Instant moment;

    private OrderStatus status;

    private ClientDTO client;

    private PaymentDTO payment;

    @NotEmpty(message = "O pedido deve ter pelo menos 1 item")
    private List<OrderItemDTO> items = new ArrayList<>();

    public OrderDTO(Long id, Instant moment, OrderStatus status, ClientDTO client, PaymentDTO payment) {
        this.id = id;
        this.moment = moment;
        this.status = status;
        this.client = client;
        this.payment = payment;
    }

    public OrderDTO(Order entity){
        id = entity.getId();
        moment = entity.getMoment();
        status = entity.getStatus();
        client = new ClientDTO(entity.getClient());
        payment = (entity.getPayment() == null) ? null : new PaymentDTO(entity.getPayment());
        for (OrderItem x : entity.getItems()){
            items.add(new OrderItemDTO(x));
        }
    }

    public Long getId() {
        return id;
    }

    public Instant getMoment() {
        return moment;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public ClientDTO getClient() {
        return client;
    }

    public PaymentDTO getPayment() {
        return payment;
    }

    public List<OrderItemDTO> getItems() {
        return items;
    }

    public double getTotal(){
        return items.stream().map(OrderItemDTO::getSubTotal).reduce(0.0, Double::sum);
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setMoment(Instant moment) {
        this.moment = moment;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public void setClient(ClientDTO client) {
        this.client = client;
    }

    public void setPayment(PaymentDTO payment) {
        this.payment = payment;
    }

    public void setItems(List<OrderItemDTO> items) {
        this.items = items;
    }

    
}

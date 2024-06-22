package com.devsuperior.dscommerce.factories;

import static com.devsuperior.dscommerce.factories.UserFactory.CLIENT;
import static com.devsuperior.dscommerce.factories.ProductFactory.PROD;

import java.time.Instant;

import com.devsuperior.dscommerce.entities.Order;
import com.devsuperior.dscommerce.entities.OrderItem;
import com.devsuperior.dscommerce.entities.OrderStatus;
import com.devsuperior.dscommerce.entities.Product;
import com.devsuperior.dscommerce.entities.User;

public class OrderFactory {

    public static Order ORDER(){
        User client = CLIENT();
        Order order = new Order(1L, Instant.now(), OrderStatus.WAITING_PAYMENT, client, null);

        Product prod = PROD();
        OrderItem oi = new OrderItem(order, prod, 1, prod.getPrice());
        order.getItems().add(oi);
        return order;
    }
}

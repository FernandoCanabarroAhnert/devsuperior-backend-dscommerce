package com.devsuperior.dscommerce.factories;

import java.time.LocalDate;

import com.devsuperior.dscommerce.entities.User;

import static com.devsuperior.dscommerce.factories.RoleFactory.ROLE_ADMIN;
import static com.devsuperior.dscommerce.factories.RoleFactory.ROLE_CLIENT;

public class UserFactory {

    public static User CLIENT(){
        User user = new User(1L, "Maria Brown", "maria@gmail.com", "988888888", LocalDate.parse("2001-07-25"), "12345678");
        user.addRole(ROLE_CLIENT());
        return user;
    }

    public static User ADMIN(){
        User user = new User(2L, "Alex Green", "alex@gmail.com", "977777777", LocalDate.parse("1987-12-13"), "12345678");
        user.addRole(ROLE_ADMIN());
        return user;
    }
}

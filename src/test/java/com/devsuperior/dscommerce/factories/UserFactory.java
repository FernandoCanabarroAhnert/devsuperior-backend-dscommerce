package com.devsuperior.dscommerce.factories;

import java.time.LocalDate;

import com.devsuperior.dscommerce.entities.User;

import static com.devsuperior.dscommerce.factories.RoleFactory.ROLE_ADMIN;
import static com.devsuperior.dscommerce.factories.RoleFactory.ROLE_CLIENT;

public class UserFactory {

    public static User CLIENT(){
        User user = new User(1L, "client", "client@gmail.com", "12345678910", LocalDate.parse("2005-10-28"), "123456");
        user.addRole(ROLE_CLIENT());
        return user;
    }

    public static User ADMIN(){
        User user = new User(2L, "admin", "admin@gmail.com", "12345678910", LocalDate.parse("2005-10-28"), "123456");
        user.addRole(ROLE_ADMIN());
        return user;
    }
}

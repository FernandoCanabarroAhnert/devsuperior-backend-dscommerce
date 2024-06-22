package com.devsuperior.dscommerce.factories;

import com.devsuperior.dscommerce.entities.Role;

public class RoleFactory {

    public static Role ROLE_CLIENT(){
        return new Role(1L, "ROLE_CLIENT");
    }

    public static Role ROLE_ADMIN(){
        return new Role(2L, "ROLE_ADMIN");
    }
}

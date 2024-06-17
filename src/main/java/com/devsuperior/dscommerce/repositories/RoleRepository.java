package com.devsuperior.dscommerce.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.devsuperior.dscommerce.entities.Role;

public interface RoleRepository extends JpaRepository<Role,Long>{

    Role findByAuthority(String authority);

}

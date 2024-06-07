package com.devsuperior.dscommerce.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.devsuperior.dscommerce.entities.User;
import com.devsuperior.dscommerce.projections.UserDetailsProjection;

public interface UserRepository extends JpaRepository<User,Long>{

    @Query(nativeQuery = true,value = """
            SELECT u.email AS username,u.password,r.id AS roleId,r.authority
            FROM users AS u
            INNER JOIN user_role AS ur ON ur.user_id = u.id
            INNER JOIN roles AS r ON ur.role_id = r.id
            WHERE u.email = :email
            """)
    List<UserDetailsProjection> searchUserAndRolesByEmail(String email);

    Optional<User> findByEmail(String email);
}

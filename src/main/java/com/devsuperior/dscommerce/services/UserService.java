package com.devsuperior.dscommerce.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.devsuperior.dscommerce.dtos.RoleDTO;
import com.devsuperior.dscommerce.dtos.UserDTO;
import com.devsuperior.dscommerce.dtos.UserInsertDTO;
import com.devsuperior.dscommerce.dtos.UserUpdateDTO;
import com.devsuperior.dscommerce.entities.Role;
import com.devsuperior.dscommerce.entities.User;
import com.devsuperior.dscommerce.projections.UserDetailsProjection;
import com.devsuperior.dscommerce.repositories.RoleRepository;
import com.devsuperior.dscommerce.repositories.UserRepository;
import com.devsuperior.dscommerce.services.exceptions.DatabaseException;
import com.devsuperior.dscommerce.services.exceptions.ResourceNotFoundException;
import com.devsuperior.dscommerce.util.CustomUserUtils;

import jakarta.persistence.EntityNotFoundException;

@Service
public class UserService implements UserDetailsService{

    @Autowired
    private UserRepository repository;

    @Autowired
    private CustomUserUtils customUserUtils;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private AuthService authService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        List<UserDetailsProjection> list = repository.searchUserAndRolesByEmail(username);
        if (list.isEmpty()) {
            throw new UsernameNotFoundException("Email Not Found");
        }
        User user = new User();
        user.setEmail(list.get(0).getUsername());
        user.setPassword(list.get(0).getPassword());
        for (UserDetailsProjection x : list){
            user.addRole(new Role(x.getRoleId(), x.getAuthority()));
        }
        return user;
    }

    protected User authenticated(){
        try{
            String username = customUserUtils.getLoggedUsername();
            return repository.findByEmail(username);
        }
        catch(Exception e){
            throw new UsernameNotFoundException("Email Not Found");
        }
    }

    @Transactional(readOnly = true)
    public UserDTO getMe(){
        User user = authenticated();
        return new UserDTO(user);
    }

    @Transactional(readOnly = true)
    public Page<UserDTO> findAll(Pageable pageable){
        return repository.findAll(pageable).map(UserDTO::new);
    } 

    @Transactional(readOnly = true)
    public UserDTO findById(Long id){
        User user = repository.findById(id).orElseThrow(() ->
        new ResourceNotFoundException(id));
        return new UserDTO(user);
    }

    @Transactional
    public UserDTO insert(UserInsertDTO dto){
        User entity = new User();
        entity.setName(dto.getName());
        entity.setEmail(dto.getEmail());
        entity.setPassword(passwordEncoder.encode(dto.getPassword()));
        entity.setPhone(dto.getPhone());
        entity.setBirthDate(dto.getBirthDate());

        Role role = roleRepository.findByAuthority("ROLE_CLIENT");
        entity.addRole(role);

        entity = repository.save(entity);
        return new UserDTO(entity);
    }

    @Transactional
    public UserDTO update(Long id,UserUpdateDTO dto){
        try{
            User entity = repository.getReferenceById(id);
            authService.validateSelfOrAdmin(entity.getId());
            updateData(entity,dto);
            entity = repository.save(entity);
            return new UserDTO(entity);
        }
        catch (EntityNotFoundException e){
            throw new ResourceNotFoundException(id);
        }        
    }

    private void updateData(User entity, UserUpdateDTO dto) {
        entity.setName(dto.getName());
        entity.setEmail(dto.getEmail());
        entity.setPassword(passwordEncoder.encode(dto.getPassword()));
        entity.setPhone(dto.getPhone());
        entity.setBirthDate(dto.getBirthDate());

        entity.getRoles().clear();
        for (RoleDTO x : dto.getRoles()){
            Role role = roleRepository.getReferenceById(x.getId());
            entity.addRole(role);
        }
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    public void deleteById(Long id){
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException(id);
        }
        try{
            repository.deleteById(id);
        }
        catch (DataIntegrityViolationException e){
            throw new DatabaseException("Falha de Integridade Referencial");
        }
    }
}

package com.devsuperior.dscommerce.services;

import static com.devsuperior.dscommerce.factories.RoleFactory.ROLE_CLIENT;
import static com.devsuperior.dscommerce.factories.UserFactory.CLIENT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

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

@ExtendWith(MockitoExtension.class)
public class UserServiceTests {

    @InjectMocks
    private UserService service;

    @Mock
    private UserRepository repository;

    @Mock
    private CustomUserUtils customUserUtils;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private AuthService authService;

    private User client;
    private UserDTO clientDTO;

    private Role role;

    private UserDetailsProjection projection;

    private String existingUsername;
    private String nonExistingUsername;

    private Long existingId;
    private Long nonExistingId;
    private Long dependentId;

    @BeforeEach
    public void setup() throws Exception{
        client = CLIENT();
        clientDTO = new UserDTO(client);

        role = ROLE_CLIENT();

        projection = new UserDetailsProjection() {
            
            @Override
            public String getUsername(){
                return client.getEmail();
            }

            @Override
            public String getPassword(){
                return client.getPassword();
            }

            @Override
            public Long getRoleId(){
                return 1L;
            }

            @Override
            public String getAuthority(){
                return "ROLE_CLIENT";
            }    
        };

        existingUsername = "client@gmail.com";
        nonExistingUsername = "nonexisting@gmail.com";

        existingId = 1L;
        nonExistingId = 2L;
        dependentId = 3L;
    }

    @Test
    public void loadUserByUsernameShouldReturnUserWhenUserNameExists(){
        when(repository.searchUserAndRolesByEmail(existingUsername)).thenReturn(List.of(projection));

        UserDetails result = service.loadUserByUsername(existingUsername);

        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo(client.getEmail());
        assertThat(result.getPassword()).isEqualTo(client.getPassword());
    }

    @Test
    public void loadUserByUsernameShouldThrowUserNameNotFoundExceptionWhenUserNameDoesNotExist(){
        when(repository.searchUserAndRolesByEmail(nonExistingUsername)).thenReturn(Collections.emptyList());

        assertThatThrownBy(() -> service.loadUserByUsername(nonExistingUsername)).isInstanceOf(UsernameNotFoundException.class);
    }

    @Test
    public void authenticatedShouldReturnLoggedUserWhenUserExists(){
        when(customUserUtils.getLoggedUsername()).thenReturn(existingUsername);
        when(repository.findByEmail(existingUsername)).thenReturn(client);

        User sut = service.authenticated();

        assertThat(sut.getId()).isEqualTo(client.getId());
        assertThat(sut.getName()).isEqualTo(client.getName());
        assertThat(sut.getEmail()).isEqualTo(client.getEmail());
        assertThat(sut.getPhone()).isEqualTo(client.getPhone());
        assertThat(sut.getBirthDate()).isEqualTo(client.getBirthDate());
        assertThat(sut.getRoles()).isEqualTo(client.getRoles());
    }

    @Test
    public void authenticatedShouldThrowUsernameNotFoundExceptionWhenUserDoesNotExist(){
        doThrow(ClassCastException.class).when(customUserUtils).getLoggedUsername();

        assertThatThrownBy(() -> service.authenticated()).isInstanceOf(UsernameNotFoundException.class);
    }

    @Test
    public void getMeShouldReturnUserDTOWhenUserIsAuthenticated(){
        UserService spyService = Mockito.spy(service);
        when(spyService.authenticated()).thenReturn(client);

        UserDTO result = spyService.getMe();

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(clientDTO.getId());
        assertThat(result.getName()).isEqualTo(clientDTO.getName());
        assertThat(result.getEmail()).isEqualTo(clientDTO.getEmail());
        assertThat(result.getPhone()).isEqualTo(clientDTO.getPhone());
        assertThat(result.getBirthDate()).isEqualTo(clientDTO.getBirthDate());
        assertThat(result.getRoles().get(0).getAuthority()).isEqualTo(clientDTO.getRoles().get(0).getAuthority());
    }

    @Test
    public void getMeShouldThrowUsernameNotFoundExceptionWhenUserIsNotAuthenticated(){
        UserService spyService = Mockito.spy(service);
        when(spyService.authenticated()).thenThrow(UsernameNotFoundException.class);

        assertThatThrownBy(() -> spyService.getMe()).isInstanceOf(UsernameNotFoundException.class);
    }

    @Test
    public void findAllShouldReturnUserDTOPaged(){
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> page = new PageImpl<>(List.of(client));
        when(repository.findAll(pageable)).thenReturn(page);

        Page<UserDTO> result = service.findAll(pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent().get(0).getId()).isEqualTo(clientDTO.getId());
        assertThat(result.getContent().get(0).getName()).isEqualTo(clientDTO.getName());
        assertThat(result.getContent().get(0).getEmail()).isEqualTo(clientDTO.getEmail());
        assertThat(result.getContent().get(0).getPhone()).isEqualTo(clientDTO.getPhone());
        assertThat(result.getContent().get(0).getBirthDate()).isEqualTo(clientDTO.getBirthDate());
        assertThat(result.getContent().get(0).getRoles().get(0).getAuthority()).isEqualTo(clientDTO.getRoles().get(0).getAuthority());
    }

    @Test
    public void findByIdShouldReturnUserDTOWhenIdExists(){
        when(repository.findById(existingId)).thenReturn(Optional.of(client));

        UserDTO sut = service.findById(existingId);

        assertThat(sut).isNotNull();
        assertThat(sut.getId()).isEqualTo(client.getId());
    }

    @Test
    public void findByIdShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist(){
        when(repository.findById(nonExistingId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(nonExistingId)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    public void insertShouldReturnUserDTOWhenDataIsValid(){
        String encoded = passwordEncoder.encode(client.getPassword());

        when(repository.save(any(User.class))).thenReturn(client);
        when(passwordEncoder.encode(client.getPassword())).thenReturn(encoded);

        UserDTO sut = service.insert(new UserInsertDTO(client));

        assertThat(sut).isNotNull();
        assertThat(sut.getId()).isEqualTo(clientDTO.getId());
        assertThat(sut.getName()).isEqualTo(clientDTO.getName());
        assertThat(sut.getEmail()).isEqualTo(clientDTO.getEmail());
        assertThat(sut.getPhone()).isEqualTo(clientDTO.getPhone());
        assertThat(sut.getBirthDate()).isEqualTo(clientDTO.getBirthDate());
        assertThat(sut.getRoles().get(0).getAuthority()).isEqualTo(clientDTO.getRoles().get(0).getAuthority());
    }

    @Test
    public void updateShouldReturnUpdatedUserDTOWhenIdExistsAndDataIsValid(){
        User update = new User(null, "update", "update@gmail.com", "1092836518", LocalDate.parse("2005-06-28"), "update");
        update.addRole(role);
        UserUpdateDTO updateDTO = new UserUpdateDTO(update);
        String encoded = passwordEncoder.encode(update.getPassword());

        when(repository.getReferenceById(existingId)).thenReturn(client);
        doNothing().when(authService).validateSelfOrAdmin(existingId);
        when(passwordEncoder.encode(update.getPassword())).thenReturn(encoded);
        when(repository.save(any(User.class))).thenReturn(update);

        UserDTO sut = service.update(existingId, updateDTO);

        assertThat(sut).isNotNull();
        assertThat(sut.getId()).isEqualTo(updateDTO.getId());
        assertThat(sut.getName()).isEqualTo(updateDTO.getName());
        assertThat(sut.getEmail()).isEqualTo(updateDTO.getEmail());
        assertThat(sut.getPhone()).isEqualTo(updateDTO.getPhone());
        assertThat(sut.getBirthDate()).isEqualTo(updateDTO.getBirthDate());
        assertThat(sut.getRoles().get(0).getAuthority()).isEqualTo(updateDTO.getRoles().get(0).getAuthority());
    }

    @Test
    public void updateShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist(){
        when(repository.getReferenceById(nonExistingId)).thenThrow(EntityNotFoundException.class);

        assertThatThrownBy(() -> service.update(nonExistingId, new UserUpdateDTO(client))).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    public void deleteShouldThrowNoExceptionWhenIdExists(){
        when(repository.existsById(existingId)).thenReturn(true);
        doNothing().when(repository).deleteById(existingId);

        assertThatCode(() -> service.deleteById(existingId)).doesNotThrowAnyException();
    }

    @Test
    public void deleteShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist(){
        when(repository.existsById(nonExistingId)).thenReturn(false);

        assertThatThrownBy(() -> service.deleteById(nonExistingId)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    public void deleteShouldThrowDatabaseExceptionWhenIdIsDependent(){
        when(repository.existsById(dependentId)).thenReturn(true);
        doThrow(DataIntegrityViolationException.class).when(repository).deleteById(dependentId);

        assertThatThrownBy(() -> service.deleteById(dependentId)).isInstanceOf(DatabaseException.class);
    }
}

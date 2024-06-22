package com.devsuperior.dscommerce.services;

import static com.devsuperior.dscommerce.factories.RoleFactory.ROLE_CLIENT;
import static com.devsuperior.dscommerce.factories.UserFactory.ADMIN;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import com.devsuperior.dscommerce.dtos.EmailDTO;
import com.devsuperior.dscommerce.dtos.NewPasswordDTO;
import com.devsuperior.dscommerce.entities.PasswordRecover;
import com.devsuperior.dscommerce.entities.User;
import com.devsuperior.dscommerce.repositories.PasswordRecoverRepository;
import com.devsuperior.dscommerce.repositories.UserRepository;
import com.devsuperior.dscommerce.services.exceptions.ForbiddenException;
import com.devsuperior.dscommerce.services.exceptions.ResourceNotFoundException;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTests {

    @InjectMocks
    private AuthService service;

    @Mock
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordRecoverRepository passwordRecoverRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private PasswordEncoder passwordEncoder;

    private User admin;
    private User selfClient;
    private User otherClient;

    private String token;
    private PasswordRecover passwordRecover;
    private NewPasswordDTO newPasswordDTO;

    @BeforeEach
    public void setup() throws Exception{
        admin = ADMIN();
        selfClient = new User(1L, "Bob", "bob@gmail.com", "12345678106", LocalDate.parse("2000-09-04"), "12345678");
        selfClient.addRole(ROLE_CLIENT());
        otherClient = new User(3L, "Ana", "ana@gmail.com", "014531859317", LocalDate.parse("2001-07-21"), "12345678");
        otherClient.addRole(ROLE_CLIENT());

        ReflectionTestUtils.setField(service, "tokenMinutes", 30L);
        ReflectionTestUtils.setField(service, "recoverUri", "http://example.com/recover?token=");

        token = UUID.randomUUID().toString();
        passwordRecover = new PasswordRecover(1L, token, "bob@gmail.com", Instant.now().plusSeconds(1800));
        newPasswordDTO = new NewPasswordDTO(token, "bob12345");
    }

    @Test
    public void createRecoverTokenShouldDoNothingWhenEmailExistsInDatabase(){
        EmailDTO emailDTO = new EmailDTO(selfClient.getEmail());
        when(userRepository.findByEmail("bob@gmail.com")).thenReturn(selfClient);
        when(passwordRecoverRepository.save(any(PasswordRecover.class))).thenReturn(passwordRecover);

        assertThatCode(() -> service.createRecoverToken(emailDTO)).doesNotThrowAnyException();

        verify(emailService, times(1)).sendEmail(
            eq(selfClient.getEmail()),
            eq("Recuperação de Senha"),
            contains("Clique no link a seguir para criar uma nova senha:")
        );
    }

    @Test
    public void createRecoverTokenShouldThrowResourceNotFoundExceptionWhenEmailDoesNotExistInDatabase(){
        EmailDTO nonExistingEmail = new EmailDTO("fernando@gmail.com");
        when(userRepository.findByEmail("fernando@gmail.com")).thenReturn(null);

        assertThatCode(() -> service.createRecoverToken(nonExistingEmail)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    public void saveNewPasswordShouldThrowNoExceptionAndUpdateUserPasswordWhenTokenIsValid(){
        String password = "bob12345";
        String encoded = passwordEncoder.encode(password);

        User user = new User();
        user.setEmail("bob@gmail.com");
        user.setPassword(encoded);

        when(userRepository.findByEmail("bob@gmail.com")).thenReturn(selfClient);
        when(passwordRecoverRepository.searchValidTokens(eq(newPasswordDTO.getToken()), any(Instant.class)))
            .thenReturn(List.of(passwordRecover));
        when(passwordEncoder.encode(newPasswordDTO.getPassword())).thenReturn(encoded);
        when(userRepository.save(any(User.class))).thenReturn(user);

        assertThatCode(() -> service.saveNewPassword(newPasswordDTO)).doesNotThrowAnyException();
        assertThat(user.getEmail()).isEqualTo(selfClient.getEmail());
        assertThat(selfClient.getPassword()).isEqualTo(encoded);
    }

    @Test
    public void saveNewPasswordShouldThrowResourceNotFoundExceptionWhenTokenIsNotValid(){
        when(passwordRecoverRepository.searchValidTokens(eq(newPasswordDTO.getToken()),any(Instant.class))).thenReturn(Collections.emptyList());

        assertThatThrownBy(() -> service.saveNewPassword(newPasswordDTO)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    public void validateSelfOrAdminShouldDoNothingWhenAdminIsLogged(){
        when(userService.authenticated()).thenReturn(admin);

        Long userId = admin.getId();

        assertThatCode(() -> service.validateSelfOrAdmin(userId)).doesNotThrowAnyException();
    }

    @Test
    public void validateSelfOrAdminShouldDoNothingWhenSelfIsLogged(){
        when(userService.authenticated()).thenReturn(selfClient);

        Long userId = selfClient.getId();

        assertThatCode(() -> service.validateSelfOrAdmin(userId)).doesNotThrowAnyException();
    }

    @Test
    public void validateSelfOrAdminShouldThrowForbiddenExceptionWhenOtherClientIsLogged(){
        when(userService.authenticated()).thenReturn(selfClient);

        Long userId = otherClient.getId();

        assertThatThrownBy(() -> service.validateSelfOrAdmin(userId)).isInstanceOf(ForbiddenException.class);
    }

    
}

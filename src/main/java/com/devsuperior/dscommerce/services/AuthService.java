package com.devsuperior.dscommerce.services;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.devsuperior.dscommerce.dtos.EmailDTO;
import com.devsuperior.dscommerce.dtos.NewPasswordDTO;
import com.devsuperior.dscommerce.entities.PasswordRecover;
import com.devsuperior.dscommerce.entities.User;
import com.devsuperior.dscommerce.repositories.PasswordRecoverRepository;
import com.devsuperior.dscommerce.repositories.UserRepository;
import com.devsuperior.dscommerce.services.exceptions.ForbiddenException;
import com.devsuperior.dscommerce.services.exceptions.ResourceNotFoundException;

@Service
public class AuthService {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordRecoverRepository passwordRecoverRepository;

    @Autowired
    private UserRepository userRepository;

    @Value("${email.password-recover.token.minutes}")
    private Long tokenMinutes;

    @Value("${email.password-recover.uri}")
    private String recoverUri;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional
    public void createRecoverToken(EmailDTO obj){

        User user = userRepository.findByEmail(obj.getEmail());

        if (user == null) {
            throw new ResourceNotFoundException("Email não encontrado");
        }

        String token = UUID.randomUUID().toString();

        PasswordRecover passwordRecover = new PasswordRecover();
        passwordRecover.setToken(token);
        passwordRecover.setEmail(user.getEmail());
        passwordRecover.setExpiration(Instant.now().plusSeconds(tokenMinutes * 60L));

        passwordRecover = passwordRecoverRepository.save(passwordRecover);

        String body = "Clique no link a seguir para criar uma nova senha:\n\n"
                    + recoverUri + token + "\n\nVálido por " + tokenMinutes + " minutos";

        emailService.sendEmail(user.getEmail(), "Recuperação de Senha", body);

    }

    public void saveNewPassword(NewPasswordDTO obj){
        List<PasswordRecover> result = passwordRecoverRepository.searchValidTokens(obj.getToken(), Instant.now());
        if (result.isEmpty()) {
            throw new ResourceNotFoundException("Token Inválido");
        }
        User user = userRepository.findByEmail(result.get(0).getEmail());
        user.setPassword(passwordEncoder.encode(obj.getPassword()));
        user = userRepository.save(user);
    }

    public void validateSelfOrAdmin(Long userId){
        User me = userService.authenticated();
        if (!me.hasRole("ROLE_ADMIN") && !me.getId().equals(userId)) {
            throw new ForbiddenException("Acess denied");
        }
    }

    
}

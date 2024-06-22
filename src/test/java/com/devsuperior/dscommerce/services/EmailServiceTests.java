package com.devsuperior.dscommerce.services;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import com.devsuperior.dscommerce.services.exceptions.EmailException;


@ExtendWith(MockitoExtension.class)
public class EmailServiceTests {

    @InjectMocks
    private EmailService service;

    @Mock
    private JavaMailSender emailSender;

    private String existingEmail;
    private String nonExistingEmail;

    private SimpleMailMessage validMessage;
    private SimpleMailMessage invalidMessage;

    @BeforeEach
    public void setup() throws Exception{
        ReflectionTestUtils.setField(service, "emailFrom", "ahnertfernando499@gmail.com");

        existingEmail = "existingemail@gmail.com";
        nonExistingEmail = "nonexistingemail@gmail.com";

        validMessage = new SimpleMailMessage();
        validMessage.setFrom("ahnertfernando499@gmail.com");
        validMessage.setTo(existingEmail);
        validMessage.setSubject("subject");
        validMessage.setText("test");
    }

    @Test
    public void sendEmailShouldThrowNoExceptionWhenEmailExists(){
        doNothing().when(emailSender).send(validMessage);

        assertThatCode(() -> service.sendEmail(existingEmail,validMessage.getSubject(),validMessage.getText()));
        verify(emailSender, times(1)).send(validMessage);
    }

    @Test
    public void sendEmailShouldThrowEmailExceptionWhenEmailDoesNotExist(){
        invalidMessage = validMessage;
        invalidMessage.setTo(nonExistingEmail);
        doThrow(MailSendException.class).when(emailSender).send(any(SimpleMailMessage.class));

        assertThrows(EmailException.class, () -> {
            service.sendEmail(nonExistingEmail, "Subject", "Body");
        });

    }
}

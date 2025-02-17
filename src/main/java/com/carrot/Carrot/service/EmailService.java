package com.carrot.Carrot.service;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendVerificationEmail(String email, String token) throws MessagingException {
        String verificationLink = "http://app.powerwebsoftware.it/auth/verify?token=" + token;

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setTo(email);
        helper.setSubject("Conferma il tuo account");
        helper.setText("<h2>Benvenuto su Carrot!</h2>" +
                "<p>Clicca sul link per verificare il tuo account:</p>" +
                "<a href=\"" + verificationLink + "\">Verifica il tuo account</a>",
                true);

        mailSender.send(message);
    }
}

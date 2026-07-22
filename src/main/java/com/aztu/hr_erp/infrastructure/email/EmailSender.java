package com.aztu.hr_erp.infrastructure.email;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

/** Low-level SMTP sender. Higher-level rendering + hr_email_log live in the notification feature. */
@Component
public class EmailSender {

    private static final Logger log = LoggerFactory.getLogger(EmailSender.class);

    private final JavaMailSender javaMailSender;
    private final String from;

    public EmailSender(JavaMailSender javaMailSender, @Value("${app.mail.from:hr@aztu.edu.az}") String from) {
        this.javaMailSender = javaMailSender;
        this.from = from;
    }

    /** Sends a plain-text email; throws on failure so the caller can mark the log row failed. */
    public void send(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        javaMailSender.send(message);
        log.debug("Email sent to {}", to);
    }
}

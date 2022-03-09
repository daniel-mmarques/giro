package com.giro.service;

import com.giro.entites.email.EmailModel;
import com.giro.entites.enums.EmailStatusEnum;
import com.giro.repository.EmailRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.internet.MimeMessage;
import javax.transaction.Transactional;
import java.time.LocalDateTime;

@Slf4j
@Service
public class EmailService {

    @Autowired
    private EmailRepository emailRepository;

    @Autowired
    private JavaMailSender emailSender;

    @Transactional
    public EmailModel sendEmail(EmailModel emailModel) {

        emailModel.setSendDateEmail(LocalDateTime.now());
        try {

            MimeMessage message = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message);

            helper.setFrom(emailModel.getEmailFrom(), "IMPIC");
            helper.setTo(emailModel.getEmailTo());
            helper.setSubject(emailModel.getSubject());
            helper.setText(emailModel.getText(), true);
            emailSender.send(message);
            emailModel.setEmailStatus(EmailStatusEnum.SEND);
        } catch (MailException e) {
            log.error(e.getMessage());
            emailModel.setEmailStatus(EmailStatusEnum.ERROR);
        } finally {
            return emailRepository.save(emailModel);
        }
    }

}

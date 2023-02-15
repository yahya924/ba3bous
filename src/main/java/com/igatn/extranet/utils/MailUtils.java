package com.igatn.extranet.utils;

import com.igatn.extranet.rest.exceptions.MailingConfigurationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import java.util.Arrays;
import java.util.Objects;

@Service
public class MailUtils {

    @Value("${spring.mail.username}")
    private String sender;

    public MimeMessagePreparator prepareMail(InternetAddress[] recipients, String subject, String content, MultipartFile[] files) {
        return mimeMessage -> {
            
            try {
                
                MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

                helper.setFrom(new InternetAddress(sender));
                helper.setTo(recipients);
                helper.setSubject(subject);
                helper.setText(content);

                if (files != null) {
                    Arrays.stream(files).forEach(file -> {
                        try {
                            helper.addAttachment(Objects.requireNonNull(file.getOriginalFilename()), file);
                        }
                        catch (MessagingException e) {
                            throw new MailingConfigurationException();
                        }
                    });
                }
            }
            catch (MessagingException e) {
                throw new MailingConfigurationException();
            }
        };
    }
}

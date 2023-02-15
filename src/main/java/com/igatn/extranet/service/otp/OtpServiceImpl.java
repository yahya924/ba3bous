package com.igatn.extranet.service.otp;

import com.igatn.extranet.app.AppProperties;
import com.igatn.extranet.app.AppProperties.Security;
import com.igatn.extranet.app.AppProperties.Security.IGASecurityExpirations;
import com.igatn.extranet.app.AppService;
import com.igatn.extranet.domainjpa.api.data.otp.OtpCodeRepository;
import com.igatn.extranet.domainjpa.impl.domain.auth.OtpCode;
import com.igatn.extranet.domainjpa.impl.domain.user.User;
import com.igatn.extranet.rest.exceptions.InvalidInternetAddressException;
import com.igatn.extranet.utils.MailUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Service;

import javax.el.PropertyNotFoundException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Service
public class OtpServiceImpl implements OtpService {

    private final OtpCodeRepository codeRepository;

    private final MailUtils mailUtils;

    private final AppService appService;

    private final JavaMailSender javaMailSender;

    public OtpServiceImpl(OtpCodeRepository codeRepository, MailUtils mailUtils, AppService appService, JavaMailSender javaMailSender) {
        this.codeRepository = codeRepository;
        this.mailUtils = mailUtils;
        this.appService = appService;
        this.javaMailSender = javaMailSender;
    }

    @Override
    public void sendOtpCodeEmail(User user, String username) {

        int otpExpirationTime = Optional.ofNullable(
                appService.getAppProperties())
            .map(AppProperties::getSecurity)
            .map(Security::getExpiration)
            .map(IGASecurityExpirations::getOtpCode)
            .orElseThrow(
                () -> new PropertyNotFoundException("No such property 'expiration.otp-code'!")
            );

        // TODO: For security reasons, use SecureRandom
        String securityCode = RandomStringUtils.randomNumeric(4);
        
        // 1 send code
        try {

            InternetAddress[] recipients = new InternetAddress[]{new InternetAddress(username)};

            final String SUBJECT = "Extranet Mobile - Code d'authentification temporaire";
            final String CONTENT = "Voici le code de sécurité que vous devez utiliser pour vous authentifier: " + securityCode;

            MimeMessagePreparator preparator = mailUtils.prepareMail(recipients, SUBJECT, CONTENT, null);

            this.javaMailSender.send(preparator);


        } catch (AddressException e) {
            
            throw new InvalidInternetAddressException();
        }

        // 2 - persist information
        OtpCode userCode = new OtpCode();
        
        userCode.setCodeValue(securityCode);
        userCode.setUser(user);
        userCode.setRecipient(username);
        userCode.setExpiresAt(
            Instant
                .now()
                .plus(otpExpirationTime, ChronoUnit.SECONDS)
        );

        codeRepository.save(userCode);

    }
    
    @Override
    public boolean checkCodeByUser(String code, User user){

        OtpCode validOtpCode = codeRepository.findByCodeValueAndUser(code,user)
            .orElseThrow(
                () -> new IllegalArgumentException(
                    "The OTP code: " + code + " doesn't exist for user with ID: " + user.getId()
                )
            );
        
        if(validOtpCode.getCodeValue().matches(code)){

            Instant expirationTime = validOtpCode.getExpiresAt();
            Instant nowTime = Instant.now();
            
            Optional<Instant> matchingTime = Optional.ofNullable(validOtpCode.getMatchedAt());

            if(matchingTime.isPresent()) 
                throw new RuntimeException("OTP code expired! for user with ID: " + user.getId());
            
            validOtpCode.setMatchedAt(nowTime);
                
            codeRepository.save(validOtpCode);
            
            return nowTime.compareTo(expirationTime) < 0;
        }
        
        return false;
    }
    
    @Override
    public OtpCode getLatestUsedByUser(User user){
        
        OtpCode targetOtp = codeRepository.findTopByUserOrderByMatchedAtDesc(user)
            .orElseThrow(
                () -> new SecurityException("No OTP code found for the 'username': " + user.getUsername())
            );
        
        return targetOtp;
    }
}

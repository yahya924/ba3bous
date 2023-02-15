package com.igatn.extranet.utils;

import com.igatn.extranet.rest.exceptions.ApiError;
import com.igatn.extranet.security.filters.SecurityFilterConstants;
import com.igatn.extranet.utils.date.CustomDateValidator;
import com.igatn.extranet.utils.date.CustomDateValidatorImpl;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.experimental.UtilityClass;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@UtilityClass
public final class ExtranetUtils {

    public static boolean listHasOneItem(List<?> list) { return list.size() == 1;}

    public static boolean listHasManyItems(List<?> list) { return list.size() > 1;}
    
    public static boolean verifyDateValidity(String dateString) {
        
        DateTimeFormatter dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE;
        CustomDateValidator dateValidator = new CustomDateValidatorImpl(dateFormatter);
        
        boolean result = false;
        
        if(StringUtils.hasText(dateString)) {
            result = dateValidator.isDateValid(dateString);
            
            if(!result) throw new IllegalArgumentException("Invalid date: '"+dateString+"'");
        }
        
        return result;
    }

    /**
     * Generic objects validator against Null
     * 
     * @param nullableObject
     * @param errArg
     * @param errExpression
     * @param <T>
     */
    public static <T> void validateObject(T nullableObject, String errArg, String errExpression) {
        Optional
            .ofNullable(nullableObject)
            .ifPresentOrElse(
                t -> {},
                () -> {
                    throw new IllegalArgumentException(String.format(errExpression, errArg));
                }
            );
    }

    public ApiError getApiError(String errorMsg) {
        
        final ApiError apiError = new ApiError(HttpStatus.INTERNAL_SERVER_ERROR.value(), errorMsg);

        return apiError;
    }

    /**
     * FRE - This method is on hold for being slow on remote server
     * @author FRE
     * Secured way to get 4 digits code
     * 
     * @return
     */
//    public String generateSecured4DigitsCode() {
//        String code;
//
//        try {
//            SecureRandom random = SecureRandom.getInstanceStrong();
//            code = String.valueOf(random.nextInt(9000) + 1000);
//            
//        } catch (NoSuchAlgorithmException e) {
//            throw new RuntimeException("Problem when generating secure random code.");
//        }
//
//        return code;
//    }

    /**
     * helper method to build jwt security token
     * 
     * @param username
     * @param signingKey
     * @param expirationInSeconds
     * @return
     */
    public static String buildApiSecurityToken(
        String username,
        String signingKey,
        int expirationInSeconds
    ) {

        SecretKey secretKey = Keys
            .hmacShaKeyFor(
                signingKey.getBytes(StandardCharsets.UTF_8)
            );

        Date dateNow = new Date();

        Instant instantExp = dateNow
            .toInstant()
            .plusSeconds(expirationInSeconds);

        final String AUTH_TOKEN = Jwts.builder()
            .setClaims(
                Map.of("username", username)
            )
            .setExpiration(Date.from(instantExp))
            .signWith(secretKey)
            .compact();

        return AUTH_TOKEN;
    }
    
    /**
     * build custom auth token  
     * 
     * @param username
     * @param passwordOrCode
     * @param exp
     * @return
     */
    public static String buildCustomAuthToken(
        final String username, 
        final String passwordOrCode,
        int exp
    ) {

        StringBuilder toEncodeBuilder = new StringBuilder();

        toEncodeBuilder
            .append(username)
            .append(SecurityFilterConstants.SEPARATOR);

        if(Optional.ofNullable(passwordOrCode).isPresent()) {
            toEncodeBuilder
                .append(passwordOrCode)
                .append(SecurityFilterConstants.SEPARATOR);
        }
        
        if(exp > 0) {
            
            var expirationDate = LocalDateTime.now()
                .plusSeconds(exp)
                .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            
            toEncodeBuilder.append(expirationDate);
        }
        
        final String ENCODED = Base64
            .getEncoder()
            .encodeToString(
                toEncodeBuilder
                    .toString()
                    .getBytes(StandardCharsets.UTF_8)
            );
        
        return ENCODED;
    }

    public static String[] validateOtpToken(String otpAuthorization, String errMsg) {

        final String DECODED_TOKEN = ExtranetUtils.decodeSimpleAuthToken(otpAuthorization);
        
        if (!DECODED_TOKEN.contains(SecurityFilterConstants.SEPARATOR))
            throw new SecurityException(errMsg);

        final String[] OTP_TOKEN_SPLIT = DECODED_TOKEN.split(SecurityFilterConstants.SEPARATOR);

       return OTP_TOKEN_SPLIT;
    }
    
    public static String decodeSimpleAuthToken(String OTP_AUTHORIZATION) {
        final byte[] DECODED_AUTHORIZATION = Base64
            .getDecoder()
            .decode(OTP_AUTHORIZATION);

        final String DECODED_TOKEN = new String(DECODED_AUTHORIZATION);

        return DECODED_TOKEN;
    }

    /**
     * helper method to verify whether a list of string args has at least one 
     * invalid element. The cases are: empty, null and blank 
     * @param args
     * @return
     */
    public static boolean hasAnyNullOrEmpty(String... args){

        return Arrays
            .stream(args)
            .anyMatch(e -> !StringUtils.hasText(e));
    }
    
}

package com.igatn.extranet.security.filters;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.igatn.extranet.domainjpa.impl.domain.tracking.AppAuthHistory;
import com.igatn.extranet.domainjpa.impl.domain.user.User;
import com.igatn.extranet.rest.exceptions.ApiError;
import com.igatn.extranet.service.history.AuthHistoryService;
import com.igatn.extranet.service.user.UserServiceImpl;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import io.micrometer.core.lang.NonNullApi;
import org.apache.http.entity.ContentType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@NonNullApi
@Component
public class ValidationAuthenticationFilter extends OncePerRequestFilter {

    @Value("${igatn.extranet.security.keys.signing}")
    private String signInKey;

    private final UserServiceImpl userService;
    
    private final AuthHistoryService authHistoryService;

    public ValidationAuthenticationFilter(
        UserServiceImpl userService, 
        AuthHistoryService authHistoryService
    ) {
        this.userService = userService;
        this.authHistoryService = authHistoryService;
    }

    @Override
    protected void doFilterInternal(
        HttpServletRequest request, 
        HttpServletResponse response,
        FilterChain filterChain
    )
        throws
        IOException,
        SecurityException,
        SignatureException 
    {
        
        try {
            
            final String AUTHORIZATION = Optional.ofNullable(
                request.getHeader(HttpHeaders.AUTHORIZATION)
            ).orElseThrow(
                () -> new SecurityException("Not authorized! Please sign in")
            );

            final boolean HAS_BEARER_KEYWORD = AUTHORIZATION.contains("Bearer");
            final boolean HAS_WHITE_SPACE = AUTHORIZATION.contains(" ");
            
            String defaultErrorMsg = "Wrong authorization structure: " + AUTHORIZATION;
            
            if(!HAS_BEARER_KEYWORD || !HAS_WHITE_SPACE)
                throw new SecurityException(defaultErrorMsg);
                
            // token is the second part of the authorization string
            final String[] AUTHORIZATION_SPLIT = AUTHORIZATION.split(" ");
           
           // the authorization must be like "Bearer ey45a45z.."
            // if we split it, it becomes an array of 2 elements: {"Bearer", "ey45a45z.."}
            if(AUTHORIZATION_SPLIT.length != 2)
                throw new SecurityException(defaultErrorMsg);
            
            final String TOKEN = AUTHORIZATION_SPLIT[1];
            
            if(!StringUtils.hasText(TOKEN))
                throw new SecurityException("Token is empty or blank! : "+ TOKEN);
            
            // decrypt with signing key
            final SecretKey SIGNING_KEY = Keys.hmacShaKeyFor(signInKey.getBytes(StandardCharsets.UTF_8));
            
            final Claims TOKEN_SECURED_CLAIMS = Jwts.parserBuilder()
                .setSigningKey(SIGNING_KEY)
                .build()
                .parseClaimsJws(TOKEN)
                .getBody();
            
            // get username
            final Object USERNAME = Optional.ofNullable(TOKEN_SECURED_CLAIMS.get("username"))
                .orElseThrow(
                    () -> new SecurityException("No 'username' found in token claims!")
                );

            final Date EXPIRATION = Optional.ofNullable(TOKEN_SECURED_CLAIMS.getExpiration())
                .orElseThrow(
                    () -> new SecurityException("No expiration datetime found in token claims!")
                );
            
            if(!(USERNAME instanceof String))
                throw new SecurityException("'username' found but it must be of type String");
            
            // TODO: validate token expiration with db

            AppAuthHistory history = authHistoryService.getByToken(TOKEN);
            
            authHistoryService.validate(history);
            
//            if(!Instant.now().isBefore(EXPIRATION.toInstant()))
//                throw new SecurityException("Authorization token has expired!");

            final User TARGET_USER = userService.loadUserByUsername((String) USERNAME);
            TARGET_USER.setAuthToken(TOKEN);
            
            final var FRAMEWORK_AUTH = new UsernamePasswordAuthenticationToken(
                TARGET_USER, null, TARGET_USER.getAuthorities()
            );

            // set authentication for user
            SecurityContextHolder.getContext().setAuthentication(FRAMEWORK_AUTH);

            // validate security filter
            filterChain.doFilter(request, response);
            
        } catch (Exception e){
            
            ApiError apiError = new ApiError(e.getLocalizedMessage());

            boolean isJwtFormException = e instanceof MalformedJwtException;
            boolean isJwtSignatureException = e instanceof SignatureException;
            boolean isJwtExpiredException = e instanceof ExpiredJwtException;
            boolean isJwtException = isJwtFormException || isJwtSignatureException || isJwtExpiredException;
            
            if(isJwtException)
                apiError.setMessage("Security: Invalid authorization token!");
            
            final ObjectWriter OBJECT_WRITER = new ObjectMapper()
                .writer()
                .withDefaultPrettyPrinter();

            apiError.setCode(HttpStatus.UNAUTHORIZED.value());

            final ResponseEntity<ApiError> RESPONSE_ENTITY = ResponseEntity.ok(apiError);

            final String JSON_RESPONSE = OBJECT_WRITER
                .withDefaultPrettyPrinter()
                .writeValueAsString(RESPONSE_ENTITY.getBody());

            response.getWriter().write(JSON_RESPONSE);
            
        } finally {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType(ContentType.APPLICATION_JSON.getMimeType());
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        
        final String SERVLET_PATH = request.getServletPath();

        // TODO: the following paths must be protected somehow
        // copied from SecurityConfiguration class
        final List<String> EXCLUDED_PATHS = List.of(
            "/users/signIn",
            "/users/sendCode",
            "/test/notifications",
            "/styles/styles.css",
            "/languages/getAll",
            "/users/verifyCode",
            "/users/forgetPassword/update"
        );

        // as there are 2 endpoints to send notifications
        // the one consumed with GET is used in web interface page,
        // we need to tell the REST API filter here to skip
        // that endpoint if it's sent with GET
        final boolean IS_USING_GET = request.getMethod()
            .equalsIgnoreCase(HttpMethod.GET.toString());
        
        if(IS_USING_GET && SERVLET_PATH.equals("/notifications/send"))
            return true;
        
        return EXCLUDED_PATHS.stream()
            .anyMatch((e)-> e.matches(SERVLET_PATH));
    }
}

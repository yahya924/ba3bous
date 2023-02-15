package com.igatn.extranet.security;

import com.igatn.extranet.security.filters.InitAuthenticationFilter;
import com.igatn.extranet.security.filters.ValidationAuthenticationFilter;
import com.igatn.extranet.security.providers.UsernamePasswordAuthenticationProvider;
import com.igatn.extranet.security.providers.biometrics.BiometricsAuthenticationProvider;
import com.igatn.extranet.security.providers.otp.OtpReceiverAuthenticationProvider;
import com.igatn.extranet.security.providers.otp.OtpSenderAuthenticationProvider;
import com.igatn.extranet.security.providers.pin.PinAuthenticationProvider;
import com.igatn.extranet.security.providers.pin.PinReceiverAuthenticationProvider;
import com.igatn.extranet.service.user.UserServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;


/**
 * FRE - Security main config
 */
@SuppressWarnings("deprecation")
@Configuration
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    // filters
    private final InitAuthenticationFilter initAuthenticationFilter;
    private final ValidationAuthenticationFilter validationAuthenticationFilter;
    
    // providers
    private final UsernamePasswordAuthenticationProvider usernamePasswordAuthenticationProvider;
    private final OtpSenderAuthenticationProvider otpSenderAuthenticationProvider;
    private final OtpReceiverAuthenticationProvider otpReceiverAuthenticationProvider;
    private final PinReceiverAuthenticationProvider pinReceiverAuthenticationProvider;
    private final PinAuthenticationProvider pinAuthenticationProvider;
    private final BiometricsAuthenticationProvider biometricsAuthenticationProvider;
    private final UserServiceImpl userService;

    // constructor injection is better than field injection
    // read more: https://stackoverflow.com/a/40620318/10000150
    public SecurityConfiguration(
        InitAuthenticationFilter initAuthenticationFilter,
        ValidationAuthenticationFilter validationAuthenticationFilter,
        UsernamePasswordAuthenticationProvider usernamePasswordAuthenticationProvider,
        OtpSenderAuthenticationProvider otpSenderAuthenticationProvider,
        OtpReceiverAuthenticationProvider otpReceiverAuthenticationProvider,
        PinReceiverAuthenticationProvider pinReceiverAuthenticationProvider,
        PinAuthenticationProvider pinAuthenticationProvider,
        BiometricsAuthenticationProvider biometricsAuthenticationProvider,
        UserServiceImpl userService
    ) {
        // filters
        this.initAuthenticationFilter = initAuthenticationFilter;
        this.validationAuthenticationFilter = validationAuthenticationFilter;
        
        // providers
        this.usernamePasswordAuthenticationProvider = usernamePasswordAuthenticationProvider;
        this.otpSenderAuthenticationProvider = otpSenderAuthenticationProvider;
        this.otpReceiverAuthenticationProvider = otpReceiverAuthenticationProvider;
        this.pinReceiverAuthenticationProvider = pinReceiverAuthenticationProvider;
        this.pinAuthenticationProvider = pinAuthenticationProvider;
        this.biometricsAuthenticationProvider = biometricsAuthenticationProvider;
        this.userService = userService;
    }
    
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        
        auth
            .userDetailsService(userService)
            // TODO: use recommended password encoder instead of plainText
            .passwordEncoder(passwordEncoder())
            .and()
            .authenticationProvider(usernamePasswordAuthenticationProvider)
            .authenticationProvider(otpSenderAuthenticationProvider)
            .authenticationProvider(otpReceiverAuthenticationProvider)
            .authenticationProvider(pinReceiverAuthenticationProvider)
            .authenticationProvider(pinAuthenticationProvider)
            .authenticationProvider(biometricsAuthenticationProvider);
    }
    
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .httpBasic()
            .and()
            .csrf()
            .disable();
        
        http
            .addFilterAfter(
                initAuthenticationFilter,
                BasicAuthenticationFilter.class
            )
            .addFilterAfter(
                validationAuthenticationFilter,
                BasicAuthenticationFilter.class
            )
            .authorizeRequests()
            .antMatchers(HttpMethod.GET, "/test/notifications").hasAuthority("INSURANCE_SYSTEM")
            .antMatchers(HttpMethod.GET, "/styles/**").hasAuthority("INSURANCE_SYSTEM")
            // this endpoint is only used in form, and its temporary
            .antMatchers(HttpMethod.GET,"/notifications/send").hasAuthority("INSURANCE_SYSTEM")
            
            .antMatchers(HttpMethod.OPTIONS, "/**").permitAll() // necessary for react CORS
            
            // TODO: improve security by adding other endpoints
            // get
            .antMatchers(HttpMethod.GET, "/historyActivity/**").hasAuthority("Insured")
            .antMatchers(HttpMethod.GET, "/documents/**").hasAuthority("Insured")
            .antMatchers(HttpMethod.GET, "/exchanges/**").hasAuthority("Insured")
            .antMatchers(HttpMethod.GET, "/mails/**").hasAuthority("Insured")
            .antMatchers(HttpMethod.GET, "/payment/**").hasAuthority("Insured")
            .antMatchers(HttpMethod.GET, "/policies/**").hasAuthority("Insured")
            .antMatchers(HttpMethod.GET, "/premiums/**").hasAuthority("Insured")
            .antMatchers(HttpMethod.GET, "/reimbursements/**").hasAuthority("Insured")
            .antMatchers(HttpMethod.GET, "/statistics/**").hasAuthority("Insured")
            .antMatchers(HttpMethod.GET, "/supports/**").hasAuthority("Insured")
            .antMatchers(HttpMethod.GET, "/languages/**").hasAuthority("Insured")

            // post
            .antMatchers(HttpMethod.POST,"/payment/**").hasAuthority("Insured")
            .antMatchers(HttpMethod.POST,"/notifications/send").hasAuthority("INSURANCE_SYSTEM")
            .antMatchers(HttpMethod.POST,"/notifications/see").hasAuthority("Insured")

            .anyRequest()
            .authenticated();
        
        http.cors()
             //needed for H2-Console
            .and()
            .headers()
            .frameOptions()
            .sameOrigin();
        
        // FRE - BUGFIX: the user can authenticate with wrong passwords
        // after 1st auth. this issue is due to spring security auto session management.
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
    }
    
    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring()
            // TODO: add filters to these paths
            .antMatchers("/languages/getAll")
//            .antMatchers("/notifications/send")
//            .antMatchers("/styles/**")
//            .antMatchers("/test/notifications")
            .antMatchers("/users/sendCode")
            .antMatchers("/users/verifyCode")
            .antMatchers("/users/forgetPassword/update");
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return NoOpPasswordEncoder.getInstance();
    }
    
    // Authentication manager must be injected with @Lazy 
    // in order to avoid absent-dependency issues
    @Bean
    protected AuthenticationManager customAuthenticationManager() throws Exception {
        return authenticationManager();
    }

}


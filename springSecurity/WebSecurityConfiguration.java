package com.richinfoai.server.config;

import com.richinfoai.server.security.CasAuthEntryPoint;
import com.richinfoai.server.security.CommunityUserDetailsService;
import com.richinfoai.server.security.LoginSuccessHandler;
import com.richinfoai.server.security.UserDetailsServiceImpl;
import com.richinfoai.server.service.AuthService;
import org.jasig.cas.client.session.SingleSignOutFilter;
import org.jasig.cas.client.validation.Cas30ServiceTicketValidator;
import org.jasig.cas.client.validation.TicketValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.cas.ServiceProperties;
import org.springframework.security.cas.authentication.CasAssertionAuthenticationToken;
import org.springframework.security.cas.authentication.CasAuthenticationProvider;
import org.springframework.security.cas.web.CasAuthenticationFilter;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.util.matcher.AndRequestMatcher;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestHeaderRequestMatcher;
import org.springframework.stereotype.Component;

@Configuration
@EnableWebSecurity
@EnableConfigurationProperties
@Component
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Value("${auth.cas.server.login:}")
    private String casServerLogin;
    @Value("${auth.cas.server.logout:}")
    private String casServerLogout;
    @Value("${auth.cas.server.prefix:}")
    private String casServerPrefix;
    @Value("${auth.cas.client.login:}")
    private String casClientLogin;
    @Value("${auth.cas.header.name:}")
    private String casAuthHeaderName;
    @Value("${auth.cas.header.value:}")
    private String casAuthHeaderValue;
    @Value("${auth.cas.enabled}")
    private Boolean casAuthEnabled;


    @Autowired
    AuthService authService;

    @Override
    public void configure(WebSecurity web) {
        RequestHeaderRequestMatcher requestHeaderRequestMatcher = new RequestHeaderRequestMatcher(casAuthHeaderName, casAuthHeaderValue);
        OrRequestMatcher orRequestMatcher = new OrRequestMatcher(
                new AntPathRequestMatcher("/api/bi/alarmReceipt"),
                new AntPathRequestMatcher("/api/bi/queryAlarm"),
                new AntPathRequestMatcher("/api/bi/queryBase64"));
        AndRequestMatcher andRequestMatcher = new AndRequestMatcher(
                requestHeaderRequestMatcher,
                orRequestMatcher
        );
        web.ignoring().requestMatchers(andRequestMatcher);
    }

    @Bean
    public LoginSuccessHandler getLoginSuccessHandler() {
        return new LoginSuccessHandler();
    }



    @Override
    protected void configure(HttpSecurity http) throws Exception {

        http.authorizeRequests()
                .antMatchers("/debug/hello")
                .permitAll()
                ;
        http.exceptionHandling().authenticationEntryPoint(casAuthenticationEntryPoint());
        http.addFilterBefore(requestSingleLogoutFilter(), LogoutFilter.class);
        http.addFilterBefore(singleLogoutFilter(), CasAuthenticationFilter.class);
        http.addFilterBefore(casFilter(), CasAuthenticationFilter.class);
        http.authorizeRequests().antMatchers(HttpMethod.OPTIONS).permitAll();
        if (casAuthEnabled) {
            http.authorizeRequests().anyRequest().authenticated();
        }
        http.csrf().disable();

        http
                .cors().and()
                .authorizeRequests()
                .anyRequest().authenticated() // Explains that all requests to any endpoint must be authorizes, or else they should be rejected
                .and().formLogin().permitAll()
                .successHandler(getLoginSuccessHandler());//when login failure will redirect to post
        http.logout().logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler(HttpStatus.OK));
    }

    @Override
    protected void configure(AuthenticationManagerBuilder authenticationManagerBuilder) throws Exception {
        /**
         * CAS Login Provider Config
         */
        authenticationManagerBuilder.authenticationProvider(casAuthenticationProvider());
        authenticationManagerBuilder.userDetailsService(communityUserDetailsService());
    }

    @Bean
    public UserDetailsService communityUserDetailsService() {
        return new CommunityUserDetailsService(authService);
    }


    @Bean
    public NoOpPasswordEncoder passwordEncoder() {
        return (NoOpPasswordEncoder) NoOpPasswordEncoder.getInstance();
    }

    public AuthenticationEntryPoint casAuthenticationEntryPoint() {

        CasAuthEntryPoint casEntryPoint = new CasAuthEntryPoint(casServerLogin, serviceProperties());
        return casEntryPoint;
    }

    @Bean
    public CasAuthenticationProvider casAuthenticationProvider() {
        CasAuthenticationProvider casAuthenticationProvider = new CasAuthenticationProvider();
        casAuthenticationProvider.setAuthenticationUserDetailsService(casAuthenticationUserDetailsService());
        casAuthenticationProvider.setServiceProperties(serviceProperties());
        casAuthenticationProvider.setTicketValidator(ticketValidator());
        casAuthenticationProvider.setKey("rich_server_app_key");
        return casAuthenticationProvider;
    }

    public TicketValidator ticketValidator() {
        TicketValidator ticketValidator = new Cas30ServiceTicketValidator(casServerPrefix);
        return ticketValidator;
    }

    public AuthenticationUserDetailsService<CasAssertionAuthenticationToken> casAuthenticationUserDetailsService() {
        return new UserDetailsServiceImpl(authService);
    }

    public ServiceProperties serviceProperties() {
        ServiceProperties serviceProperties = new ServiceProperties();
        serviceProperties.setService(casClientLogin);
        serviceProperties.setAuthenticateAllArtifacts(true);
        return serviceProperties;
    }

    public CasAuthenticationFilter casFilter() throws Exception {
        CasAuthenticationFilter casFilter = new CasAuthenticationFilter();
        casFilter.setAuthenticationManager(authenticationManager());
        return casFilter;
    }


    public SingleSignOutFilter singleLogoutFilter() {
        SingleSignOutFilter singleSignOutFilter = new SingleSignOutFilter();
        singleSignOutFilter.setCasServerUrlPrefix(casServerPrefix);
        return singleSignOutFilter;
    }

    public LogoutFilter requestSingleLogoutFilter() {
        LogoutFilter logoutFilter = new LogoutFilter(
                casServerLogout,
                new SecurityContextLogoutHandler());
        logoutFilter.setFilterProcessesUrl("/logout/cas");
        return logoutFilter;
    }
}
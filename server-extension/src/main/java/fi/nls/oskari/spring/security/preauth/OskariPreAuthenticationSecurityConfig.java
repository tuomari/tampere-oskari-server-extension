package fi.nls.oskari.spring.security.preauth;

import fi.nls.oskari.spring.SpringEnvHelper;
import fi.nls.oskari.util.PropertyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationProvider;

@Profile("preauth")
@Configuration
@EnableWebSecurity
@Order(0)
// IMPORTANT: This must be before formLogin or /auth doesn't trigger this
public class OskariPreAuthenticationSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private SpringEnvHelper env;

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        // Don't set "X-Frame-Options: deny" header, that would prevent
        // embedded maps from working
        http.headers().frameOptions().disable();

        // Don't create unnecessary sessions
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED);

        // Disable HSTS header, we don't want to force HTTPS for ALL requests
        http.headers().httpStrictTransportSecurity().disable();

        OskariRequestHeaderAuthenticationFilter filter = new OskariRequestHeaderAuthenticationFilter();
        filter.setAuthenticationSuccessHandler(new OskariPreAuthenticationSuccessHandler());
        filter.setPrincipalRequestHeader(PropertyUtil.get("oskari.preauth.username.header", "X_EMAIL"));

        HeaderAuthenticationDetailsSource headerAuthenticationDetailsSource = new HeaderAuthenticationDetailsSource();

        filter.setExceptionIfHeaderMissing(true);

        filter.setAuthenticationDetailsSource(headerAuthenticationDetailsSource);
        filter.setAuthenticationManager(authenticationManager());
        filter.setContinueFilterChainOnUnsuccessfulAuthentication(false);

        String authorizeUrl = PropertyUtil.get("oskari.authorize.url", "/auth");

        // use authorization for requests matching /auth
        http.authorizeRequests()
                // IF accessing /auth -> require authentication (== headers)
                .antMatchers(authorizeUrl).authenticated();

        http.antMatcher(authorizeUrl)
                .addFilterBefore(filter, UsernamePasswordAuthenticationFilter.class)
                .authorizeRequests()
                .anyRequest().authenticated();

    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth)
            throws Exception {
        PreAuthenticatedAuthenticationProvider preAuthenticatedProvider = new PreAuthenticatedAuthenticationProvider();
        preAuthenticatedProvider.setPreAuthenticatedUserDetailsService(new OskariPreAuthenticatedUserDetailsService());
        auth.authenticationProvider(preAuthenticatedProvider);
    }
}
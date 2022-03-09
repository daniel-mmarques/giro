package com.giro.config;

import com.giro.repository.UtilizadorRepository;
import com.giro.service.SUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.BeanIds;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Autowired
    private UtilizadorRepository utilizadorRepository;

    @Override
    public UserDetailsService userDetailsServiceBean() {
        return new SUserDetailsService(utilizadorRepository);
    }

    @Bean
    public CustomPasswordEncoder customPasswordEncoder() {
        return new CustomPasswordEncoder();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        http.authorizeRequests()
                .antMatchers("/javax.faces.resource/**").permitAll()
                .antMatchers("/authenticationAT").permitAll()
                .antMatchers("/authenticationAMA").permitAll()
                .antMatchers("/email").permitAll()
                .antMatchers("/utilizador.xhtml").permitAll()
                .antMatchers("/verificarEmail.xhtml").permitAll()
                .antMatchers("/activate/*").permitAll()
                .anyRequest().authenticated();

        http.formLogin()
                .loginPage("/login.xhtml")
                .permitAll()
                .failureUrl("/login.xhtml?error=true");

        http.logout().logoutSuccessUrl("/login.xhtml");

        http.csrf().disable();
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder builder) throws Exception {
        builder.userDetailsService(userDetailsServiceBean())
                .passwordEncoder(customPasswordEncoder());
    }

    @Bean(name = BeanIds.AUTHENTICATION_MANAGER)
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

}


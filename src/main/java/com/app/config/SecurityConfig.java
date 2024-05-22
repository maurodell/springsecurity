package com.app.config;

import com.app.config.filter.JwtValidator;
import com.app.service.UserDetailsServiceImpl;
import com.app.util.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity// permite mediante annotations configurar los filtros.
public class SecurityConfig {

    @Autowired
    private JwtUtils jwtUtils;


    //Se definen los filtros y el DelegatingFilterProxy, es decir las reglas, condiciones.
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
                .csrf(csrf -> csrf.disable())//autenticación basada en token que se guardan en las cookies. Cuando trabajamos con aplicaciones REST esto no lo necesitamos a esta protección, se usa en formularios.
                .httpBasic(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))//cuando trabajamos con app web es necesario una sesión sin estado. Esto quiere decir, que cuando un usuario se logea se crea un objeto en memoria y esto es pesado.
                .authorizeHttpRequests(http -> {
                    //configurar los endpoint publicos
                    http.requestMatchers("/auth/**").permitAll();

                    //configurar los endpoint privados
                    //http.requestMatchers(HttpMethod.POST, "/auth/post").hasAnyAuthority("CREATE", "READ");
                    http.requestMatchers(HttpMethod.POST, "/method/post").hasRole("ADMIN");
                    http.requestMatchers(HttpMethod.PATCH, "/method/patch").hasAuthority("REFACTOR");
                    http.requestMatchers(HttpMethod.GET, "/method/get").hasAnyRole("INVITED", "TEST");

                    //configurar el resto de endpoint - NO ESPECIFIDOS
                    http.anyRequest().denyAll();
                })
                .addFilterBefore(new JwtValidator(jwtUtils), BasicAuthenticationFilter.class)
                .build();
    }

    //Configuramos el Authentication Manager
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    //Configura del Authentication Provider, se pasa por parametro el servicio implementado personalizado
    //Mediante el Servicio UserDetailsServiceImpl se conecta a la base de datos para buscar los usuarios
    @Bean
    public AuthenticationProvider authenticationProvider(UserDetailsServiceImpl userDetailsServiceImpl){
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();//Necesita el Password Encode (que es el que encripta las password)
                                                                            // y El User Details Service (que es el que llama a las dase de datos)
        provider.setPasswordEncoder(passwordEncoder());
        provider.setUserDetailsService(userDetailsServiceImpl);
        return provider;
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

}

package com.example.security;

import com.example.security.filters.JwtAuthenticationFilter;
import com.example.security.filters.JwtAuthorizationFilter;
import com.example.security.jwt.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.proxy.NoOp;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Autowired
    JwtUtils jwtUtils;

    @Autowired
    UserDetailsService userDetailsService;  //lo vamos a pasar como parametro con los datos del usuario

    @Autowired
    JwtAuthorizationFilter authorizationFilter;

    //Configuramos el comportamiento de acceso a nuestros endpoins, el manejo de la sesion con autenticacion bsaica
    //Metodo que configura la cadena de filtros, osesa, la seguridad. Con httpSecurity configuramos la seguridad
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity, AuthenticationManager authenticationManager) throws Exception {

        JwtAuthenticationFilter jwtAuthenticationFilter = new JwtAuthenticationFilter(jwtUtils); //creamos
        jwtAuthenticationFilter.setAuthenticationManager(authenticationManager);
        jwtAuthenticationFilter.setFilterProcessesUrl("/login");

        return httpSecurity
                .csrf(config -> config.disable())  //no trabajamos con formularios
                .authorizeHttpRequests(auth -> {  //configuramos el acceso a las url y endpoins
                    auth.requestMatchers("/hello").permitAll();  //cuando accedo a /hello, cualquiera puede acceder. endpoin publico
                    //auth.requestMatchers("/accessAdmin").hasAnyRole("ADMIN","USER");
                    auth.anyRequest().authenticated();  //cualquier otra ruta tiene que estar autenticada
                })
                .sessionManagement(session -> {  //configuramos la adminisstracion de la sesion
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS);  //politica de creacion de la sesion, stateless
                })
                .addFilter(jwtAuthenticationFilter)
                .addFilterBefore(authorizationFilter, UsernamePasswordAuthenticationFilter.class)
                //.httpBasic()  //para autentificacion basica
                //.and()
                .build();
    }

//    //Creamos un usuario en memoria
//    @Bean
//    UserDetailsService userDetailsService(){
//        InMemoryUserDetailsManager manager = new InMemoryUserDetailsManager();
//        manager.createUser(User.withUsername("santiago")
//                .password("1234")
//                        .roles()
//                .build());
//
//        return manager;
//    }


    //politica de encriptacion de contraseña. BCryp... es un algoritmp de encriptacion potente
    @Bean
    PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    //para que el usuario pueda funcionar tiene que ser administrado por un objeto que administre la autenticacion. esto es el authentication manager. Este nos exije usar un password encoder
    @Bean
    AuthenticationManager authenticationManager(HttpSecurity httpSecurity, PasswordEncoder passwordEncoder) throws Exception {
        return httpSecurity.getSharedObject(AuthenticationManagerBuilder.class)
                .userDetailsService(userDetailsService)
                .passwordEncoder(passwordEncoder)
                .and().build();
    }

//    public static void main(String[] args) {
//        System.out.println(new BCryptPasswordEncoder().encode("1234"));
//    }

}

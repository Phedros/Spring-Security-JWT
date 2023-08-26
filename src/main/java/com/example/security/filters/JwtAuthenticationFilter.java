package com.example.security.filters;

import com.example.models.UserEntity;
import com.example.security.jwt.JwtUtils;
import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {  //extension que nos ayuda autenticarnos en nuestra aplicacion. Nos da un path. utiliza peticiones tipo Post


    private JwtUtils jwtUtils;

    //constructor para inyectar nuestro JwtUtils
    public JwtAuthenticationFilter(JwtUtils jwtUtils){
        this.jwtUtils = jwtUtils;
    }

    //cuando intenta autenticarse
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request,
                                                HttpServletResponse response) throws AuthenticationException {

        UserEntity userEntity = null;
        String username = "";
        String password = "";

        try{                           //mapeamos el request.getInputStream() a UserEntity.class
            userEntity = new ObjectMapper().readValue(request.getInputStream(), UserEntity.class);  //mapeamos el Json con una libreria que viene con Sprin (Jackson)
            username = userEntity.getUsername();
            password = userEntity.getPassword();

        } catch (StreamReadException e) {
            throw new RuntimeException(e);
        } catch (DatabindException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // nosotros vamos a autenticarnos en la aplicacion
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(username, password);

        //metodo para autenticarse
        return getAuthenticationManager().authenticate(authenticationToken);
    }

    //cuando se logra autenticar en nuestra aplicacion
    @Override
    protected void successfulAuthentication(HttpServletRequest request,
                                            HttpServletResponse response,
                                            FilterChain chain,
                                            Authentication authResult) throws IOException, ServletException {
        //debemos obtener los detalles del usuario. Usuario, contraseña, roles
        User user = (User) authResult.getPrincipal(); //User de SpringSecurity. aqui obtenemos el objeto con los detalles del usuario
        String token = jwtUtils.generateAccesToken(user.getUsername());  //generamos el token desde el username del usuario

        response.addHeader("Authorization", token);  //En el Header de la respuesta enviamos el token

        Map<String, Object> httpResponse = new HashMap<>();  //aqui mapeamos la respuesta y la convertimos en un Json
        httpResponse.put("token", token);
        httpResponse.put("Message", "Autenticacion Correcta");
        httpResponse.put("Username", user.getUsername());

        response.getWriter().write(new ObjectMapper().writeValueAsString(httpResponse));  //escribimos la respuesta
        response.setStatus(HttpStatus.OK.value());  //status de respuesta
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);  //contenido de la respuesta "application/json"
        response.getWriter().flush();  //nos aseguramos de que todo se escriba correctamente

        super.successfulAuthentication(request, response, chain, authResult);
    }
}

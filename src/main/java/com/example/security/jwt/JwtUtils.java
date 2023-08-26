package com.example.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.function.Function;

@Component
@Slf4j  //creo que para getMessage del exception del token en la validacion del mismo
public class JwtUtils {  //provee los metodos necesarios para trabajar con JWT

    @Value("${jwt.secret.key}")  //ponemos el parametro del application.propertis1
    private String secretKey;  //ayuda a firmar nuestro metodo para validar un mejor permiso. Si un atacante genera un token sin la firma, este no es valido

    @Value("${jwt.time.expiration}")
    private String timeExpiration;

    //Generar token de acceso
    public String generateAccesToken(String username){
        return Jwts.builder()
                .setSubject(username)  //usuario que genera el token
                .setIssuedAt(new Date(System.currentTimeMillis()))  //fecha de creacion del token
                .setExpiration(new Date(System.currentTimeMillis() + Long.parseLong(timeExpiration)))
                .signWith(getSignatureKey(), SignatureAlgorithm.HS256)  //vuelve a encriptar la firma con otro algoritmo de encriptacion
                .compact();
    }

    //Validar el token de acceso
    public boolean isTokenValid(String token){
        try{
            Jwts.parserBuilder()  //lee el token
                    .setSigningKey(getSignatureKey())  //primero vemos si la firma es correcta
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return true;
        }catch (Exception e){  //si salta exception el token es invalido
            log.error("Token invalido, error: ".concat(e.getMessage()));
            return false;
        }
    }

    //Obtener el username del tolen
    public String getUsernameFromToken(String token){
        return getClaim(token, Claims::getSubject);
    }

    //Obtener un solo claim
    public <T> T getClaim(String token, Function<Claims, T> claimsTFunction){
        Claims claims = extractAllCraims(token);
        return claimsTFunction.apply(claims);
    }

    //obtener todos los claims del token
    public Claims extractAllCraims(String token){
        return Jwts.parserBuilder()
                .setSigningKey(getSignatureKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    //obtener firma del token
    public Key getSignatureKey(){
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);  //decodificamos la clave secretKey y luego la volvemos a encriptar en otro algoritmo de encriptacion
        return Keys.hmacShaKeyFor(keyBytes);
    }

}

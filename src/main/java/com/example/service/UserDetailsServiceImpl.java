package com.example.service;

import com.example.models.UserEntity;
import com.example.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.stream.Collectors;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {  //implementar metodos dentro de UserDetailService

    @Autowired
    private UserRepository userRepository;  //lo inyectamos para que consulte el usuario a la base de datos

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {   //para asegurarse cual va a ser el usuario que se va a consultar

        UserEntity userEntity = userRepository.findByUsername(username)  //recuperamos el usuario de base de datos
                .orElseThrow(() -> new UsernameNotFoundException("El usuario " + username + " no existe."));

        Collection<? extends GrantedAuthority> authorities = userEntity.getRoles()
                .stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_".concat(role.getName().name())))
                .collect(Collectors.toSet());  //getRoles nos trae un Set<RoleEntity>, con stream().map() y con lambda vamos creando SimpleGrantedAuthority(la autorizacion de spring security) y le agregamos ROLE_ adelante. Luego .collect toSet para hacer un set

        return new User(userEntity.getUsername(),  //devolvemos un User de SpringSecurity, pasandoles usuario, contraseña, 4 booleans y una lista de grantet authorities, que son los permisos o roles que vamos a utilizar
                userEntity.getPassword(),
                true,
                true,
                true,
                true,
                authorities);  //lo creamos arriba
    }

}

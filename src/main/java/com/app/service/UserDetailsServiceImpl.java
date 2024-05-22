package com.app.service;

import com.app.controller.dto.AuthCreateUserRequest;
import com.app.controller.dto.AuthLoginRequest;
import com.app.controller.dto.AuthResponse;
import com.app.repository.RepositoryUser;
import com.app.repository.RoleRepository;
import com.app.repository.entity.RolEntity;
import com.app.repository.entity.UserEntity;
import com.app.util.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private RepositoryUser repositoryUser;

    @Autowired
    private RoleRepository roleRepository;

    //Busca el usuario en la base de datos
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity userEntity = repositoryUser.findUserEntitiesByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("The user: " + username + " don´t exist."));

        List<SimpleGrantedAuthority> authorityList = new ArrayList<>();
        userEntity.getRolEntities()
                .forEach(role -> authorityList.add(new SimpleGrantedAuthority("ROLE_".concat(role.getRoleEnum().name()))));
        userEntity.getRolEntities().stream()
                .flatMap(role -> role.getPermissionList().stream())
                .forEach(permission -> authorityList.add(new SimpleGrantedAuthority(permission.getName())));

        return new User(userEntity.getUsername(),
                userEntity.getPassword(),
                userEntity.getIsEnabled(),
                userEntity.getIsAccountNoExpired(),
                userEntity.getIsCredentialNoExpired(),
                userEntity.getIsAccountNoLocked(),
                authorityList);
    }

    //método para hacer login
    public AuthResponse loginUser (AuthLoginRequest authLoginRequest){
        String username = authLoginRequest.username();
        String password = authLoginRequest.password();
        Authentication authentication = this.authenticate(username, password);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String accessToken = jwtUtils.createToken(authentication);

        AuthResponse response = new AuthResponse(username, "User Loged successfuly", accessToken, true);
        return response;
    }

    //método para autenticar usuario
    public Authentication authenticate(String username, String password){
        UserDetails userDetails = this.loadUserByUsername(username);
        if(userDetails == null){
            throw new BadCredentialsException("Invalid username or password");
        }

        if (!passwordEncoder.matches(password, userDetails.getPassword())){
            throw new BadCredentialsException("Invalid password");
        }
        return new UsernamePasswordAuthenticationToken(username, userDetails.getPassword(), userDetails.getAuthorities());
    }

    //método para registrar usuario
    public AuthResponse createUser(AuthCreateUserRequest authCreateUserRequest){
        String username = authCreateUserRequest.username();
        String password = authCreateUserRequest.password();
        List<String> roleRequest = authCreateUserRequest.roleRequest().roleListName();

        Set<RolEntity> rolEntitiesSet = roleRepository.findRolEntitiesByRoleEnumIn(roleRequest)
                                        .stream()
                                        .collect(Collectors.toSet());
        if(rolEntitiesSet.isEmpty()){
            throw new IllegalArgumentException("The roles specified do not exist");
        }

        UserEntity userEntity = UserEntity.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .rolEntities(rolEntitiesSet)
                .isEnabled(true)
                .isAccountNoLocked(true)
                .isAccountNoExpired(true)
                .isCredentialNoExpired(true)
                .build();

        UserEntity userCreated = repositoryUser.save(userEntity);

        ArrayList<SimpleGrantedAuthority> authorityList = new ArrayList<>();

        //Se agregan los roles al authority list
        userCreated.getRolEntities().forEach(role ->{
            authorityList.add(new SimpleGrantedAuthority("ROLE_".concat(role.getRoleEnum().name())));
        });

        //Se agregan los permisos al authority list
        userCreated.getRolEntities()
                .stream()
                .flatMap(role -> role.getPermissionList().stream())
                .forEach(permission -> {
                    authorityList.add(new SimpleGrantedAuthority(permission.getName()));
                });
        //SecurityContext context = SecurityContextHolder.getContext();
        Authentication authentication = new UsernamePasswordAuthenticationToken(userCreated.getUsername(), userCreated.getPassword(), authorityList);

        String accessToken = jwtUtils.createToken(authentication);
        AuthResponse authResponse = new AuthResponse(userCreated.getUsername(), "User created successfully", accessToken, true);
        return authResponse;
    }
}

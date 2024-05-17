package com.app.service;

import com.app.repository.RepositoryUser;
import com.app.repository.entity.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private RepositoryUser repositoryUser;
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
}

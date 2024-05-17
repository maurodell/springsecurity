package com.app.repository;

import com.app.repository.entity.UserEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RepositoryUser extends CrudRepository<UserEntity, Long> {

    Optional<UserEntity> findUserEntitiesByUsername(String userName);
}

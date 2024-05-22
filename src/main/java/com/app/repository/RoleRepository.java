package com.app.repository;

import com.app.repository.entity.RolEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoleRepository extends CrudRepository<RolEntity, Long> {

    List<RolEntity> findRolEntitiesByRoleEnumIn(List<String> roleNames);
}

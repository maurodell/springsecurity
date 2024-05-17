package com.app;

import com.app.config.Config;
import com.app.repository.RepositoryUser;
import com.app.repository.entity.PermissionEntity;
import com.app.repository.entity.RolEntity;
import com.app.repository.entity.RoleEnum;
import com.app.repository.entity.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.List;
import java.util.Set;

@SpringBootApplication
public class SpringSecurityAppApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringSecurityAppApplication.class, args);
	}

	@Autowired
	private Config config;

	@Bean
	CommandLineRunner init(RepositoryUser repositoryUser){
		return args -> {
			//Primer paso crear permisos
			PermissionEntity createPermission = PermissionEntity.builder()
					.name("CREATE")
					.build();
			PermissionEntity readPermission = PermissionEntity.builder()
					.name("READ")
					.build();
			PermissionEntity updatePermission = PermissionEntity.builder()
					.name("UPDATE")
					.build();
			PermissionEntity deletePermission = PermissionEntity.builder()
					.name("DELETE")
					.build();
			PermissionEntity refactorPermission = PermissionEntity.builder()
					.name("REFACTOR")
					.build();

			//Crear roles
			RolEntity roleAdmin = RolEntity.builder()
					.roleEnum(RoleEnum.ADMIN)
					.permissionList(Set.of(createPermission, readPermission, updatePermission,
							deletePermission))
					.build();
			RolEntity roleUser = RolEntity.builder()
					.roleEnum(RoleEnum.USER)
					.permissionList(Set.of(createPermission, readPermission))
					.build();
			RolEntity roleTest = RolEntity.builder()
					.roleEnum(RoleEnum.TEST)
					.permissionList(Set.of(readPermission))
					.build();
			RolEntity roleDeveloper = RolEntity.builder()
					.roleEnum(RoleEnum.DEVELOPER)
					.permissionList(Set.of(createPermission, readPermission, updatePermission,
							deletePermission, refactorPermission))
					.build();

			//Crear User
			UserEntity userPepe = UserEntity.builder()
					.username("Pepe")
					.password(config.passUser)
					.isEnabled(true)
					.isAccountNoExpired(true)
					.isAccountNoLocked(true)
					.isCredentialNoExpired(true)
					.rolEntities(Set.of(roleAdmin, roleDeveloper))
					.build();
			UserEntity userPedro = UserEntity.builder()
					.username("Pedro")
					.password(config.passUser)
					.isEnabled(true)
					.isCredentialNoExpired(true)
					.isAccountNoLocked(true)
					.isAccountNoExpired(true)
					.rolEntities(Set.of(roleTest))
					.build();
			repositoryUser.saveAll(List.of(userPepe, userPedro));
		};
	}
}

package ec.edu.ups.icc.proyect.security.repository;

import ec.edu.ups.icc.proyect.security.entity.RoleEntity;
import ec.edu.ups.icc.proyect.security.enums.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<RoleEntity, Long> {
    Optional<RoleEntity> findByName(RoleName name);
}
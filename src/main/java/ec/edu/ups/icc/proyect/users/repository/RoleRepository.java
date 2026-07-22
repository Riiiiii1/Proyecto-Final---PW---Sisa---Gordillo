package ec.edu.ups.icc.proyect.users.repository;

import ec.edu.ups.icc.proyect.users.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByName(Role.RoleName name);
}
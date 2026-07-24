package ec.edu.ups.icc.proyect.security.config;

import ec.edu.ups.icc.proyect.security.entity.RoleEntity;
import ec.edu.ups.icc.proyect.security.enums.RoleName;
import ec.edu.ups.icc.proyect.security.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class SecurityDataInitializer implements CommandLineRunner {

    private static final Map<RoleName, String> DEFAULT_DESCRIPTIONS = Map.of(
            RoleName.ADMIN, "Administra usuarios, roles, categorías, estados y reportes generales.",
            RoleName.ORGANIZER, "Gestiona únicamente sus eventos, sesiones e inscripciones.",
            RoleName.PARTICIPANT, "Consulta eventos y gestiona sus propias inscripciones."
    );

    private final RoleRepository roleRepository;

    @Override
    public void run(String... args) {
        for (RoleName roleName : RoleName.values()) {
            if (roleRepository.findByName(roleName).isEmpty()) {
                log.warn("Rol {} no encontrado en la base de datos. Creándolo automáticamente...", roleName);

                RoleEntity role = RoleEntity.builder()
                        .name(roleName)
                        .description(DEFAULT_DESCRIPTIONS.get(roleName))
                        .build();

                roleRepository.save(role);

                log.info("Rol {} creado correctamente.", roleName);
            }
        }
    }
}
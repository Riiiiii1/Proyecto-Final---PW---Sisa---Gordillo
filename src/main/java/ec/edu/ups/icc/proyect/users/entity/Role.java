package ec.edu.ups.icc.proyect.users.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

/*
 * Mapea la tabla "roles".
 * El check constraint chk_roles_name en la base de datos ya restringe
 * los valores posibles; aquí usamos un enum para tener seguridad de
 * tipos también en el lado de Java.
 */
@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Role {

    public enum RoleName {
        ADMIN, ORGANIZER, PARTICIPANT
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "name", length = 30, nullable = false, unique = true)
    private RoleName name;

    @Column(name = "description", length = 150, nullable = false)
    private String description;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime createdAt;
}
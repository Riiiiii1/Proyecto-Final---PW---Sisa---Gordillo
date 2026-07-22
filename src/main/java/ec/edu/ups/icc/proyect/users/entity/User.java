package ec.edu.ups.icc.proyect.users.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;

/*
 * Mapea la tabla "users".
 *
 * La relación con roles es Many-to-Many a través de la tabla intermedia
 * "user_roles". No creamos una entidad @Entity separada para user_roles
 * porque no necesitamos consultar assigned_at por ahora; Hibernate maneja
 * la tabla puente automáticamente con @JoinTable, y la columna
 * assigned_at se llena sola con su DEFAULT CURRENT_TIMESTAMP en la BD.
 *
 * Si más adelante necesitas consultar/ordenar por assigned_at, ahí sí
 * conviene reemplazar esto por una entidad UserRole con clave compuesta.
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    public enum Status {
        ACTIVE, BLOCKED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name", length = 80, nullable = false)
    private String firstName;

    @Column(name = "last_name", length = 80, nullable = false)
    private String lastName;

    @Column(name = "email", length = 160, nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", length = 255, nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private Status status;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime updatedAt;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @Builder.Default
    private Set<Role> roles = new HashSet<>();
}
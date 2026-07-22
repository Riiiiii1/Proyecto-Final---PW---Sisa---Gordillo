package ec.edu.ups.icc.proyect.users.entity;

/*
 * NOTA: La tabla "user_roles" se maneja mediante la relación
 * @ManyToMany + @JoinTable definida en User.java (ver campo "roles").
 *
 * No se necesita una entidad @Entity explícita para user_roles a menos
 * que en el futuro se requiera consultar/ordenar por la columna
 * assigned_at directamente. Si llega ese caso, aquí se reemplazaría
 * esta clase por una entidad con @IdClass o @EmbeddedId (userId, roleId)
 * + campo assignedAt.
 *
 * Se deja este archivo sin @Entity a propósito para no duplicar el
 * mapeo de la tabla puente y evitar conflictos con Hibernate.
 */
public class UserRole {
}
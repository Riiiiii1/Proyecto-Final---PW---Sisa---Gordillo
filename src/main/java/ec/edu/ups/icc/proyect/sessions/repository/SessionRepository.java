package ec.edu.ups.icc.proyect.sessions.repository;

import ec.edu.ups.icc.proyect.sessions.entity.SessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SessionRepository extends JpaRepository<SessionEntity, Long> {


    List<SessionEntity> findByEventId(Long eventId);

    /**
     * Verifica si existe alguna sesión que se superponga o use el mismo nombre
     * dentro de un mismo evento, útil para reglas de negocio.
     */
    boolean existsByTitleAndEventId(String title, Long eventId);
}
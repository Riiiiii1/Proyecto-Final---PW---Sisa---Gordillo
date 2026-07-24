package ec.edu.ups.icc.proyect.events.repository;

import ec.edu.ups.icc.proyect.events.entity.EventEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<EventEntity, Long> {

    // Método para buscar eventos que no estén eliminados lógicamente
    Page<EventEntity> findByDeletedFalse(Pageable pageable);

    // Método para buscar un evento específico asegurando que no esté eliminado
    Optional<EventEntity> findByIdAndDeletedFalse(Long id);

    // Método para validar que un evento pertenezca a un organizador específico
    Optional<EventEntity> findByIdAndOrganizerIdAndDeletedFalse(Long id, Long organizerId);
}
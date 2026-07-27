package ec.edu.ups.icc.proyect.registrations.repository;

import ec.edu.ups.icc.proyect.registrations.entity.RegistrationEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RegistrationRepository extends JpaRepository<RegistrationEntity, Long> {

    boolean existsByEvent_IdAndParticipant_Id(Long eventId, Long participantId);

    Optional<RegistrationEntity> findByEvent_IdAndParticipant_Id(Long eventId, Long participantId);

    Page<RegistrationEntity> findByParticipant_Id(Long participantId, Pageable pageable);

    Page<RegistrationEntity> findByEvent_Id(Long eventId, Pageable pageable);
}
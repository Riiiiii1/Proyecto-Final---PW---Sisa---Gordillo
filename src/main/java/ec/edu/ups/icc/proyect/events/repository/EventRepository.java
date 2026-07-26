package ec.edu.ups.icc.proyect.events.repository;

import ec.edu.ups.icc.proyect.events.entity.EventEntity;
import ec.edu.ups.icc.proyect.events.enums.EventStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<EventEntity, Long> {
    Optional<EventEntity> findByIdAndDeletedFalse(Long id);
    Optional<EventEntity> findByTitleIgnoreCaseAndDeletedFalse(String title);
    @Query("""
            SELECT e
            FROM EventEntity e
            WHERE e.deleted = false
              AND (:title IS NULL OR LOWER(e.title) LIKE LOWER(CONCAT('%', :title, '%')))
              AND (:categoryId IS NULL OR e.category.id = :categoryId)
              AND (:status IS NULL OR e.status = :status)
            """)
    Page<EventEntity> findPageWithFilters(
            @Param("title") String title,
            @Param("categoryId") Long categoryId,
            @Param("status") EventStatus status,
            Pageable pageable
    );

    @Query("""
            SELECT e
            FROM EventEntity e
            WHERE e.deleted = false
              AND (:title IS NULL OR LOWER(e.title) LIKE LOWER(CONCAT('%', :title, '%')))
              AND (:categoryId IS NULL OR e.category.id = :categoryId)
              AND (:status IS NULL OR e.status = :status)
            """)
    Slice<EventEntity> findSliceWithFilters(
            @Param("title") String title,
            @Param("categoryId") Long categoryId,
            @Param("status") EventStatus status,
            Pageable pageable
    );
}
package ec.edu.ups.icc.proyect.categories.service;

import ec.edu.ups.icc.proyect.categories.dto.CategoryResponseDTO;
import ec.edu.ups.icc.proyect.categories.dto.CreateCategoryDTO;
import ec.edu.ups.icc.proyect.categories.dto.UpdateCategoryDTO;
import ec.edu.ups.icc.proyect.categories.dto.UpdateCategoryStatusDTO;
import ec.edu.ups.icc.proyect.categories.entity.CategoryEntity;
import ec.edu.ups.icc.proyect.categories.mapper.CategoryMapper;
import ec.edu.ups.icc.proyect.categories.repository.CategoryRepository;
import ec.edu.ups.icc.proyect.core.dto.PaginationDTO;
import ec.edu.ups.icc.proyect.core.exception.domain.BadRequestException;
import ec.edu.ups.icc.proyect.core.exception.domain.ConflictException;
import ec.edu.ups.icc.proyect.core.exception.domain.NotFoundException;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.flywaydb.core.internal.util.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
// Con Lombok puedo utilizar esta notación para inyectar dependencias sin tener que utiliar el Autowired.
// Es buena practica.
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService{

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    private final EntityManager entityManager;


    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "id", "name", "active", "createdAt", "updatedAt"
    );



    @Override
    @Transactional
    public CategoryResponseDTO create(CreateCategoryDTO dto) {
        if (categoryRepository.existsByNameIgnoreCase(dto.name())) {
            throw new ConflictException("DUPLICATE_RESOURCE",
                    "Ya existe una categoría con el nombre '" + dto.name() + "'");
        }

        CategoryEntity entity = categoryMapper.toEntity(dto);
        entity = categoryRepository.save(entity);
        entityManager.refresh(entity);

        return categoryMapper.toResponseDto(entity);
    }

    @Override
    @Transactional
    public CategoryResponseDTO update(Long id, UpdateCategoryDTO dto) {
        CategoryEntity entity = findEntityOrThrow(id);

        if (!entity.getName().equalsIgnoreCase(dto.name())
                && categoryRepository.existsByNameIgnoreCase(dto.name())) {
            throw new ConflictException("DUPLICATE_RESOURCE",
                    "Ya existe una categoría con el nombre '" + dto.name() + "'");
        }

        entity.setName(dto.name());
        entity.setDescription(dto.description());

        entity = categoryRepository.save(entity);
        entityManager.refresh(entity);

        return categoryMapper.toResponseDto(entity);
    }

    @Override
    @Transactional
    public CategoryResponseDTO updateStatus(Long id, UpdateCategoryStatusDTO dto) {
        CategoryEntity entity = findEntityOrThrow(id);
        entity.setActive(dto.active());

        entity = categoryRepository.save(entity);
        entityManager.refresh(entity);

        return categoryMapper.toResponseDto(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponseDTO findById(Long id) {
        return categoryMapper.toResponseDto(findEntityOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CategoryResponseDTO> findAll(String search, Boolean active, PaginationDTO pagination) {
        Pageable pageable = createPageable(pagination);
        return categoryRepository.search(search, active, pageable)
                .map(categoryMapper::toResponseDto);
    }

    private CategoryEntity findEntityOrThrow(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Categoría con id " + id + " no encontrada"));
    }

    private Pageable createPageable(PaginationDTO pagination) {
        String sortBy = normalizeSortBy(pagination.getSortBy());
        Sort.Direction direction = normalizeDirection(pagination.getDirection());
        Sort sort = Sort.by(direction, sortBy);
        return PageRequest.of(pagination.getPage(), pagination.getSize(), sort);
    }

    private String normalizeSortBy(String sortBy) {
        if (sortBy == null || sortBy.isBlank()) {
            return "id";
        }
        if (!ALLOWED_SORT_FIELDS.contains(sortBy)) {
            throw new BadRequestException("Campo de ordenamiento no permitido: " + sortBy);
        }
        return sortBy;
    }

    private Sort.Direction normalizeDirection(String direction) {
        if (direction == null || direction.isBlank()) {
            return Sort.Direction.ASC;
        }
        if (direction.equalsIgnoreCase("asc")) {
            return Sort.Direction.ASC;
        }
        if (direction.equalsIgnoreCase("desc")) {
            return Sort.Direction.DESC;
        }
        throw new BadRequestException("Dirección de ordenamiento no válida: " + direction);
    }
}

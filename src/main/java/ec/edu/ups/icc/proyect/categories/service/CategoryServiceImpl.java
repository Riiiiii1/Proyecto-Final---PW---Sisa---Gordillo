package ec.edu.ups.icc.proyect.categories.service;

import ec.edu.ups.icc.proyect.categories.dto.CategoryResponseDTO;
import ec.edu.ups.icc.proyect.categories.dto.CreateCategoryDTO;
import ec.edu.ups.icc.proyect.categories.dto.UpdateCategoryDTO;
import ec.edu.ups.icc.proyect.categories.dto.UpdateCategoryStatusDTO;
import ec.edu.ups.icc.proyect.categories.mapper.CategoryMapper;
import ec.edu.ups.icc.proyect.categories.repository.CategoryRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
// Con Lombok puedo utilizar esta notación para inyectar dependencias sin tener que utiliar el Autowired.
// Es buena practica.
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService{

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    private final EntityManager entityManager;


    @Override
    public CategoryResponseDTO create(CreateCategoryDTO dto) {
        return null;
    }

    @Override
    public CategoryResponseDTO update(Long id, UpdateCategoryDTO dto) {
        return null;
    }

    @Override
    public CategoryResponseDTO updateStatus(Long id, UpdateCategoryStatusDTO dto) {
        return null;
    }

    @Override
    public CategoryResponseDTO findById(Long id) {
        return null;
    }

    @Override
    public Page<CategoryResponseDTO> findAll(String search, Boolean active, Pageable pageable) {
        return null;
    }
}

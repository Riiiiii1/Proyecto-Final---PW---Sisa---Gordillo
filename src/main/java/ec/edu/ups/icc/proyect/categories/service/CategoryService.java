package ec.edu.ups.icc.proyect.categories.service;

import ec.edu.ups.icc.proyect.categories.dto.CategoryResponseDTO;
import ec.edu.ups.icc.proyect.categories.dto.CreateCategoryDTO;
import ec.edu.ups.icc.proyect.categories.dto.UpdateCategoryDTO;
import ec.edu.ups.icc.proyect.categories.dto.UpdateCategoryStatusDTO;
import ec.edu.ups.icc.proyect.core.dto.PaginationDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CategoryService {


    CategoryResponseDTO create(CreateCategoryDTO dto);

    CategoryResponseDTO update(Long id, UpdateCategoryDTO dto);

    CategoryResponseDTO updateStatus(Long id, UpdateCategoryStatusDTO dto);

    CategoryResponseDTO findById(Long id);

    Page<CategoryResponseDTO> findAll(String search, Boolean active, PaginationDTO pagination);

}

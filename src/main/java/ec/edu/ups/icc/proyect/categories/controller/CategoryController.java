package ec.edu.ups.icc.proyect.categories.controller;

import ec.edu.ups.icc.proyect.categories.dto.CategoryResponseDTO;
import ec.edu.ups.icc.proyect.categories.dto.CreateCategoryDTO;
import ec.edu.ups.icc.proyect.categories.dto.UpdateCategoryDTO;
import ec.edu.ups.icc.proyect.categories.dto.UpdateCategoryStatusDTO;
import ec.edu.ups.icc.proyect.categories.service.CategoryService;
import ec.edu.ups.icc.proyect.core.dto.PaginationDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    public ResponseEntity<CategoryResponseDTO> create(@Valid @RequestBody CreateCategoryDTO dto) {
        CategoryResponseDTO created = categoryService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponseDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCategoryDTO dto
    ) {
        return ResponseEntity.ok(categoryService.update(id, dto));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<CategoryResponseDTO> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCategoryStatusDTO dto
    ) {
        return ResponseEntity.ok(categoryService.updateStatus(id, dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponseDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(categoryService.findById(id));
    }

    @GetMapping
    public ResponseEntity<Page<CategoryResponseDTO>> findAll(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean active,
            @ModelAttribute PaginationDTO pagination
    ) {
        return ResponseEntity.ok(categoryService.findAll(search, active, pagination));
    }
}
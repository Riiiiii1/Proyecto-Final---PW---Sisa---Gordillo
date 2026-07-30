package ec.edu.ups.icc.proyect.categories.controller;

import ec.edu.ups.icc.proyect.categories.dto.CategoryResponseDTO;
import ec.edu.ups.icc.proyect.categories.dto.CreateCategoryDTO;
import ec.edu.ups.icc.proyect.categories.dto.UpdateCategoryDTO;
import ec.edu.ups.icc.proyect.categories.dto.UpdateCategoryStatusDTO;
import ec.edu.ups.icc.proyect.categories.service.CategoryService;
import ec.edu.ups.icc.proyect.core.dto.PaginationDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
@Tag(name = "Categories", description = "Gestión de categorías de eventos")
@SecurityRequirement(name = "bearerAuth")
public class CategoryController {

    private final CategoryService categoryService;

    @Operation(summary = "Crear una nueva categoría", description = "Permite a un ADMIN registrar una nueva categoría de eventos.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Categoría creada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Errores de validación"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado (requiere rol ADMIN)"),
            @ApiResponse(responseCode = "409", description = "Ya existe una categoría con ese nombre")
    })
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryResponseDTO> create(@Valid @RequestBody CreateCategoryDTO dto) {
        CategoryResponseDTO created = categoryService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(summary = "Actualizar una categoría", description = "Permite a un ADMIN modificar nombre y descripción de una categoría existente.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Categoría actualizada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Errores de validación"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado (requiere rol ADMIN)"),
            @ApiResponse(responseCode = "404", description = "Categoría no encontrada"),
            @ApiResponse(responseCode = "409", description = "Ya existe una categoría con ese nombre")
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryResponseDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCategoryDTO dto
    ) {
        return ResponseEntity.ok(categoryService.update(id, dto));
    }

    @Operation(summary = "Cambiar el estado de una categoría", description = "Permite a un ADMIN activar o desactivar una categoría.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Estado actualizado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Errores de validación"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado (requiere rol ADMIN)"),
            @ApiResponse(responseCode = "404", description = "Categoría no encontrada")
    })
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryResponseDTO> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCategoryStatusDTO dto
    ) {
        return ResponseEntity.ok(categoryService.updateStatus(id, dto));
    }

    @Operation(summary = "Obtener una categoría por ID", description = "Consulta los detalles de una categoría específica.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Categoría encontrada"),
            @ApiResponse(responseCode = "404", description = "Categoría no encontrada")
    })
    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponseDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(categoryService.findById(id));
    }

    @Operation(summary = "Listar categorías (Paginado)", description = "Devuelve una lista paginada de categorías con filtros opcionales de búsqueda y estado.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de categorías recuperada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Parámetros de paginación inválidos")
    })
    @GetMapping
    public ResponseEntity<Page<CategoryResponseDTO>> findAll(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean active,
            @ModelAttribute PaginationDTO pagination
    ) {
        return ResponseEntity.ok(categoryService.findAll(search, active, pagination));
    }
}
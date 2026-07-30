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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CategoryMapper categoryMapper;

    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private CategoryEntity mockEntity;
    private CategoryResponseDTO mockResponseDto;

    @BeforeEach
    void setUp() {
        mockEntity = CategoryEntity.builder()
                .id(1L)
                .name("Tecnología")
                .description("Eventos de tecnología")
                .active(true)
                .build();

        mockResponseDto = new CategoryResponseDTO(
                1L, "Tecnología", "Eventos de tecnología", true, null, null
        );
    }


    @Test
    void create_deberiaCrearCategoria_cuandoNombreNoExiste() {
        CreateCategoryDTO dto = new CreateCategoryDTO("Tecnología", "Eventos de tecnología");

        when(categoryRepository.existsByNameIgnoreCase(dto.name())).thenReturn(false);
        when(categoryMapper.toEntity(dto)).thenReturn(mockEntity);
        when(categoryRepository.save(any(CategoryEntity.class))).thenReturn(mockEntity);
        doNothing().when(entityManager).refresh(any());
        when(categoryMapper.toResponseDto(mockEntity)).thenReturn(mockResponseDto);

        CategoryResponseDTO result = categoryService.create(dto);

        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo("Tecnología");
        verify(categoryRepository).save(any(CategoryEntity.class));
        verify(entityManager).refresh(mockEntity);
    }

    @Test
    void create_deberiaLanzarConflictException_cuandoNombreYaExiste() {
        CreateCategoryDTO dto = new CreateCategoryDTO("Tecnología", "Eventos de tecnología");

        when(categoryRepository.existsByNameIgnoreCase(dto.name())).thenReturn(true);

        assertThatThrownBy(() -> categoryService.create(dto))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Ya existe una categoría");

        verify(categoryRepository, never()).save(any());
    }

    @Test
    void update_deberiaActualizarCategoria_cuandoDatosValidos() {
        UpdateCategoryDTO dto = new UpdateCategoryDTO("Ciencias", "Nueva descripción");

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(mockEntity));
        when(categoryRepository.existsByNameIgnoreCase(dto.name())).thenReturn(false);
        when(categoryRepository.save(any(CategoryEntity.class))).thenReturn(mockEntity);
        doNothing().when(entityManager).refresh(any());

        CategoryResponseDTO updatedResponse = new CategoryResponseDTO(1L, "Ciencias", "Nueva descripción", true, null, null);
        when(categoryMapper.toResponseDto(mockEntity)).thenReturn(updatedResponse);

        CategoryResponseDTO result = categoryService.update(1L, dto);

        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo("Ciencias");
        verify(categoryRepository).save(mockEntity);
    }

    @Test
    void update_deberiaLanzarNotFoundException_cuandoCategoriaNoExiste() {
        UpdateCategoryDTO dto = new UpdateCategoryDTO("Ciencias", "Nueva descripción");
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.update(99L, dto))
                .isInstanceOf(NotFoundException.class);

        verify(categoryRepository, never()).save(any());
    }

    @Test
    void update_deberiaLanzarConflictException_cuandoNuevoNombreYaExisteEnOtraCategoria() {
        UpdateCategoryDTO dto = new UpdateCategoryDTO("Matemáticas", "Nueva descripción");

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(mockEntity));
        when(categoryRepository.existsByNameIgnoreCase(dto.name())).thenReturn(true);

        assertThatThrownBy(() -> categoryService.update(1L, dto))
                .isInstanceOf(ConflictException.class);

        verify(categoryRepository, never()).save(any());
    }


    @Test
    void updateStatus_deberiaActualizarEstado_cuandoExiste() {
        UpdateCategoryStatusDTO dto = new UpdateCategoryStatusDTO(false);

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(mockEntity));
        when(categoryRepository.save(any(CategoryEntity.class))).thenReturn(mockEntity);
        doNothing().when(entityManager).refresh(any());

        CategoryResponseDTO statusResponse = new CategoryResponseDTO(1L, "Tecnología", "Eventos", false, null, null);
        when(categoryMapper.toResponseDto(mockEntity)).thenReturn(statusResponse);

        CategoryResponseDTO result = categoryService.updateStatus(1L, dto);

        assertThat(result.active()).isFalse();
        verify(categoryRepository).save(mockEntity);
    }


    @Test
    void findById_deberiaRetornarCategoria_cuandoExiste() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(mockEntity));
        when(categoryMapper.toResponseDto(mockEntity)).thenReturn(mockResponseDto);

        CategoryResponseDTO result = categoryService.findById(1L);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
    }

    @Test
    void findAll_deberiaRetornarPaginaDeCategorias_conOrdenamientoValido() {
        PaginationDTO pagination = new PaginationDTO();
        pagination.setPage(0);
        pagination.setSize(10);
        pagination.setSortBy("name");
        pagination.setDirection("desc");

        Page<CategoryEntity> entityPage = new PageImpl<>(List.of(mockEntity));
        when(categoryRepository.search(anyString(), anyBoolean(), any(Pageable.class))).thenReturn(entityPage);
        when(categoryMapper.toResponseDto(mockEntity)).thenReturn(mockResponseDto);

        Page<CategoryResponseDTO> result = categoryService.findAll("Tech", true, pagination);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).name()).isEqualTo("Tecnología");
    }

    @Test
    void findAll_deberiaLanzarBadRequest_cuandoCampoOrdenamientoNoPermitido() {
        PaginationDTO pagination = new PaginationDTO();
        pagination.setSortBy("campoInvalido");

        assertThatThrownBy(() -> categoryService.findAll("Tech", true, pagination))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Campo de ordenamiento no permitido");
    }
}
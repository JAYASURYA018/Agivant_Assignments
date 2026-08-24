package com.bookpulse;

import com.bookpulse.dto.CategoryDto;
import com.bookpulse.entity.Category;
import com.bookpulse.exception.DuplicateResourceException;
import com.bookpulse.exception.ResourceNotFoundException;
import com.bookpulse.repository.CategoryRepository;
import com.bookpulse.service.CategoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    private Category category;

    @BeforeEach
    void setUp() {
        category = new Category();
        category.setId(1L);
        category.setName("Software Engineering");
        category.setDescription("Software construction, principles, and paradigms");
    }

    @Test
    @DisplayName("Should retrieve all categories")
    void testGetAllCategories() {
        when(categoryRepository.findAll()).thenReturn(Arrays.asList(category));

        List<CategoryDto> result = categoryService.getAllCategories();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Software Engineering", result.get(0).getName());
    }

    @Test
    @DisplayName("Should retrieve category by valid ID")
    void testGetCategoryById_Success() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        CategoryDto result = categoryService.getCategoryById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Software Engineering", result.getName());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when category ID is not found")
    void testGetCategoryById_NotFound() {
        when(categoryRepository.findById(404L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> categoryService.getCategoryById(404L));
    }

    @Test
    @DisplayName("Should create a new category when name is unique")
    void testCreateCategory_Success() {
        CategoryDto dto = new CategoryDto();
        dto.setName("DevOps & Cloud");
        dto.setDescription("CI/CD, Kubernetes, and Cloud Architecture");

        Category saved = new Category();
        saved.setId(2L);
        saved.setName("DevOps & Cloud");

        when(categoryRepository.existsByNameIgnoreCase("DevOps & Cloud")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenReturn(saved);

        CategoryDto result = categoryService.createCategory(dto);

        assertNotNull(result);
        assertEquals(2L, result.getId());
        assertEquals("DevOps & Cloud", result.getName());
    }

    @Test
    @DisplayName("Should throw DuplicateResourceException when category name already exists")
    void testCreateCategory_Duplicate() {
        CategoryDto dto = new CategoryDto();
        dto.setName("Software Engineering");

        when(categoryRepository.existsByNameIgnoreCase("Software Engineering")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> categoryService.createCategory(dto));
    }
}

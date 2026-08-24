package com.bookpulse;

import com.bookpulse.dto.RecommendationDto;
import com.bookpulse.dto.SkyRecommendationRequestDto;
import com.bookpulse.dto.SkyRecommendationResponseDto;
import com.bookpulse.entity.Author;
import com.bookpulse.entity.Book;
import com.bookpulse.entity.Category;
import com.bookpulse.repository.BookRepository;
import com.bookpulse.repository.BorrowTransactionRepository;
import com.bookpulse.service.RecommendationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecommendationServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private BorrowTransactionRepository borrowTransactionRepository;

    @InjectMocks
    private RecommendationService recommendationService;

    private Category techCategory;
    private Author authorMartin;
    private Book book1;
    private Book book2;

    @BeforeEach
    void setUp() {
        techCategory = new Category(1L, "Technology & Programming", "Tech");
        authorMartin = new Author(1L, "Robert C. Martin", "Uncle Bob");

        book1 = new Book("Clean Code", "9780132350884", "Software craft", 2008, 5, 2, techCategory);
        book1.setId(1L);
        book1.setAuthors(new HashSet<>(Collections.singletonList(authorMartin)));

        book2 = new Book("Clean Architecture", "9780134494166", "Architecture guide", 2017, 3, 3, techCategory);
        book2.setId(2L);
        book2.setAuthors(new HashSet<>(Collections.singletonList(authorMartin)));
    }

    @Test
    @DisplayName("Should generate recommendations based on same category and author")
    void shouldGenerateBookRecommendations() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book1));
        when(bookRepository.findSimilarCategoryBooks(1L, 1L)).thenReturn(List.of(book2));
        when(bookRepository.findSimilarAuthorBooks(List.of(1L), 1L)).thenReturn(List.of(book2));
        when(borrowTransactionRepository.findFrequentlyCoBorrowedBookIds(1L)).thenReturn(List.of());

        List<RecommendationDto> recs = recommendationService.getRecommendationsForBook(1L);

        assertThat(recs).isNotEmpty();
        assertThat(recs.get(0).getBookId()).isEqualTo(2L);
        assertThat(recs.get(0).getTitle()).isEqualTo("Clean Architecture");
        assertThat(recs.get(0).getMatchScore()).isGreaterThan(5.0);
    }

    @Test
    @DisplayName("Sky AI should recommend relevant books for natural language prompt")
    void shouldGenerateSkyAiRecommendations() {
        when(bookRepository.findAll()).thenReturn(List.of(book1, book2));

        SkyRecommendationRequestDto request = new SkyRecommendationRequestDto("I want to learn about clean code and architecture");
        SkyRecommendationResponseDto response = recommendationService.getSkyAiRecommendations(request);

        assertThat(response).isNotNull();
        assertThat(response.getSkyGreeting()).contains("Sky");
        assertThat(response.getRecommendations()).isNotEmpty();
        assertThat(response.getInspirationalQuote()).contains("Every book holds a little world. Find yours.");
    }
}

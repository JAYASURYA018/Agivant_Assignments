package com.bookpulse.service;

import com.bookpulse.dto.RecommendationDto;
import com.bookpulse.dto.SkyRecommendationRequestDto;
import com.bookpulse.dto.SkyRecommendationResponseDto;
import com.bookpulse.entity.Author;
import com.bookpulse.entity.Book;
import com.bookpulse.exception.ResourceNotFoundException;
import com.bookpulse.repository.BookRepository;
import com.bookpulse.repository.BorrowTransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class RecommendationService {

    private final BookRepository bookRepository;
    private final BorrowTransactionRepository borrowTransactionRepository;

    public RecommendationService(BookRepository bookRepository, BorrowTransactionRepository borrowTransactionRepository) {
        this.bookRepository = bookRepository;
        this.borrowTransactionRepository = borrowTransactionRepository;
    }

    public List<RecommendationDto> getRecommendationsForBook(Long bookId) {
        Book currentBook = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book with ID " + bookId + " was not found"));

        Map<Long, RecommendationScore> scoreMap = new HashMap<>();

        // 1. Same Category Books (Weight: 3.0)
        if (currentBook.getCategory() != null) {
            List<Book> categoryBooks = bookRepository.findSimilarCategoryBooks(currentBook.getCategory().getId(), bookId);
            for (Book b : categoryBooks) {
                RecommendationScore rec = scoreMap.computeIfAbsent(b.getId(), k -> new RecommendationScore(b));
                rec.score += 3.0;
                rec.reasons.add("Same Category (" + currentBook.getCategory().getName() + ")");
            }
        }

        // 2. Same Author Books (Weight: 4.0)
        if (currentBook.getAuthors() != null && !currentBook.getAuthors().isEmpty()) {
            List<Long> authorIds = currentBook.getAuthors().stream().map(Author::getId).collect(Collectors.toList());
            List<Book> authorBooks = bookRepository.findSimilarAuthorBooks(authorIds, bookId);
            for (Book b : authorBooks) {
                RecommendationScore rec = scoreMap.computeIfAbsent(b.getId(), k -> new RecommendationScore(b));
                rec.score += 4.0;
                rec.reasons.add("Same Author");
            }
        }

        // 3. Frequently Co-Borrowed Books (Weight: 5.0)
        List<Object[]> coBorrowed = borrowTransactionRepository.findFrequentlyCoBorrowedBookIds(bookId);
        for (Object[] row : coBorrowed) {
            Long coborrowedBookId = ((Number) row[0]).longValue();
            long count = ((Number) row[1]).longValue();
            bookRepository.findById(coborrowedBookId).ifPresent(b -> {
                RecommendationScore rec = scoreMap.computeIfAbsent(b.getId(), k -> new RecommendationScore(b));
                rec.score += 5.0 + (count * 0.5);
                rec.reasons.add("Frequently Borrowed Together (" + count + " readers)");
            });
        }

        // Sort by score descending and return top 4
        return scoreMap.values().stream()
                .sorted(Comparator.comparingDouble((RecommendationScore r) -> r.score).reversed())
                .limit(4)
                .map(this::mapScoreToDto)
                .collect(Collectors.toList());
    }

    public SkyRecommendationResponseDto getSkyAiRecommendations(SkyRecommendationRequestDto request) {
        String prompt = request.getPrompt().trim().toLowerCase();
        List<Book> allBooks = bookRepository.findAll();

        Map<Long, SkyBookMatch> matchMap = new HashMap<>();

        // Tokenize prompt into significant words
        String[] tokens = prompt.replaceAll("[^a-zA-Z0-9\\s]", " ").split("\\s+");
        Set<String> stopWords = Set.of("the", "a", "an", "and", "or", "in", "on", "of", "to", "for", "with", "is", "are", "i", "am", "interested", "want", "read", "looking", "some", "books", "about", "like", "me", "show");
        List<String> keywords = Arrays.stream(tokens)
                .filter(t -> t.length() > 2 && !stopWords.contains(t))
                .collect(Collectors.toList());

        for (Book book : allBooks) {
            double score = 0.0;
            List<String> matchReasons = new ArrayList<>();

            String titleLower = book.getTitle().toLowerCase();
            String descLower = book.getDescription() != null ? book.getDescription().toLowerCase() : "";
            String catNameLower = book.getCategory() != null ? book.getCategory().getName().toLowerCase() : "";

            // Keyword matching across title, category, description, and author names
            for (String kw : keywords) {
                if (titleLower.contains(kw)) {
                    score += 10.0;
                    matchReasons.add("Title matches '" + kw + "'");
                }
                if (catNameLower.contains(kw)) {
                    score += 8.0;
                    matchReasons.add("Category matches '" + book.getCategory().getName() + "'");
                }
                if (descLower.contains(kw)) {
                    score += 4.0;
                    matchReasons.add("Theme matches '" + kw + "'");
                }
                if (book.getAuthors() != null) {
                    for (Author a : book.getAuthors()) {
                        if (a.getName().toLowerCase().contains(kw)) {
                            score += 9.0;
                            matchReasons.add("By author " + a.getName());
                        }
                    }
                }
            }

            // Semantic topic boosts
            if (prompt.contains("code") || prompt.contains("programming") || prompt.contains("java") || prompt.contains("software") || prompt.contains("developer") || prompt.contains("tech")) {
                if (catNameLower.contains("technology")) {
                    score += 15.0;
                    matchReasons.add("Top software engineering pick");
                }
            }
            if (prompt.contains("fantasy") || prompt.contains("magic") || prompt.contains("sci-fi") || prompt.contains("space") || prompt.contains("future") || prompt.contains("universe")) {
                if (catNameLower.contains("fantasy") || catNameLower.contains("science")) {
                    score += 15.0;
                    matchReasons.add("Rich world-building & speculative fiction");
                }
            }
            if (prompt.contains("habit") || prompt.contains("self") || prompt.contains("mind") || prompt.contains("psychology") || prompt.contains("success") || prompt.contains("focus")) {
                if (catNameLower.contains("self-help")) {
                    score += 15.0;
                    matchReasons.add("Transformative mindset & psychology");
                }
            }
            if (prompt.contains("classic") || prompt.contains("story") || prompt.contains("fiction") || prompt.contains("literature") || prompt.contains("life")) {
                if (catNameLower.contains("fiction")) {
                    score += 12.0;
                    matchReasons.add("Timeless literary classic");
                }
            }
            if (prompt.contains("history") || prompt.contains("human") || prompt.contains("world") || prompt.contains("civilization")) {
                if (catNameLower.contains("history")) {
                    score += 15.0;
                    matchReasons.add("Fascinating historical deep-dive");
                }
            }

            // Availability boost
            if (book.getAvailableCopies() > 0) {
                score += 3.0;
            }

            if (score > 0) {
                matchMap.put(book.getId(), new SkyBookMatch(book, score, matchReasons));
            }
        }

        // If no direct keyword match, recommend top popular books across available collection
        if (matchMap.isEmpty()) {
            for (Book book : allBooks) {
                double score = book.getAvailableCopies() > 0 ? 5.0 : 1.0;
                matchMap.put(book.getId(), new SkyBookMatch(book, score, List.of("Staff Curated Favorite")));
            }
        }

        List<RecommendationDto> recommendations = matchMap.values().stream()
                .sorted(Comparator.comparingDouble((SkyBookMatch m) -> m.score).reversed())
                .limit(5)
                .map(m -> {
                    String reasonText = m.reasons.isEmpty() ? "Sky recommends this bestseller" : String.join(" • ", m.reasons);
                    List<String> authorNames = m.book.getAuthors() != null ?
                            m.book.getAuthors().stream().map(Author::getName).collect(Collectors.toList()) : List.of();
                    String catName = m.book.getCategory() != null ? m.book.getCategory().getName() : "General";
                    return new RecommendationDto(
                            m.book.getId(),
                            m.book.getTitle(),
                            m.book.getIsbn(),
                            m.book.getCoverImageUrl(),
                            catName,
                            authorNames,
                            m.book.getAvailableCopies(),
                            reasonText,
                            Math.round(m.score * 10.0) / 10.0
                    );
                })
                .collect(Collectors.toList());

        String skyGreeting = "Hello! I'm Sky, your AI library companion. Based on your interest in \"" + request.getPrompt() + "\", here are " + recommendations.size() + " handpicked books ready for you:";
        String querySummary = "Matched keywords & themes for: " + request.getPrompt();
        String cuteQuote = "BookBasket — Every book holds a little world. Find yours.";

        return new SkyRecommendationResponseDto(skyGreeting, querySummary, recommendations, cuteQuote);
    }

    private RecommendationDto mapScoreToDto(RecommendationScore score) {
        Book b = score.book;
        List<String> authorNames = b.getAuthors() != null ?
                b.getAuthors().stream().map(Author::getName).collect(Collectors.toList()) : List.of();
        String catName = b.getCategory() != null ? b.getCategory().getName() : "General";
        String reason = String.join(", ", score.reasons);

        return new RecommendationDto(
                b.getId(),
                b.getTitle(),
                b.getIsbn(),
                b.getCoverImageUrl(),
                catName,
                authorNames,
                b.getAvailableCopies(),
                reason,
                Math.round(score.score * 10.0) / 10.0
        );
    }

    private static class RecommendationScore {
        Book book;
        double score;
        List<String> reasons = new ArrayList<>();

        RecommendationScore(Book book) {
            this.book = book;
            this.score = 0.0;
        }
    }

    private static class SkyBookMatch {
        Book book;
        double score;
        List<String> reasons;

        SkyBookMatch(Book book, double score, List<String> reasons) {
            this.book = book;
            this.score = score;
            this.reasons = reasons;
        }
    }
}

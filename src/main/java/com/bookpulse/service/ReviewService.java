package com.bookpulse.service;

import com.bookpulse.dto.ReviewDto;
import com.bookpulse.entity.Book;
import com.bookpulse.entity.Review;
import com.bookpulse.exception.ResourceNotFoundException;
import com.bookpulse.repository.BookRepository;
import com.bookpulse.repository.ReviewRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final BookRepository bookRepository;

    public ReviewService(ReviewRepository reviewRepository, BookRepository bookRepository) {
        this.reviewRepository = reviewRepository;
        this.bookRepository = bookRepository;
    }

    @Transactional(readOnly = true)
    public List<ReviewDto> getReviewsByBookId(Long bookId) {
        if (!bookRepository.existsById(bookId)) {
            throw new ResourceNotFoundException("Book with ID " + bookId + " was not found");
        }
        return reviewRepository.findByBookIdOrderByCreatedAtDesc(bookId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public ReviewDto addReview(Long bookId, ReviewDto dto) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book with ID " + bookId + " was not found"));

        Review review = new Review(book, dto.getReviewerName(), dto.getRating(), dto.getComment());
        Review saved = reviewRepository.save(review);
        return mapToDto(saved);
    }

    private ReviewDto mapToDto(Review r) {
        return new ReviewDto(
                r.getId(),
                r.getBook().getId(),
                r.getReviewerName(),
                r.getRating(),
                r.getComment(),
                r.getCreatedAt()
        );
    }
}

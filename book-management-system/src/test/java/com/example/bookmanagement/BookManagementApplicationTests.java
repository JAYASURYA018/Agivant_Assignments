package com.example.bookmanagement;

import com.example.bookmanagement.model.*;
import com.example.bookmanagement.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class BookManagementApplicationTests {

    @Autowired BookRepository bookRepository;
    @Autowired AuthorRepository authorRepository;
    @Autowired CategoryRepository categoryRepository;

    @Test
    void contextLoads() {
        assertNotNull(bookRepository);
    }

    @Test
    void canCreateAndReadBook() {
        Author author = authorRepository.save(new Author("Test Author"));
        Category category = categoryRepository.save(new Category("Test Category"));

        Book book = new Book();
        book.setTitle("Test Book");
        book.setAuthor(author);
        book.setCategory(category);
        book.setTotalCopies(2);
        book.setAvailableCopies(2);

        Book saved = bookRepository.save(book);

        assertNotNull(saved.getId());
        assertEquals("Test Book", bookRepository.findById(saved.getId()).orElseThrow().getTitle());
    }

    @Test
    void unavailableBookRuleIsSimple() {
        Book book = new Book();
        book.setAvailableCopies(0);
        assertEquals(0, book.getAvailableCopies());
    }
}

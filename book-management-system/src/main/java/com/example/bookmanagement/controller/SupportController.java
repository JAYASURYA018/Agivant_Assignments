package com.example.bookmanagement.controller;

import com.example.bookmanagement.model.*;
import com.example.bookmanagement.repository.*;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class SupportController {
    private final AuthorRepository authors;
    private final CategoryRepository categories;
    private final BorrowerRepository borrowers;

    public SupportController(AuthorRepository authors, CategoryRepository categories, BorrowerRepository borrowers) {
        this.authors = authors;
        this.categories = categories;
        this.borrowers = borrowers;
    }

    @GetMapping("/authors")
    public List<Author> authors() { return authors.findAll(); }

    @GetMapping("/categories")
    public List<Category> categories() { return categories.findAll(); }

    @GetMapping("/borrowers")
    public List<Borrower> borrowers() { return borrowers.findAll(); }
}

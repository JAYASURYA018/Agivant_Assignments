package com.bookpulse.dto;

public class MostBorrowedBookDto {
    private Long bookId;
    private String title;
    private String isbn;
    private String coverImageUrl;
    private long borrowCount;

    public MostBorrowedBookDto() {
    }

    public MostBorrowedBookDto(Long bookId, String title, String isbn, String coverImageUrl, long borrowCount) {
        this.bookId = bookId;
        this.title = title;
        this.isbn = isbn;
        this.coverImageUrl = coverImageUrl;
        this.borrowCount = borrowCount;
    }

    public Long getBookId() {
        return bookId;
    }

    public void setBookId(Long bookId) {
        this.bookId = bookId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getCoverImageUrl() {
        return coverImageUrl;
    }

    public void setCoverImageUrl(String coverImageUrl) {
        this.coverImageUrl = coverImageUrl;
    }

    public long getBorrowCount() {
        return borrowCount;
    }

    public void setBorrowCount(long borrowCount) {
        this.borrowCount = borrowCount;
    }
}

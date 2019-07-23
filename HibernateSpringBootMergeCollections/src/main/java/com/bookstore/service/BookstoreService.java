package com.bookstore.service;

import com.bookstore.repository.BookRepository;
import com.bookstore.repository.AuthorRepository;
import com.bookstore.entity.Author;
import com.bookstore.entity.Book;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BookstoreService {

    private final AuthorRepository authorRepository;
    private final BookRepository bookRepository;

    public BookstoreService(AuthorRepository authorRepository,
            BookRepository bookRepository) {

        this.authorRepository = authorRepository;
        this.bookRepository = bookRepository;
    }

    public List<Book> fetchBooksOfAuthor(String name) {

        return bookRepository.booksOfAuthor(name);
    }

    @Transactional
    public void updateBooksOfAuthor(String name, List<Book> books) {

        Author author = authorRepository.authorAndBooks(name);
        System.out.println("-------------------------------------------------");

        // Remove the existing database rows that are no 
        // longer found in the incoming collection (books)
        author.getBooks().removeIf(b -> !books.contains(b));

        // Update the existing database rows which can be found 
        // in the incoming collection (books)
        List<Book> newBooks = books.stream()
                .filter(b -> !author.getBooks().contains(b))
                .collect(Collectors.toList());             

        books.stream()
                .filter(b -> !newBooks.contains(b))
                .forEach((b) -> {
                    b.setAuthor(author);                       
                    Book mergedBook = bookRepository.save(b);
                    author.getBooks().set(
                            author.getBooks().indexOf(mergedBook),
                            mergedBook);
                });

        // Add the rows found in the incoming collection, 
        // which cannot be found in the current database snapshot
        newBooks.forEach(b -> author.addBook(b));
    }
}
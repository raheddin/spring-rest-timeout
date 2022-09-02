package az.raheddin.springresttimeout.controller;

import az.raheddin.springresttimeout.model.Book;
import az.raheddin.springresttimeout.repository.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class BookController {

    @Autowired
    private  BookRepository bookRepository;

    @GetMapping("/author/transactional")
    @Transactional(timeout = 1)
    public String getWithTransactionTimeout() {
        bookRepository.wasteTime();
        return bookRepository.findById("1")
                .map(Book::getName)
                .orElse("No book found for this title.");
    }

}

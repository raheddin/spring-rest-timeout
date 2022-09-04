package az.raheddin.springresttimeout.controller;

import az.raheddin.springresttimeout.model.Book;
import az.raheddin.springresttimeout.repository.BookRepository;
import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;


@RestController
public class BookController {

    @Autowired
    private  BookRepository bookRepository;

    @Autowired
    private WebClient webClient;

    private TimeLimiter ourTimeLimiter = TimeLimiter.of(TimeLimiterConfig.custom()
            .timeoutDuration(Duration.ofMillis(500)).build());


    @GetMapping("/author/transactional")
    @Transactional(timeout = 1)
    public String getWithTransactionTimeout() {
        bookRepository.wasteTime();
        return bookRepository.findById("1")
                .map(Book::getName)
                .orElse("No book found for this title.");
    }

    @GetMapping("/author/resilience4j")
    public Callable<String> getWithResilience4jTimeLimiter() {
        return TimeLimiter.decorateFutureSupplier(ourTimeLimiter, () ->
                CompletableFuture.supplyAsync(() -> {
                    bookRepository.wasteTime();
                    return bookRepository.findById("1")
                            .map(Book::getName)
                            .orElse("No book found for this title.");
                }));
    }

    @GetMapping("/author/mvc-request-timeout")
    public Callable<String> getWithMvcRequestTimeout() {
        return () -> {
            bookRepository.wasteTime();
            return bookRepository.findById("1")
                    .map(Book::getName)
                    .orElse("No book found for this title.");
        };
    }

    @GetMapping("/author/webclient")
    public String getWithWebClient() {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/author/transactional")
                        .queryParam("title", "1")
                        .build())
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

}

package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.entities.Articles;
import edu.ucsb.cs156.example.entities.UCSBDate;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.ArticlesRepository;
import edu.ucsb.cs156.example.repositories.UCSBDateRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

import com.fasterxml.jackson.core.JsonProcessingException;

import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;

/**
 * This is a REST controller for Articles
 */

@Tag(name = "articles")
@RequestMapping("/api/articles")
@RestController
@Slf4j
public class ArticlesController extends ApiController {

    @Autowired
    ArticlesRepository articlesRepository;

    /**
     * List all Articles
     * 
     * @return an iterable of Articles
     */
    @Operation(summary = "List all Articles")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/all")
    public Iterable<Articles> allArticles() {
        Iterable<Articles> Articles = articlesRepository.findAll();
        return Articles;
    }

    /**
     * Get a new Articles
     * 
     * @param title       the title of the Articles
     * @param url         the url of the Articles
     * @param explanation the explanation of the Articles
     * @param email       the email of the Articles
     * @param dateAdded   the dateAdded of the Articles
     * @return the saved Articles
     */
    @Operation(summary = "Create a new Articles")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/post")
    public Articles postArticles(
            @Parameter(name = "title") @RequestParam String title,
            @Parameter(name = "url") @RequestParam String url,
            @Parameter(name = "explanation") @RequestParam String explanation,
            @Parameter(name = "email") @RequestParam String email,
            @Parameter(name = "dateAdded") @RequestParam("dateAdded") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateAdded)
            throws JsonProcessingException {

        // For an explanation of @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        // See: https://www.baeldung.com/spring-date-parameters

        log.info("dateAdded={}", dateAdded);

        Articles Article = new Articles();
        Article.setTitle(title);
        Article.setUrl(url);
        Article.setExplanation(explanation);
        Article.setEmail(email);
        Article.setDateAdded(dateAdded);

        Articles savedArticles = articlesRepository.save(Article);

        return savedArticles;
    }


    /**
     * Get a single articles by id
     * 
     * @param id the id of the articles
     * @return a articles
     */
    @Operation(summary= "Get a single articles")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("")
    public Articles getById(
            @Parameter(name="id") @RequestParam Long id) {
        Articles articles = articlesRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Articles.class, id));

        return articles;
    }
}

package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.entities.UCSBMenuItemReview;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.UCSBMenuItemReviewRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

import com.fasterxml.jackson.core.JsonProcessingException;

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


@Tag(name = "UCSBMenuItemReview")
@RequestMapping("/api/ucsbmenuitemreview")
@RestController
@Slf4j

public class UCSBMenuItemReviewController extends ApiController {
    @Autowired
    UCSBMenuItemReviewRepository ucsbMenuItemReviewRepository;

    /**
     * List all UCSB menu item reviews
     *
     * @return an iterable of UCSBMenuItemReview
     */
    @Operation(summary= "List all ucsb menu item reviews")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/all")
    public Iterable<UCSBMenuItemReview> allUCSBMenuItemRevies() {
        Iterable<UCSBMenuItemReview> reviews = ucsbMenuItemReviewRepository.findAll();
        return reviews;
    }

    /** 
     * Create a new UCSB menu item review
     * 
     * @param itemId the id of the menu item
     * @param reviewerEmail the email of the reviewer
     * @param stars the number of stars (0 to 5)
    //  * @param dateReviewed the date the review was made
     * @param comments the comments of the review
     * @return the created UCSBMenuItemReview
     */
    @Operation(summary= "Create a new ucsb menu item review")
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping("/post")

    public UCSBMenuItemReview postUCSBMenuItemReview(
            @Parameter(name = "itemId") @RequestParam long itemId,
            @Parameter(name = "reviewerEmail") @RequestParam String reviewerEmail,
            @Parameter(name = "stars") @RequestParam int stars,
            @Parameter(name = "dateReviewed", description = "date (in iso format, e.g. YYYY-mm-ddTHH:MM:SS; see https://en.wikipedia.org/wiki/ISO_8601)") 
            @RequestParam("dateReviewed") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateReviewed,
            @Parameter(name = "comments") @RequestParam String comments)
            throws JsonProcessingException {

        log.info("dateReviewed={}", dateReviewed);

        UCSBMenuItemReview ucsbMenuItemReview = UCSBMenuItemReview.builder()
                .itemId(itemId)
                .reviewerEmail(reviewerEmail)
                .stars(stars)
                .dateReviewed(dateReviewed)
                .comments(comments)
                .build();

        UCSBMenuItemReview savedUcsbMenuItemReview = ucsbMenuItemReviewRepository.save(ucsbMenuItemReview);

        return savedUcsbMenuItemReview;
    }

    /**
     * Get a single UCSBMenuItemReview by id
     * 
     * @param id the id of the review
     * @return a UCSBMenuItemReview
     */
    @Operation(summary = "Get a single menu item review")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("")
    public UCSBMenuItemReview getById(
            @Parameter(name = "id") @RequestParam Long id) {
        UCSBMenuItemReview review = ucsbMenuItemReviewRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(UCSBMenuItemReview.class, id));

        return review;
    }

}
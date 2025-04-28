package edu.ucsb.cs156.example.controllers;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;

import edu.ucsb.cs156.example.entities.RecommendationRequest;
import edu.ucsb.cs156.example.repositories.RecommendationRequestRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

/**
 * REST Controller for RecommendationRequest
 */
@Tag(name = "RecommendationRequest")
@RequestMapping("/api/recommendationrequests")
@RestController
@Slf4j
public class RecommendationRequestController extends ApiController {

  @Autowired
  RecommendationRequestRepository recommendationRequestRepository;

  /**
   * List all recommendation requests
   * 
   * @return a list of all recommendation requests
   */
  @Operation(summary = "List all recommendation requests")
  @PreAuthorize("hasRole('ROLE_USER')")
  @GetMapping("/all")
  public Iterable<RecommendationRequest> allRecommendationRequests() {
    Iterable<RecommendationRequest> recommendationRequests = recommendationRequestRepository.findAll();
    return recommendationRequests;
  }

  /**
   * Create a new recommendation request
   * 
   * @param requesterEmail the email of the requester
   * @param professorEmail the email of the professor
   * @param explanation    the explanation of the recommendation request
   * @param dateRequested  the date requested
   * @return the saved recommendation request
   */
  @Operation(summary = "Create a new Recommendation Request")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @PostMapping("/post")
  public RecommendationRequest postRecommendationRequest(
      @Parameter(name = "requesterEmail") @RequestParam String requesterEmail,
      @Parameter(name = "professorEmail") @RequestParam String professorEmail,
      @Parameter(name = "explanation") @RequestParam String explanation,
      @Parameter(name = "dateRequested", description = "date (in iso format, e.g. YYYY-mm-ddTHH:MM:SS; see https://en.wikipedia.org/wiki/ISO_8601)") @RequestParam("dateRequested") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateRequested)
      throws JsonProcessingException {

    log.info("dateRequested={}", dateRequested);

    RecommendationRequest recommendationRequest = new RecommendationRequest();
    recommendationRequest.setRequesterEmail(requesterEmail);
    recommendationRequest.setProfessorEmail(professorEmail);
    recommendationRequest.setExplanation(explanation);
    recommendationRequest.setDateRequested(dateRequested);

    RecommendationRequest savedRecommendationRequest = recommendationRequestRepository.save(recommendationRequest);

    return savedRecommendationRequest;
  }
}
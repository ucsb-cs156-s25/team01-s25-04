package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.RecommendationRequest;
import edu.ucsb.cs156.example.repositories.RecommendationRequestRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import java.time.LocalDateTime;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;

@WebMvcTest(controllers = RecommendationRequestController.class)
@Import(TestConfig.class)
public class RecommendationRequestControllerTests extends ControllerTestCase {
  @MockBean
  RecommendationRequestRepository recommendationRequestRepository;

  @MockBean
  UserRepository userRepository;

  @Captor
  ArgumentCaptor<RecommendationRequest> recommendationRequestCaptor;

  // Authorization tests for /api/recommendationrequests/all

  @Test
  public void logged_out_users_cannot_get_all() throws Exception {
    mockMvc.perform(get("/api/recommendationrequests/all"))
        .andExpect(status().is(403)); // logged out users can't get all
  }

  @WithMockUser(roles = { "USER" })
  @Test
  public void logged_in_users_can_get_all() throws Exception {
    // Arrange
    ArrayList<RecommendationRequest> expectedRequests = new ArrayList<>();
    expectedRequests.add(new RecommendationRequest());
    when(recommendationRequestRepository.findAll()).thenReturn(expectedRequests);

    // Act & Assert
    mockMvc.perform(get("/api/recommendationrequests/all"))
        .andExpect(status().is(200))
        .andExpect(jsonPath("$[0]").exists());

    // Verify repository was called
    verify(recommendationRequestRepository, times(1)).findAll();
  }

  // Authorization tests for /api/recommendationrequests/post

  @Test
  public void logged_out_users_cannot_post() throws Exception {
    mockMvc.perform(post("/api/recommendationrequests/post"))
        .andExpect(status().is(403));
  }

  @WithMockUser(roles = { "USER" })
  @Test
  public void logged_in_regular_users_cannot_post() throws Exception {
    mockMvc.perform(post("/api/recommendationrequests/post"))
        .andExpect(status().is(403)); // only admins can post
  }

  // Recommendation POST test
  @WithMockUser(roles = { "ADMIN" })
  @Test
  public void admin_can_post() throws Exception {
    // Arrange
    LocalDateTime dateRequested = LocalDateTime.now();
    RecommendationRequest expectedRequest = new RecommendationRequest();
    expectedRequest.setRequesterEmail("requester@example.com");
    expectedRequest.setProfessorEmail("professor@example.com");
    expectedRequest.setExplanation("Test explanation");
    expectedRequest.setDateRequested(dateRequested);

    when(recommendationRequestRepository.save(any(RecommendationRequest.class))).thenReturn(expectedRequest);

    // Act & Assert
    MvcResult response = mockMvc.perform(post("/api/recommendationrequests/post")
        .param("requesterEmail", "requester@example.com")
        .param("professorEmail", "professor@example.com")
        .param("explanation", "Test explanation")
        .param("dateRequested", dateRequested.toString())
        .with(csrf()))
        .andExpect(status().is(200))
        .andReturn();

    // Verify repository was called with correct parameters
    verify(recommendationRequestRepository, times(1)).save(any(RecommendationRequest.class));

    // Verify the response contains the expected data
    String responseString = response.getResponse().getContentAsString();
    assertTrue(responseString.contains("requester@example.com"));
    assertTrue(responseString.contains("professor@example.com"));
    assertTrue(responseString.contains("Test explanation"));
  }

  // Test to verify all setter methods are called with correct values
  @WithMockUser(roles = { "ADMIN" })
  @Test
  public void test_postRecommendationRequest_sets_all_fields() throws Exception {
    // Arrange
    LocalDateTime dateRequested = LocalDateTime.now();
    String requesterEmail = "requester@example.com";
    String professorEmail = "professor@example.com";
    String explanation = "Test explanation";

    RecommendationRequest expectedRequest = new RecommendationRequest();
    expectedRequest.setRequesterEmail(requesterEmail);
    expectedRequest.setProfessorEmail(professorEmail);
    expectedRequest.setExplanation(explanation);
    expectedRequest.setDateRequested(dateRequested);

    when(recommendationRequestRepository.save(any(RecommendationRequest.class))).thenReturn(expectedRequest);

    // Act
    mockMvc.perform(post("/api/recommendationrequests/post")
        .param("requesterEmail", requesterEmail)
        .param("professorEmail", professorEmail)
        .param("explanation", explanation)
        .param("dateRequested", dateRequested.toString())
        .with(csrf()))
        .andExpect(status().is(200));

    // Assert
    verify(recommendationRequestRepository).save(recommendationRequestCaptor.capture());
    RecommendationRequest capturedRequest = recommendationRequestCaptor.getValue();

    // Verify all fields were set correctly
    assertEquals(requesterEmail, capturedRequest.getRequesterEmail());
    assertEquals(professorEmail, capturedRequest.getProfessorEmail());
    assertEquals(explanation, capturedRequest.getExplanation());
    assertEquals(dateRequested, capturedRequest.getDateRequested());
  }

  // Test to verify the returned value is the one from the repository
  @WithMockUser(roles = { "ADMIN" })
  @Test
  public void test_postRecommendationRequest_returns_saved_request() throws Exception {
    // Arrange
    LocalDateTime dateRequested = LocalDateTime.now();
    String requesterEmail = "requester@example.com";
    String professorEmail = "professor@example.com";
    String explanation = "Test explanation";

    RecommendationRequest expectedRequest = new RecommendationRequest();
    expectedRequest.setRequesterEmail(requesterEmail);
    expectedRequest.setProfessorEmail(professorEmail);
    expectedRequest.setExplanation(explanation);
    expectedRequest.setDateRequested(dateRequested);

    when(recommendationRequestRepository.save(any(RecommendationRequest.class))).thenReturn(expectedRequest);

    // Act
    MvcResult response = mockMvc.perform(post("/api/recommendationrequests/post")
        .param("requesterEmail", requesterEmail)
        .param("professorEmail", professorEmail)
        .param("explanation", explanation)
        .param("dateRequested", dateRequested.toString())
        .with(csrf()))
        .andExpect(status().is(200))
        .andReturn();

    // Assert
    String responseString = response.getResponse().getContentAsString();

    // Verify the response contains the expected data from the saved request
    assertTrue(responseString.contains(requesterEmail));
    assertTrue(responseString.contains(professorEmail));
    assertTrue(responseString.contains(explanation));

    // Verify the repository was called exactly once
    verify(recommendationRequestRepository, times(1)).save(any(RecommendationRequest.class));
  }
}

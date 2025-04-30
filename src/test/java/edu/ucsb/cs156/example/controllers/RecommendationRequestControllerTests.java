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

  // Test for empty list returned by allRecommendationRequests
  @WithMockUser(roles = { "USER" })
  @Test
  public void test_allRecommendationRequests_empty_list() throws Exception {
    // Arrange
    ArrayList<RecommendationRequest> emptyRequests = new ArrayList<>();
    when(recommendationRequestRepository.findAll()).thenReturn(emptyRequests);

    // Act & Assert
    mockMvc.perform(get("/api/recommendationrequests/all"))
        .andExpect(status().is(200))
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$").isEmpty());

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

  // Test for getById endpoint
  @Test
  public void logged_out_users_cannot_get_by_id() throws Exception {
    mockMvc.perform(get("/api/recommendationrequests?id=7"))
        .andExpect(status().is(403)); // logged out users can't get by id
  }

  @WithMockUser(roles = { "USER" })
  @Test
  public void logged_in_users_can_get_by_id() throws Exception {
    // Arrange
    RecommendationRequest expectedRequest = new RecommendationRequest();
    expectedRequest.setId(7L);
    expectedRequest.setRequesterEmail("requester@example.com");
    expectedRequest.setProfessorEmail("professor@example.com");
    expectedRequest.setExplanation("Test explanation");
    expectedRequest.setDateRequested(LocalDateTime.now());

    when(recommendationRequestRepository.findById(7L)).thenReturn(Optional.of(expectedRequest));

    // Act & Assert
    mockMvc.perform(get("/api/recommendationrequests?id=7"))
        .andExpect(status().is(200))
        .andExpect(jsonPath("$.id").value(7))
        .andExpect(jsonPath("$.requesterEmail").value("requester@example.com"))
        .andExpect(jsonPath("$.professorEmail").value("professor@example.com"))
        .andExpect(jsonPath("$.explanation").value("Test explanation"));

    // Verify repository was called
    verify(recommendationRequestRepository, times(1)).findById(7L);
  }

  @WithMockUser(roles = { "USER" })
  @Test
  public void test_getById_not_found() throws Exception {
    // Arrange
    when(recommendationRequestRepository.findById(7L)).thenReturn(Optional.empty());

    // Act & Assert
    mockMvc.perform(get("/api/recommendationrequests?id=7"))
        .andExpect(status().is(404));

    // Verify repository was called
    verify(recommendationRequestRepository, times(1)).findById(7L);
  }

  // Test for getById with null return
  @WithMockUser(roles = { "USER" })
  @Test
  public void test_getById_returns_correct_data() throws Exception {
    // Arrange
    RecommendationRequest expectedRequest = new RecommendationRequest();
    expectedRequest.setId(7L);
    expectedRequest.setRequesterEmail("requester@example.com");
    expectedRequest.setProfessorEmail("professor@example.com");
    expectedRequest.setExplanation("Test explanation");
    expectedRequest.setDateRequested(LocalDateTime.now());

    when(recommendationRequestRepository.findById(7L)).thenReturn(Optional.of(expectedRequest));

    // Act & Assert
    mockMvc.perform(get("/api/recommendationrequests?id=7"))
        .andExpect(status().is(200))
        .andExpect(jsonPath("$.id").value(7))
        .andExpect(jsonPath("$.requesterEmail").value("requester@example.com"))
        .andExpect(jsonPath("$.professorEmail").value("professor@example.com"))
        .andExpect(jsonPath("$.explanation").value("Test explanation"));

    // Verify repository was called
    verify(recommendationRequestRepository, times(1)).findById(7L);
  }

  // Tests for PUT

  @WithMockUser(roles = { "ADMIN" })
  @Test
  public void admin_can_update_recommendation_request() throws Exception {
    // Arrange
    LocalDateTime dateRequested = LocalDateTime.now();

    // Create existing recommendation request
    RecommendationRequest existingRequest = new RecommendationRequest();
    existingRequest.setId(7L);
    existingRequest.setRequesterEmail("old@example.com");
    existingRequest.setProfessorEmail("old@example.com");
    existingRequest.setExplanation("Old explanation");
    existingRequest.setDateRequested(LocalDateTime.now().minusDays(1));

    // Create updated recommendation request
    RecommendationRequest updatedRequest = new RecommendationRequest();
    updatedRequest.setRequesterEmail("new@example.com");
    updatedRequest.setProfessorEmail("new@example.com");
    updatedRequest.setExplanation("New explanation");
    updatedRequest.setDateRequested(dateRequested);

    String requestBody = mapper.writeValueAsString(updatedRequest);

    when(recommendationRequestRepository.findById(7L)).thenReturn(Optional.of(existingRequest));
    when(recommendationRequestRepository.save(any(RecommendationRequest.class))).thenReturn(existingRequest);

    // Act
    MvcResult response = mockMvc.perform(
        put("/api/recommendationrequests?id=7")
            .contentType(MediaType.APPLICATION_JSON)
            .characterEncoding("utf-8")
            .content(requestBody)
            .with(csrf()))
        .andExpect(status().isOk())
        .andReturn();

    // Assert
    verify(recommendationRequestRepository, times(1)).findById(7L);
    verify(recommendationRequestRepository, times(1)).save(any(RecommendationRequest.class));

    // Verify the response contains the expected data
    String responseString = response.getResponse().getContentAsString();
    assertTrue(responseString.contains("new@example.com"));
    assertTrue(responseString.contains("New explanation"));
  }

  // Test to verify all setter methods are called during update
  @WithMockUser(roles = { "ADMIN" })
  @Test
  public void test_updateRecommendationRequest_sets_all_fields() throws Exception {
    // Arrange
    LocalDateTime dateRequested = LocalDateTime.now();

    // Create existing recommendation request
    RecommendationRequest existingRequest = new RecommendationRequest();
    existingRequest.setId(7L);
    existingRequest.setRequesterEmail("old@example.com");
    existingRequest.setProfessorEmail("old@example.com");
    existingRequest.setExplanation("Old explanation");
    existingRequest.setDateRequested(LocalDateTime.now().minusDays(1));

    // Create updated recommendation request
    RecommendationRequest updatedRequest = new RecommendationRequest();
    updatedRequest.setRequesterEmail("new@example.com");
    updatedRequest.setProfessorEmail("new@example.com");
    updatedRequest.setExplanation("New explanation");
    updatedRequest.setDateRequested(dateRequested);

    String requestBody = mapper.writeValueAsString(updatedRequest);

    when(recommendationRequestRepository.findById(7L)).thenReturn(Optional.of(existingRequest));
    when(recommendationRequestRepository.save(any(RecommendationRequest.class))).thenReturn(existingRequest);

    // Act
    mockMvc.perform(
        put("/api/recommendationrequests?id=7")
            .contentType(MediaType.APPLICATION_JSON)
            .characterEncoding("utf-8")
            .content(requestBody)
            .with(csrf()))
        .andExpect(status().isOk());

    // Assert
    verify(recommendationRequestRepository).save(recommendationRequestCaptor.capture());
    RecommendationRequest capturedRequest = recommendationRequestCaptor.getValue();

    // Verify all fields were set correctly
    assertEquals("new@example.com", capturedRequest.getRequesterEmail());
    assertEquals("new@example.com", capturedRequest.getProfessorEmail());
    assertEquals("New explanation", capturedRequest.getExplanation());
    assertEquals(dateRequested, capturedRequest.getDateRequested());
  }

  @WithMockUser(roles = { "ADMIN" })
  @Test
  public void admin_cannot_update_recommendation_request_that_does_not_exist() throws Exception {
    // Arrange
    LocalDateTime dateRequested = LocalDateTime.now();

    RecommendationRequest updatedRequest = new RecommendationRequest();
    updatedRequest.setRequesterEmail("new@example.com");
    updatedRequest.setProfessorEmail("new@example.com");
    updatedRequest.setExplanation("New explanation");
    updatedRequest.setDateRequested(dateRequested);

    String requestBody = mapper.writeValueAsString(updatedRequest);

    when(recommendationRequestRepository.findById(7L)).thenReturn(Optional.empty());

    // Act
    MvcResult response = mockMvc.perform(
        put("/api/recommendationrequests?id=7")
            .contentType(MediaType.APPLICATION_JSON)
            .characterEncoding("utf-8")
            .content(requestBody)
            .with(csrf()))
        .andExpect(status().isNotFound())
        .andReturn();

    // Assert
    verify(recommendationRequestRepository, times(1)).findById(7L);
    verify(recommendationRequestRepository, times(0)).save(any(RecommendationRequest.class));

    Map<String, Object> json = responseToJson(response);
    assertEquals("RecommendationRequest with id 7 not found", json.get("message"));
  }

  @Test
  public void logged_out_users_cannot_update() throws Exception {
    mockMvc.perform(put("/api/recommendationrequests?id=7")
        .contentType(MediaType.APPLICATION_JSON)
        .content("{}"))
        .andExpect(status().is(403));
  }

  @WithMockUser(roles = { "USER" })
  @Test
  public void logged_in_regular_users_cannot_update() throws Exception {
    mockMvc.perform(put("/api/recommendationrequests?id=7")
        .contentType(MediaType.APPLICATION_JSON)
        .content("{}"))
        .andExpect(status().is(403)); // only admins can update
  }

  // Tests for DELETE

  @WithMockUser(roles = { "ADMIN" })
  @Test
  public void admin_can_delete_recommendation_request() throws Exception {
    // Arrange
    LocalDateTime dateRequested = LocalDateTime.now();

    RecommendationRequest recommendationRequest = new RecommendationRequest();
    recommendationRequest.setId(15L);
    recommendationRequest.setRequesterEmail("requester@example.com");
    recommendationRequest.setProfessorEmail("professor@example.com");
    recommendationRequest.setExplanation("Test explanation");
    recommendationRequest.setDateRequested(dateRequested);

    when(recommendationRequestRepository.findById(eq(15L))).thenReturn(Optional.of(recommendationRequest));

    // Act
    MvcResult response = mockMvc.perform(
        delete("/api/recommendationrequests?id=15")
            .with(csrf()))
        .andExpect(status().isOk())
        .andReturn();

    // Assert
    verify(recommendationRequestRepository, times(1)).findById(15L);
    verify(recommendationRequestRepository, times(1)).delete(any(RecommendationRequest.class));

    Map<String, Object> json = responseToJson(response);
    assertEquals("RecommendationRequest with id 15 deleted", json.get("message"));
  }

  @WithMockUser(roles = { "ADMIN" })
  @Test
  public void admin_tries_to_delete_non_existent_recommendation_request_and_gets_right_error_message()
      throws Exception {
    // Arrange
    when(recommendationRequestRepository.findById(eq(15L))).thenReturn(Optional.empty());

    // Act
    MvcResult response = mockMvc.perform(
        delete("/api/recommendationrequests?id=15")
            .with(csrf()))
        .andExpect(status().isNotFound())
        .andReturn();

    // Assert
    verify(recommendationRequestRepository, times(1)).findById(15L);
    verify(recommendationRequestRepository, times(0)).delete(any(RecommendationRequest.class));

    Map<String, Object> json = responseToJson(response);
    assertEquals("RecommendationRequest with id 15 not found", json.get("message"));
  }

  @Test
  public void logged_out_users_cannot_delete() throws Exception {
    mockMvc.perform(delete("/api/recommendationrequests?id=15"))
        .andExpect(status().is(403));
  }

  @WithMockUser(roles = { "USER" })
  @Test
  public void logged_in_regular_users_cannot_delete() throws Exception {
    mockMvc.perform(delete("/api/recommendationrequests?id=15")
        .with(csrf()))
        .andExpect(status().is(403)); // only admins can delete
  }
}

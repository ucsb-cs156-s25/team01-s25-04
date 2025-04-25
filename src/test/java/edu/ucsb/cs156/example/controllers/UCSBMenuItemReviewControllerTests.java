package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.UCSBMenuItemReview;
import edu.ucsb.cs156.example.repositories.UCSBMenuItemReviewRepository;

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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebMvcTest(controllers = UCSBMenuItemReviewController.class)
@Import(TestConfig.class)

public class UCSBMenuItemReviewControllerTests extends ControllerTestCase {

    @MockBean
    UCSBMenuItemReviewRepository ucsbMenuItemReviewRepository;

    @MockBean
    UserRepository userRepository;


    // Authorization tests for /api/ucsbdates/admin/all

    @Test
    public void logged_out_users_cannot_get_all_reviews() throws Exception {
        mockMvc.perform(get("/api/ucsbmenuitemreview/all"))
            .andExpect(status().is(403)); // logged out users can't get all
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void logged_in_users_can_get_all_reviews() throws Exception {
        mockMvc.perform(get("/api/ucsbmenuitemreview/all"))
            .andExpect(status().is(200)); // logged in users can get all
    }

    // @Test
    // public void logged_out_users_cannot_get_by_id() throws Exception {
    //         mockMvc.perform(get("/api/ucsbdates?id=7"))
    //                         .andExpect(status().is(403)); // logged out users can't get by id
    // }

    // Authorization tests for /api/ucsbdates/post
    // (Perhaps should also have these for put and delete)


    @Test
    public void logged_out_users_cannot_post_review() throws Exception {
        mockMvc.perform(post("/api/ucsbmenuitemreview/post"))
            .andExpect(status().is(403)); // logged out users can't post
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void logged_in_regular_users_cannot_post_review() throws Exception {
        mockMvc.perform(post("/api/ucsbmenuitemreview/post"))
            .andExpect(status().is(403)); // only admins can post
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void logged_in_user_can_get_all_ucsbmenuitemreviews() throws Exception {
        // arrange
        LocalDateTime dt1 = LocalDateTime.parse("2024-11-01T12:00:00");
        LocalDateTime dt2 = LocalDateTime.parse("2024-12-15T08:30:00");

        UCSBMenuItemReview review1 = UCSBMenuItemReview.builder()
            .itemId(1L)
            .reviewerEmail("test1@ucsb.edu")
            .stars(4)
            .dateReviewed(dt1)
            .comments("Tasty!")
            .build();

        UCSBMenuItemReview review2 = UCSBMenuItemReview.builder()
            .itemId(2L)
            .reviewerEmail("test2@ucsb.edu")
            .stars(5)
            .dateReviewed(dt2)
            .comments("Even better!")
            .build();

        ArrayList<UCSBMenuItemReview> expectedReviews = new ArrayList<>();
        expectedReviews.addAll(Arrays.asList(review1, review2));

        when(ucsbMenuItemReviewRepository.findAll()).thenReturn(expectedReviews);

        // act
        MvcResult response = mockMvc.perform(get("/api/ucsbmenuitemreview/all"))
            .andExpect(status().isOk()).andReturn();

        // assert
        verify(ucsbMenuItemReviewRepository, times(1)).findAll();
        String expectedJson = mapper.writeValueAsString(expectedReviews);
        String responseString = response.getResponse().getContentAsString();
        assertEquals(expectedJson, responseString);
    }

    @WithMockUser(roles = { "ADMIN", "USER" })
    @Test
    public void an_admin_user_can_post_a_new_ucsbmenuitemreview() throws Exception {
        // arrange
        LocalDateTime dt = LocalDateTime.parse("2024-11-01T12:00:00");

        UCSBMenuItemReview review = UCSBMenuItemReview.builder()
            .itemId(1L)
            .reviewerEmail("reviewer@ucsb.edu")
            .stars(5)
            .dateReviewed(dt)
            .comments("Excellent meal!")
            .build();

        when(ucsbMenuItemReviewRepository.save(eq(review))).thenReturn(review);

        // act
        MvcResult response = mockMvc.perform(
            post("/api/ucsbmenuitemreview/post")
                .param("itemId", "1")
                .param("reviewerEmail", "reviewer@ucsb.edu")
                .param("stars", "5")
                .param("dateReviewed", "2024-11-01T12:00:00")
                .param("comments", "Excellent meal!")
                .with(csrf()))
            .andExpect(status().isOk()).andReturn();

        // assert
        verify(ucsbMenuItemReviewRepository, times(1)).save(review);
        String expectedJson = mapper.writeValueAsString(review);
        String responseString = response.getResponse().getContentAsString();
        assertEquals(expectedJson, responseString);
    }


}
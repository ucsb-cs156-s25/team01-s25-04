package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.Articles;
import edu.ucsb.cs156.example.entities.UCSBDate;
import edu.ucsb.cs156.example.repositories.ArticlesRepository;
import edu.ucsb.cs156.example.repositories.UCSBDateRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.ObjectMapper;

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

@WebMvcTest(controllers = ArticlesController.class)
@Import(TestConfig.class)
public class ArticlesControllerTests extends ControllerTestCase {

        @MockBean
        ArticlesRepository articlesRepository;

        @MockBean
        UserRepository userRepository;

        @Test
        public void logged_out_users_cannot_get_all() throws Exception {
                mockMvc.perform(get("/api/articles/all"))
                                .andExpect(status().is(403)); // logged out users can't get all
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_users_can_get_all() throws Exception {
                mockMvc.perform(get("/api/articles/all"))
                                .andExpect(status().is(200)); // logged
        }

        @Test
        public void logged_out_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/articles/post"))
                                .andExpect(status().is(403));
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_regular_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/articles/post"))
                                .andExpect(status().is(403)); // only admins can post
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_user_can_get_all_articles() throws Exception {

                // arrange
                LocalDateTime ldt1 = LocalDateTime.parse("2022-04-20T00:00:00");

                Articles article1 = Articles.builder()
                        .title("Using testing-playground with React Testing Library")
                        .url("https://dev.to/katieraby/using-testing-playground-with-react-testing-library-26j7")
                        .explanation("Helpful when we get to front end development")
                        .email("phtcon@ucsb.edu")
                        .dateAdded(ldt1)
                        .build();

                ArrayList<Articles> expectedArticles = new ArrayList<>();
                expectedArticles.add(article1);

                when(articlesRepository.findAll()).thenReturn(expectedArticles);

                // act
                MvcResult response = mockMvc.perform(get("/api/articles/all"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(articlesRepository, times(1)).findAll();
                String expectedJson = mapper.writeValueAsString(expectedArticles);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void an_admin_user_can_post_a_new_articles() throws Exception {
                // arrange

                LocalDateTime ldt1 = LocalDateTime.parse("2022-04-20T00:00:00");

                Articles article1 = Articles.builder()
                        .title("Using testing-playground with React Testing Library")
                        .url("https://dev.to/katieraby/using-testing-playground-with-react-testing-library-26j7")
                        .explanation("Helpful when we get to front end development")
                        .email("phtcon@ucsb.edu")
                        .dateAdded(ldt1)
                        .build();

                when(articlesRepository.save(eq(article1))).thenReturn(article1);

                // act
                MvcResult response = mockMvc.perform(
                                post("/api/articles/post?title=Using testing-playground with React Testing Library" + "&url=https://dev.to/katieraby/using-testing-playground-with-react-testing-library-26j7" + "&explanation=Helpful when we get to front end development" + "&email=phtcon@ucsb.edu" + "&dateAdded=2022-04-20T00:00:00")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(articlesRepository, times(1)).save(eq(article1));
                String expectedJson = mapper.writeValueAsString(article1);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @Test
        public void logged_out_users_cannot_get_by_id() throws Exception {
                mockMvc.perform(get("/api/articles?id=7"))
                                .andExpect(status().is(403)); // logged out users can't get by id
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_does_exist() throws Exception {
                LocalDateTime ldt1 = LocalDateTime.parse("2022-04-20T00:00:00");

                Articles article1 = Articles.builder()
                        .title("Using testing-playground with React Testing Library")
                        .url("https://dev.to/katieraby/using-testing-playground-with-react-testing-library-26j7")
                        .explanation("Helpful when we get to front end development")
                        .email("phtcon@ucsb.edu")
                        .dateAdded(ldt1)
                        .build();
                // arrange

                when(articlesRepository.findById(eq(7L))).thenReturn(Optional.of(article1));

                // act
                MvcResult response = mockMvc.perform(get("/api/articles?id=7"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(articlesRepository, times(1)).findById(eq(7L));
                String expectedJson = mapper.writeValueAsString(article1);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_does_not_exist() throws Exception {
                
                // arrange

                when(articlesRepository.findById(eq(7L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(get("/api/articles?id=7"))
                                .andExpect(status().isNotFound()).andReturn();

                // assert

                verify(articlesRepository, times(1)).findById(eq(7L));
                Map<String, Object> json = responseToJson(response);
                assertEquals("EntityNotFoundException", json.get("type"));
                assertEquals("Articles with id 7 not found", json.get("message"));
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_can_edit_an_existing_articles() throws Exception {
                // arrange
                LocalDateTime ldt1 = LocalDateTime.parse("2022-04-20T00:00:00");
                LocalDateTime ldt2 = LocalDateTime.parse("2022-04-19T00:00:00");

                Articles article1 = Articles.builder()
                        .title("Using testing-playground with React Testing Library")
                        .url("https://dev.to/katieraby/using-testing-playground-with-react-testing-library-26j7")
                        .explanation("Helpful when we get to front end development")
                        .email("phtcon@ucsb.edu")
                        .dateAdded(ldt1)
                        .build();

                

                Articles editedArticles = Articles.builder()
                        .title("Handy Spring Utility Classes")
                        .url("https://github.com/ucsb-cs156-s25/team01-s25-04/commit/9d931ba32848ddecbd926f8ecb1deb46e17d012e")
                        .explanation("A lot of really useful classes are built into Spring")
                        .email("renbo@ucsb.edu")
                        .dateAdded(ldt2)
                        .build();
               

                String requestBody = mapper.writeValueAsString(editedArticles);


                when(articlesRepository.findById(eq(67L))).thenReturn(Optional.of(article1));

                // act
                MvcResult response = mockMvc.perform(
                                put("/api/articles?id=67")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .characterEncoding("utf-8")
                                                .content(requestBody)
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(articlesRepository, times(1)).findById(67L);
                verify(articlesRepository, times(1)).save(editedArticles); // should be saved with correct user
                String responseString = response.getResponse().getContentAsString();
                assertEquals(requestBody, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_cannot_edit_ucsbdate_that_does_not_exist() throws Exception {
                // arrange

                LocalDateTime ldt1 = LocalDateTime.parse("2022-04-20T00:00:00");
        
                Articles article1 = Articles.builder()
                        .title("Using testing-playground with React Testing Library")
                        .url("https://dev.to/katieraby/using-testing-playground-with-react-testing-library-26j7")
                        .explanation("Helpful when we get to front end development")
                        .email("phtcon@ucsb.edu")
                        .dateAdded(ldt1)
                        .build();

                String requestBody = mapper.writeValueAsString(article1);

                when(articlesRepository.findById(eq(67L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(
                                put("/api/articles?id=67")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .characterEncoding("utf-8")
                                                .content(requestBody)
                                                .with(csrf()))
                                .andExpect(status().isNotFound()).andReturn();

                // assert
                verify(articlesRepository, times(1)).findById(67L);
                Map<String, Object> json = responseToJson(response);
                assertEquals("Articles with id 67 not found", json.get("message"));

        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_can_delete_a_articles() throws Exception {
                // arrange

                LocalDateTime ldt1 = LocalDateTime.parse("2022-04-20T00:00:00");
        
                Articles article1 = Articles.builder()
                        .title("Using testing-playground with React Testing Library")
                        .url("https://dev.to/katieraby/using-testing-playground-with-react-testing-library-26j7")
                        .explanation("Helpful when we get to front end development")
                        .email("phtcon@ucsb.edu")
                        .dateAdded(ldt1)
                        .build();

                when(articlesRepository.findById(eq(15L))).thenReturn(Optional.of(article1));

                // act
                MvcResult response = mockMvc.perform(
                                delete("/api/articles?id=15")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(articlesRepository, times(1)).findById(15L);
                verify(articlesRepository, times(1)).delete(article1); // should be saved with correct user

                Map<String, Object> json = responseToJson(response);
                assertEquals("Articles with id 15 deleted", json.get("message"));
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_tries_to_delete_non_existant_articles_and_gets_right_error_message() throws Exception {
                // arrange

                when(articlesRepository.findById(eq(15L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(
                                delete("/api/articles?id=15")
                                                .with(csrf()))
                                .andExpect(status().isNotFound()).andReturn();

                // assert
                verify(articlesRepository, times(1)).findById(15L);
                Map<String, Object> json = responseToJson(response);
                assertEquals("Articles with id 15 not found", json.get("message"));
        }
}
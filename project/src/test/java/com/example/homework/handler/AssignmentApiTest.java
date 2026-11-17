package com.example.homework.handler;

import com.example.homework.TestTokens;
import com.example.homework.config.Application;
import com.example.homework.persistence.AssignmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
class AssignmentApiTest {
    private static final String URL = "/api/v1/assignments";
    private static final String BODY = "{\"businessKey\":\"HW-API-1\",\"title\":\"Essay\"}";

    @Autowired
    MockMvc mvc;

    @Autowired
    AssignmentRepository assignments;

    @BeforeEach
    void clean() {
        assignments.deleteAll();
    }

    @Test
    void secondPostOfSameKeyIs409AndCountStaysOne() throws Exception {
        mvc.perform(post(URL).with(TestTokens.teacher())
                        .contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isCreated());

        mvc.perform(post(URL).with(TestTokens.teacher())
                        .contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DUPLICATE"));

        assertEquals(1, assignments.countByBusinessKey("HW-API-1"));
    }

    @Test
    void anonymousCreateIs401AndNothingIsStored() throws Exception {
        mvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY))
                .andExpect(status().isUnauthorized());

        assertEquals(0, assignments.countByBusinessKey("HW-API-1"));
    }

    @Test
    void openApiListsTheContractWithoutToken() throws Exception {
        mvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/v1/assignments'].post.responses['201']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/assignments'].post.responses['400']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/assignments'].post.responses['409']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/assignments/{id}'].get.responses['200']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/assignments/{id}'].get.responses['404']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/assignments/{id}/approve'].post.responses['200']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/assignments/{id}/approve'].post.responses['401']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/assignments/{id}/approve'].post.responses['403']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/auth/login'].post.responses['200']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/auth/login'].post.responses['400']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/auth/login'].post.responses['401']").exists())
                .andExpect(jsonPath("$.paths['/api/me'].get.responses['200']").exists())
                .andExpect(jsonPath("$.paths['/api/me'].get.responses['401']").exists());
    }

    @Test
    void swaggerUiOpensWithoutToken() throws Exception {
        mvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk());
    }
}
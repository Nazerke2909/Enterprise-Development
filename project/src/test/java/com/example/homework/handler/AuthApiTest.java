package com.example.homework.handler;

import com.example.homework.config.AssignmentService;
import com.example.homework.domain.AssignmentStatus;
import com.example.homework.persistence.AssignmentEntity;
import com.example.homework.persistence.AssignmentRepository;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthApiTest {
    private static final String LOGIN = "/api/v1/auth/login";
    private static final String URL = "/api/v1/assignments";
    private static final String BUSINESS_KEY = "HW-AUTH-1";

    @Autowired
    MockMvc mvc;

    @Autowired
    AssignmentRepository assignments;

    @Autowired
    AssignmentService service;

    @BeforeEach
    void clean() {
        assignments.deleteAll();
    }

    private String login(String user, String password) throws Exception {
        String json = mvc.perform(post(LOGIN).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + user + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(json, "$.accessToken");
    }

    @Test
    void loginReturnsBearerTokenFor900Seconds() throws Exception {
        mvc.perform(post(LOGIN).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"teacher\",\"password\":\"teacher123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(900));
    }

    @Test
    void unknownUserAndWrongPasswordGiveTheSame401() throws Exception {
        mvc.perform(post(LOGIN).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"nobody\",\"password\":\"x\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHENTICATED"))
                .andExpect(jsonPath("$.message").value("bad username or password"));

        mvc.perform(post(LOGIN).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"teacher\",\"password\":\"wrong\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHENTICATED"))
                .andExpect(jsonPath("$.message").value("bad username or password"));
    }

    @Test
    void blankPasswordIs400() throws Exception {
        mvc.perform(post(LOGIN).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"teacher\",\"password\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION"))
                .andExpect(jsonPath("$.message").value("password must not be blank"));
    }

    @Test
    void garbageTokenIs401() throws Exception {
        mvc.perform(get("/api/me").header("Authorization", "Bearer not.a.jwt"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("valid token required"));
    }

    @Test
    void meShowsUsernameFromRealToken() throws Exception {
        String token = login("student", "student123");
        mvc.perform(get("/api/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("student"))
                .andExpect(jsonPath("$.authorities[0]").value("ROLE_STUDENT"));
    }

    @Test
    void juniorGets403RowStaysSeniorGets409ThenSuccess() throws Exception {
        String teacher = login("teacher", "teacher123");
        String student = login("student", "student123");
        String created = mvc.perform(post(URL).header("Authorization", "Bearer " + student)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"businessKey\":\"" + BUSINESS_KEY + "\",\"title\":\"Essay\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String id = JsonPath.read(created, "$.id");

        mvc.perform(post(URL + "/" + id + "/approve").header("Authorization", "Bearer " + student))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"))
                .andExpect(jsonPath("$.message").value("not allowed for your role"));
        assertEquals(AssignmentStatus.ASSIGNED,
                assignments.findByBusinessKey(BUSINESS_KEY).orElseThrow().getStatus());

        mvc.perform(post(URL + "/" + id + "/approve").header("Authorization", "Bearer " + teacher))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("FORBIDDEN_TRANSITION"));
        assertEquals(AssignmentStatus.ASSIGNED,
                assignments.findByBusinessKey(BUSINESS_KEY).orElseThrow().getStatus());

        AssignmentEntity row = assignments.findByBusinessKey(BUSINESS_KEY).orElseThrow();
        row.changeStatus(AssignmentStatus.CHECKED);
        assignments.save(row);
        mvc.perform(post(URL + "/" + id + "/approve").header("Authorization", "Bearer " + teacher))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    void methodSecurityBlocksJuniorEvenWithoutTheUrlRule() {
        assertThrows(AccessDeniedException.class, () -> service.approve(UUID.randomUUID(), "student"));
    }
}

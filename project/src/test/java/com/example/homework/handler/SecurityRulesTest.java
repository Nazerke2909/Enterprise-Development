package com.example.homework.handler;

import com.example.homework.TestTokens;
import com.example.homework.config.AssignmentService;
import com.example.homework.config.SecurityConfig;
import com.example.homework.domain.AssignmentStatus;
import com.example.homework.persistence.AssignmentRecord;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({AssignmentController.class, MeController.class})
@ContextConfiguration(classes = {AssignmentController.class, MeController.class,
        ApiExceptionHandler.class, SecurityConfig.class})
@TestPropertySource(properties = {
        "app.jwt.secret=" + TestTokens.SECRET,
        "app.jwt.issuer=homework-control"})
class SecurityRulesTest {
    private static final String URL = "/api/v1/assignments";

    @Autowired
    MockMvc mvc;

    @MockitoBean
    AssignmentService service;

    @MockitoBean
    JwtDecoder jwtDecoder;

    @Test
    void noTokenIs401AndServiceIsNotCalled() throws Exception {
        mvc.perform(get(URL + "/" + UUID.randomUUID()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHENTICATED"))
                .andExpect(jsonPath("$.message").value("valid token required"))
                .andExpect(jsonPath("$.correlationId").isNotEmpty());
        verifyNoInteractions(service);
    }

    @Test
    void anonymousCreateIs401NotCreated() throws Exception {
        mvc.perform(post(URL).contentType("application/json")
                        .content("{\"businessKey\":\"HW-1\",\"title\":\"Essay\"}"))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(service);
    }

    @Test
    void correlationIdIsEchoedOn401() throws Exception {
        mvc.perform(get(URL + "/" + UUID.randomUUID()).header("X-Correlation-Id", "abc-123"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string("X-Correlation-Id", "abc-123"))
                .andExpect(jsonPath("$.correlationId").value("abc-123"));
    }

    @Test
    void noTokenOnLockedVerbIs401() throws Exception {
        mvc.perform(post(URL + "/" + UUID.randomUUID() + "/approve"))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(service);
    }

    @Test
    void juniorOnLockedVerbIs403AndServiceIsNotCalled() throws Exception {
        mvc.perform(post(URL + "/" + UUID.randomUUID() + "/approve").with(TestTokens.student()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"))
                .andExpect(jsonPath("$.message").value("not allowed for your role"))
                .andExpect(jsonPath("$.correlationId").isNotEmpty());
        verifyNoInteractions(service);
    }

    @Test
    void seniorOnLockedVerbSucceedsAndServiceIsCalledWithTokenName() throws Exception {
        UUID id = UUID.randomUUID();
        when(service.approve(id, "tom"))
                .thenReturn(new AssignmentRecord(id, "HW-1", AssignmentStatus.APPROVED, "Essay"));

        mvc.perform(post(URL + "/" + id + "/approve").with(TestTokens.teacher())
                        .contentType("application/json")
                        .content("{\"actor\":\"hacker\",\"role\":\"TEACHER\"}"))   // ignored
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
        verify(service).approve(id, "tom");
    }

    @Test
    void meReturnsNameAndAuthoritiesFromToken() throws Exception {
        mvc.perform(get("/api/me").with(TestTokens.student()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("sam"))
                .andExpect(jsonPath("$.authorities[0]").value("ROLE_STUDENT"));
    }

    @Test
    void meWithoutTokenIs401() throws Exception {
        mvc.perform(get("/api/me")).andExpect(status().isUnauthorized());
    }

    @Test
    void anythingElseIsDeniedEvenWithToken() throws Exception {
        mvc.perform(get("/something-else").with(TestTokens.teacher()))
                .andExpect(status().isForbidden());
    }
}
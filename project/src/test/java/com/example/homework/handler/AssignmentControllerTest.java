package com.example.homework.handler;

import com.example.homework.config.AssignmentService;
import com.example.homework.domain.AssignmentId;
import com.example.homework.domain.AssignmentNotFound;
import com.example.homework.domain.AssignmentStatus;
import com.example.homework.domain.DuplicateAssignment;
import com.example.homework.persistence.AssignmentRecord;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AssignmentController.class)
@ContextConfiguration(classes = {AssignmentController.class, ApiExceptionHandler.class})
class AssignmentControllerTest {
    private static final String URL = "/api/v1/assignments";

    @Autowired
    MockMvc mvc;

    @MockitoBean
    AssignmentService service;

    private static String body(String key, String title) {
        return "{\"businessKey\":\"" + key + "\",\"title\":\"" + title + "\"}";
    }

    @Test
    void validBodyReturns201WithLocation() throws Exception {
        UUID id = UUID.randomUUID();
        when(service.register(any(AssignmentId.class), eq("HW-1"), eq("Essay")))
                .thenReturn(new AssignmentRecord(id, "HW-1", AssignmentStatus.ASSIGNED, "Essay"));

        mvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(body("HW-1", "Essay")))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith(URL + "/" + id)))
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.businessKey").value("HW-1"))
                .andExpect(jsonPath("$.status").value("ASSIGNED"))
                .andExpect(jsonPath("$.title").value("Essay"));
    }

    @Test
    void title120CharsIsAccepted() throws Exception {
        String title = "x".repeat(120);
        when(service.register(any(AssignmentId.class), eq("HW-2"), eq(title)))
                .thenReturn(new AssignmentRecord(UUID.randomUUID(), "HW-2", AssignmentStatus.ASSIGNED, title));

        mvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(body("HW-2", title)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value(title));
    }

    @Test
    void blankTitleReturns400AndServiceIsNotCalled() throws Exception {
        mvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(body("HW-1", "")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION"))
                .andExpect(jsonPath("$.message").value("title must not be blank"));
        verifyNoInteractions(service);
    }

    @Test
    void spacesOnlyTitleReturns400() throws Exception {
        mvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(body("HW-1", "   ")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("title must not be blank"));
        verifyNoInteractions(service);
    }

    @Test
    void blankBusinessKeyReturns400() throws Exception {
        mvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(body("", "Essay")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("business key must not be blank"));
        verifyNoInteractions(service);
    }

    @Test
    void title121CharsReturns400() throws Exception {
        mvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(body("HW-1", "x".repeat(121))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION"))
                .andExpect(jsonPath("$.message").value(containsString("size must be between 0 and 120")));
        verifyNoInteractions(service);
    }

    @Test
    void notJsonBodyReturns400() throws Exception {
        mvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content("not json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION"))
                .andExpect(jsonPath("$.message").value("body must be JSON"));
    }

    @Test
    void wrongContentTypeReturns415() throws Exception {
        mvc.perform(post(URL).contentType(MediaType.TEXT_PLAIN).content("x"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.code").value("UNSUPPORTED"));
    }

    @Test
    void missingItemReturns404() throws Exception {
        UUID id = UUID.randomUUID();
        when(service.findById(id)).thenThrow(new AssignmentNotFound(id.toString()));

        mvc.perform(get(URL + "/" + id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    @Test
    void duplicateReturns409() throws Exception {
        when(service.register(any(AssignmentId.class), eq("HW-1"), eq("Essay")))
                .thenThrow(new DuplicateAssignment("HW-1"));

        mvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(body("HW-1", "Essay")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DUPLICATE"));
    }

    @Test
    void businessKeyInPathReturns400() throws Exception {
        mvc.perform(get(URL + "/HW-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION"))
                .andExpect(jsonPath("$.message").value("id must be a UUID"));
        verifyNoInteractions(service);
    }

    @Test
    void correlationHeaderIsEchoedOnError() throws Exception {
        mvc.perform(post(URL).header("X-Correlation-Id", "abc-123")
                        .contentType(MediaType.APPLICATION_JSON).content(body("HW-1", "")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.correlationId").value("abc-123"));
    }

    @Test
    void missingCorrelationHeaderIsMinted() throws Exception {
        mvc.perform(get(URL + "/HW-1"))
                .andExpect(jsonPath("$.correlationId").isNotEmpty());
    }
}

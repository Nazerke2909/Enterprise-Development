package com.example.homework.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static
org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static
org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static
org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ActuatorTest {
    @Autowired
    MockMvc mvc;

    @Test
    void healthIsUp() throws Exception {
        mvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$. status") .value("UP"))
                .andExpect(jsonPath("$.components").doesNotExist());
    }

    @Test
    void infoCarriesTheProductName() throws Exception {
        mvc.perform(get("/actuator/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.app.name").value("Homework Control"));
    }
    
    @Test 
    void dangeriusEndpointsAreNotExposed() throws Exception {
        for (String name : new String[]{"env", "beans", "headmap", "shutdown"}) {
            mvc.perform(get("/actuator/" + name)).andExpect(status().isNotFound());
        }
    }
}
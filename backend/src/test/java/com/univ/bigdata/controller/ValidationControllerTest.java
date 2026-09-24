package com.univ.bigdata.controller;

import com.univ.bigdata.service.RecommendService;
import com.univ.bigdata.service.UniversityService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ValidationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RecommendService recommendService;

    @MockBean
    private UniversityService universityService;

    @Test
    void negativeRecommendationScoreShouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/api/recommend/match")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"province\":\"湖南\",\"score\":-1,\"subjectType\":\"物理类\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void oversizedUniversityPageShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/api/university/page?page=0&size=101"))
                .andExpect(status().isBadRequest());
    }
}

package edu.numerical;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest @AutoConfigureMockMvc
class ApiIntegrationTest {
    @Autowired MockMvc api;
    @Test void healthAndRegistry() throws Exception {api.perform(get("/api/health")).andExpect(status().isOk());api.perform(get("/api/differentiation/methods")).andExpect(jsonPath("$.length()").value(18));}
    @Test void realCalculation() throws Exception {api.perform(post("/api/differentiation/calculate").contentType("application/json").content(body("0.1","CENTRAL"))).andExpect(status().isOk()).andExpect(jsonPath("$.approximation").value(org.hamcrest.Matchers.closeTo(14.01,1e-10))).andExpect(jsonPath("$.steps.length()").value(9));}
    @Test void comparison() throws Exception {api.perform(post("/api/differentiation/compare").contentType("application/json").content(body("0.1","CENTRAL"))).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(6));}
    @Test void badRequests() throws Exception {for(String json:new String[]{body("0","CENTRAL"),body("0.1","UNKNOWN"),"{}","{bad}"})api.perform(post("/api/differentiation/calculate").contentType("application/json").content(json)).andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").exists());}
    private String body(String h,String method){return "{\"expression\":\"x^3+2*x\",\"x\":2,\"h\":"+h+",\"derivativeOrder\":1,\"method\":\""+method+"\",\"accuracy\":\"O_H2\"}";}
}

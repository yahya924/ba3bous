package com.igatn.extranet.rest.policy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.hamcrest.Matchers.hasSize;

/**
 * FRE - this test, will require local json-server deployed
 * with 8 elements inside!
 * 
 * TODO - FRE - mock 3rd-party API and define data from here!
 */
@SpringBootTest
@AutoConfigureMockMvc
@EnableAutoConfiguration(exclude = {SecurityAutoConfiguration.class})
@ExtendWith(SpringExtension.class)
public class PolicyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Call '/policies/getAll' endpoints with GET!")
    @WithUserDetails("m1@m1.m1")
    public void testRetrievePolicies() throws Exception {

        int expectedSize = 2;

        mockMvc.perform(get("/policies/getAll"))
                .andDo(print())
                .andExpect(jsonPath("$.totalResults").value(expectedSize))
                .andExpect(jsonPath("$.policies", hasSize(expectedSize)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Call '/policies/getDetails' endpoints with GET!")
    @WithUserDetails("m1@m1.m1")
    public void testRetrievePolicyDetails() throws Exception {

        String  policyId = "62d2e4d7b2fb646f63e21d8f";
        int benefExpectedSize = 2;
        int bankAccountExpectedSize = 1;
        int guaranteesExpectedSize = 5;
        int documentsExpectedSize = 2;

        mockMvc.perform(get("/policies/getDetails?id="+policyId))
                .andDo(print())
                .andExpect(jsonPath("$.beneficiaries", hasSize(benefExpectedSize)))
                .andExpect(jsonPath("$.bankAccounts", hasSize(bankAccountExpectedSize)))
                .andExpect(jsonPath("$.guarantees", hasSize(guaranteesExpectedSize)))
                .andExpect(jsonPath("$.documents", hasSize(documentsExpectedSize)))
                .andExpect(status().isOk());
    }
}
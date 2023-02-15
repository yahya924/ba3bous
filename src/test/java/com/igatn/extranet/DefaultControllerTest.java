package com.igatn.extranet;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.hamcrest.Matchers.containsString;

/**
 * FRE - a test class to check a controller task.
 * 
 * if you don't know testing in Spring of tests, pls visit: 
 * https://www.baeldung.com/integration-testing-in-spring
 */
@WebMvcTest(DefaultController.class)   // <1>
public class DefaultControllerTest {

    // this object role is to create a mock mvc ops
    private MockMvc mockMvc;   // <2>

    // inject app context
    @Autowired
    private WebApplicationContext webApplicationContext;
    
    // get mockMvc instance from app context
    @BeforeEach
    public void setup() throws Exception {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext).build();
    }

    /**
     * Test case
     * 
     * @throws Exception
     */
    @Test
    public void testDefaultHtmlPage() throws Exception {
        mockMvc.perform(get("/"))    // <3>
                .andExpect(status().isOk())  // <4>
                .andExpect(view().name("default"))  // <5>
                .andExpect(content().string(           // <6>
                        containsString("Welcome to Extranet IGATN")));
    }

}

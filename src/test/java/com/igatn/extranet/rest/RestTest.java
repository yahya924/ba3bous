//package com.igatn.extranet.rest;
//
//import com.igatn.extranet.data.RoleRepository;
//import com.igatn.extranet.data.UserRepository;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
//import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
//import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.http.*;
//import org.springframework.test.web.servlet.setup.MockMvcBuilders;
//import org.springframework.web.context.WebApplicationContext;
//
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
//import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
//import static org.hamcrest.Matchers.greaterThanOrEqualTo;
//import static org.hamcrest.CoreMatchers.is;
//
//@SpringBootTest
//@AutoConfigureMockMvc
//@EnableAutoConfiguration(exclude = SecurityAutoConfiguration.class )
//@AutoConfigureTestDatabase
//public class RestTest {
//
//    private MockMvc mockMvc;
//
//    // inject app context
//    @Autowired
//    private WebApplicationContext webApplicationContext;
//
//    // get mockMvc instance from app context
//    @BeforeEach
//    public void setup() throws Exception {
//        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext).build();
//    }
//
//    @Autowired
//    private PasswordEncoder encoder;
//
//    @Autowired
//    private UserRepository userRepository;
//
//    @Autowired
//    private RoleRepository roleRepository;
//
//    @Test
//    public void testAllUsers() throws Exception {
//
//        // test user
//        mockMvc.perform(get("/api/user?all").contentType(MediaType.APPLICATION_JSON))
//                .andDo(print())
//                .andExpect(status().isOk())
//                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
//                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
//                .andExpect(jsonPath("$[0].username", is("johnDoe@test.com")));
//    }
//
//}

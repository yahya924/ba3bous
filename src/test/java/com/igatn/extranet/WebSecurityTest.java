package com.igatn.extranet;

import org.json.JSONObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * FRE - authentication test full
 */
@SpringBootTest
@AutoConfigureMockMvc
public class WebSecurityTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    @DisplayName("Endpoints cannot be called by unauthenticated users")
    public void testFailedAuthentication() throws Exception {
        
        mockMvc.perform(get("/"))
                .andExpect(unauthenticated());

        mockMvc.perform(post("/a"))
                .andExpect(unauthenticated());

        mockMvc.perform(post("/a/b"))
                .andExpect(unauthenticated());
    }

    @Test
    @DisplayName("Any authenticated user can call '/' endpoints with GET ")
    @WithUserDetails("johnDoe@test.com")
    public void testAnyAuthenticatedUserCanCallGET() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk());
    }
    
    @Test
    @DisplayName("Any authenticated user cannot call '/' endpoints with POST!")
    @WithUserDetails("johnDoe@test.com")
    public void testAnyAuthenticatedUserCanCallPOST() throws Exception {
        
        mockMvc.perform(
                post("/"))
                .andExpect(status().isForbidden());
    }

    // FRE - testing api call after authentication
    @Test
    @DisplayName("Test login allowed user ")
    @WithUserDetails("m1@m1.m1")
    public void testAllowedUserToLogin() throws Exception {

        HttpHeaders httpHeaders = new HttpHeaders();
        // use some online tool to encode to base64 to verify
        httpHeaders.add("Authorization", "Basic bTFAbTEubTE6SGVsbG8xMjMh");

        // test user
        String response = mockMvc.perform(
                post("/users/signIn")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\": \"m1@m1.m1\", \"password\": \"Hello123!\" }")
                        .headers(httpHeaders))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JSONObject expected = new JSONObject();
        expected.put("token", "bTFAbTEubTE6SGVsbG8xMjMh");
        expected.put("message", "Hello m1@m1.m1!");

        JSONAssert.assertEquals(
                expected.toString(),
                response,
                true
        );
    }

    // FRE - TODO
//    @Test
//    @DisplayName("Test login NOT allowed user ")
//    @WithUserDetails("johnDoe@test.com")
//    public void testNotAllowedUserToLogin() throws Exception {
//
//        HttpHeaders httpHeaders = new HttpHeaders();
//        // use some online tool to encode to base64 to verify
//        httpHeaders.add("Authorization", "Basic am9obkRvZUB0ZXN0LmNvbTpBemVydHkxMjMh");
//        
//        // test user
//        String response = mockMvc.perform(
//                post("/api/authentication/login")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content("{\"username\": \"johnDoe@test.com\", \"password\": \"Azerty123!\" }")
//                        .headers(httpHeaders))
//                .andDo(print())
//                .andExpect(status().isForbidden())
////                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
//                .andReturn()
//                .getResponse()
//                .getContentAsString();
//               
//
//        JSONObject expected = new JSONObject();
//        expected.put("token", "bTFAbTEubTE6SGVsbG8xMjMh");
//        expected.put("message", "Hello johnDoe@test.com!");
//
////        JSONAssert.assertNotEquals(
////                expected,
////                response,
////                true
////        );
//    }


//    @Test
//    @DisplayName("Endpoints /a and /a/b are denied")
////    @WithUserDetails("johnDoe@test.com")
//    public void testOtherEndpointsAreUnauthorized() throws Exception {
//        mockMvc.perform(post("/a"))
//                .andExpect(status().isUnauthorized());
//
//        mockMvc.perform(post("/a/b"))
//                .andExpect(status().isUnauthorized());
//    }
}

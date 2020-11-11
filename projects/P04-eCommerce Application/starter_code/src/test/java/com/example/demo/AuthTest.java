package com.example.demo;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Ignore;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class AuthTest {

  @Autowired
  private MockMvc mvc;

  @Test
  public void shouldNotAllowAccessToUnauthenticatedUsers() throws Exception {
    mvc.perform(MockMvcRequestBuilders.get("/test")).andExpect(status().isForbidden());
  }

  @Test
  @Ignore
  public void shouldAllowAccessToUnauthenticatedToCreateUser() throws Exception {
    String createUser = "{\n"
        + "    \"username\": \"testtest\",\n"
        + "    \"password\": \"testtest\",\n"
        + "    \"confirmPassword\": \"testtest\"\n"
        + "}";

    mvc.perform(MockMvcRequestBuilders.post("/api/user/create").content(createUser))
        .andExpect(status().isOk());
  }

}

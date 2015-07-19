package org.emo.simple.um;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;

import org.emo.simple.um.dao.UserRepository;
import org.emo.simple.um.entity.User;
import org.emo.simple.um.rest.UserController;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = { TestContext.class, Application.class })
@WebAppConfiguration
public class UserControllerTests {

  @InjectMocks
  UserController userController;

  @Mock
  private UserRepository repositoryMock;

  private MockMvc mockMvc;

  @Before
  public void init() {
    MockitoAnnotations.initMocks(this);

    this.mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
  }

  @Test
  public void listAllUsersTest() throws Exception {
    Mockito.when(repositoryMock.findAll()).thenReturn(Arrays.asList(
        new User("bugs.bunny@acme.org", "Bugs", "Bunny"),
        new User("daffy.duck@acme.org", "Daffy", "Duck"),
        new User("elmer.fudd@acme.org", "Elmer", "Fudd")));

    mockMvc.perform(get("/users"))
        .andExpect(status().isOk())
        .andExpect(content().contentType("application/json;charset=UTF-8"))
        .andExpect(jsonPath("$", hasSize(3)))
        .andExpect(jsonPath("$[0].email", is("bugs.bunny@acme.org")))
        .andExpect(jsonPath("$[0].firstName", is("Bugs")))
        .andExpect(jsonPath("$[0].lastName", is("Bunny")))
        .andExpect(jsonPath("$[1].email", is("daffy.duck@acme.org")))
        .andExpect(jsonPath("$[1].firstName", is("Daffy")))
        .andExpect(jsonPath("$[1].lastName", is("Duck")))
        .andExpect(jsonPath("$[2].email", is("elmer.fudd@acme.org")))
        .andExpect(jsonPath("$[2].firstName", is("Elmer")))
        .andExpect(jsonPath("$[2].lastName", is("Fudd")));

  }

  @Test
  public void registerNewUser() throws Exception {
    User user = new User("daffy.duck@acme.org", "Daffy", "Duck");

    mockMvc.perform(post("/users")
        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        .param("email", user.getEmail())
        .param("firstName", user.getFirstName())
        .param("firstName", user.getLastName()))

    .andExpect(status().isCreated());

    Mockito.verify(repositoryMock, Mockito.times(1)).save(user);
  }

  @Test
  public void deleteUserByEmail() throws Exception {
    Mockito.when(repositoryMock.findAll()).thenReturn(Arrays.asList(
        new User("bugs.bunny@acme.org", "Bugs", "Bunny"),
        new User("elmer.fudd@acme.org", "Elmer", "Fudd")));

    mockMvc.perform(delete("/users/daffy.duck@acme.org/"))
        .andExpect(status().isOk());

    User deletedUser = new User("daffy.duck@acme.org", null, null);
    Mockito.verify(repositoryMock, Mockito.times(1)).delete(deletedUser);
  }

  @Test
  public void deleteUserWithouEmail() throws Exception {
    mockMvc.perform(delete("/users/xy/"))
        .andExpect(status().isBadRequest());

    Mockito.verifyZeroInteractions(repositoryMock);
  }
}

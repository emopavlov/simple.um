package org.emo.simple.um;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

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

import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = { TestContext.class, Application.class })
@WebAppConfiguration
public class UserControllerTests {
  
  private Date testDate;

  @InjectMocks
  UserController userController;

  @Mock
  private UserRepository mockRepository;

  private MockMvc mockMvc;

  @Before
  public void init() throws ParseException {
    DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
    testDate = format.parse("1936-03-05");
    
    MockitoAnnotations.initMocks(this);

    this.mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
  }

  @Test
  public void listAllUsersTest() throws Exception {
    Mockito.when(mockRepository.findAll()).thenReturn(Arrays.asList(
        new User("bugs.bunny@acme.org", "Bugs", "Bunny", testDate),
        new User("daffy.duck@acme.org", "Daffy", "Duck", testDate),
        new User("elmer.fudd@acme.org", "Elmer", "Fudd", testDate)));

    mockMvc.perform(get("/users"))
        .andExpect(status().isOk())
        .andExpect(content().contentType("application/json;charset=UTF-8"))
        .andExpect(jsonPath("$", hasSize(3)))
        .andExpect(jsonPath("$[0].email", is("bugs.bunny@acme.org")))
        .andExpect(jsonPath("$[0].firstName", is("Bugs")))
        .andExpect(jsonPath("$[0].lastName", is("Bunny")))
//        .andExpect(jsonPath("$[0].birthdate", is("1936-03-05")))
        .andExpect(jsonPath("$[1].email", is("daffy.duck@acme.org")))
        .andExpect(jsonPath("$[1].firstName", is("Daffy")))
        .andExpect(jsonPath("$[1].lastName", is("Duck")))
        .andExpect(jsonPath("$[2].email", is("elmer.fudd@acme.org")))
        .andExpect(jsonPath("$[2].firstName", is("Elmer")))
        .andExpect(jsonPath("$[2].lastName", is("Fudd")));
  }

  @Test
  public void registerNewUser() throws Exception {
    User user = new User("daffy.duck@acme.org", "Daffy", "Duck", testDate);
    ObjectMapper mapper = new ObjectMapper();
    String userJson = mapper.writeValueAsString(user);

    mockMvc.perform(post("/users")
        .contentType(MediaType.APPLICATION_JSON)
        .content(userJson))

    .andExpect(status().isCreated());

    Mockito.verify(mockRepository, Mockito.times(1)).save(user);
  }
  
  @Test
  public void registerExistingUser() throws Exception {
    User user = new User("daffy.duck@acme.org", "Daffy", "Duck", testDate);
    ObjectMapper mapper = new ObjectMapper();
    String userJson = mapper.writeValueAsString(user);
    
    Mockito.when(mockRepository.exists(user.getEmail())).thenReturn(true);

    mockMvc.perform(post("/users")
        .contentType(MediaType.APPLICATION_JSON)
        .content(userJson))

    .andExpect(status().isConflict());

    Mockito.verify(mockRepository, Mockito.times(0)).save(user);
  }

  @Test
  public void deleteUserByEmail() throws Exception {
    Mockito.when(mockRepository.findAll()).thenReturn(Arrays.asList(
        new User("bugs.bunny@acme.org", "Bugs", "Bunny", testDate),
        new User("elmer.fudd@acme.org", "Elmer", "Fudd", testDate)));

    mockMvc.perform(delete("/users/daffy.duck@acme.org/"))
        .andExpect(status().isOk());

    User deletedUser = new User("daffy.duck@acme.org", null, null, null);
    Mockito.verify(mockRepository, Mockito.times(1)).delete(deletedUser);
  }

  @Test
  public void deleteUserWithInvalidEmail() throws Exception {
    mockMvc.perform(delete("/users/xy/"))
        .andExpect(status().isBadRequest());

    Mockito.verifyZeroInteractions(mockRepository);
  }
  
  @Test
  public void updateExistingUser() throws Exception {
    User user = new User("daffy.duck@acme.org", "Fluffy", "Duck", testDate);
    ObjectMapper mapper = new ObjectMapper();
    String userJson = mapper.writeValueAsString(user);
    
    Mockito.when(mockRepository.exists(user.getEmail())).thenReturn(true);

    mockMvc.perform(put("/users/" + user.getEmail() + "/")
        .contentType(MediaType.APPLICATION_JSON)
        .content(userJson))

    .andExpect(status().isOk());

    Mockito.verify(mockRepository, Mockito.times(1)).save(user);
  }
  
  @Test
  public void emailCannotBeUpdated() throws Exception {
    User user = new User("fluffy.duck@acme.org", "Fluffy", "Duck", testDate);
    ObjectMapper mapper = new ObjectMapper();
    String userJson = mapper.writeValueAsString(user);
    
    mockMvc.perform(put("/users/daffy.duck@acme.org/")
        .contentType(MediaType.APPLICATION_JSON)
        .content(userJson))

    .andExpect(status().isBadRequest());

    Mockito.verify(mockRepository, Mockito.times(0)).save(user);
  }
}

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
        new User(1, "bugs.bunny@acme.org", "Bugs", "Bunny", testDate),
        new User(2, "daffy.duck@acme.org", "Daffy", "Duck", testDate),
        new User(3, "elmer.fudd@acme.org", "Elmer", "Fudd", testDate)));

    mockMvc.perform(get("/users"))
        .andExpect(status().isOk())
        .andExpect(content().contentType("application/json;charset=UTF-8"))
        .andExpect(jsonPath("$", hasSize(3)))
        .andExpect(jsonPath("$[0].id", is(1)))
        .andExpect(jsonPath("$[0].email", is("bugs.bunny@acme.org")))
        .andExpect(jsonPath("$[0].firstName", is("Bugs")))
        .andExpect(jsonPath("$[0].lastName", is("Bunny")))
//        .andExpect(jsonPath("$[0].birthdate", is("1936-03-05")))
        .andExpect(jsonPath("$[1].id", is(2)))
        .andExpect(jsonPath("$[1].email", is("daffy.duck@acme.org")))
        .andExpect(jsonPath("$[1].firstName", is("Daffy")))
        .andExpect(jsonPath("$[1].lastName", is("Duck")))
        .andExpect(jsonPath("$[2].id", is(3)))
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
  public void registerUserWithExistingEmailFails() throws Exception {
    User user = new User("daffy.duck@acme.org", "Daffy", "Duck", testDate);
    ObjectMapper mapper = new ObjectMapper();
    String userJson = mapper.writeValueAsString(user);
    
    Mockito.when(mockRepository.findByEmail(user.getEmail())).thenReturn(user);

    mockMvc.perform(post("/users")
        .contentType(MediaType.APPLICATION_JSON)
        .content(userJson))
    .andExpect(status().isConflict())
    .andExpect(jsonPath("$.fieldErrors.email", is("User with email 'daffy.duck@acme.org' already exists")));

    Mockito.verify(mockRepository, Mockito.times(0)).save(user);
  }
  
  @Test
  public void registerUserWithInvalidDataFails() throws Exception {
    User user = new User(1, null, null, null, null);    
    ObjectMapper mapper = new ObjectMapper();
    String userJson = mapper.writeValueAsString(user);
    
    mockMvc.perform(post("/users")
        .contentType(MediaType.APPLICATION_JSON)
        .content(userJson))
    .andExpect(status().isBadRequest())
    .andExpect(jsonPath("$.fieldErrors.email", is("may not be empty")))
    .andExpect(jsonPath("$.fieldErrors.lastName", is("may not be empty")))
    .andExpect(jsonPath("$.fieldErrors.birthdate", is("may not be null")));
  
    Mockito.verify(mockRepository, Mockito.times(0)).save(user);
  }
  
  @Test
  public void registerUserWithInvalidDataFails2() throws Exception {
    User user = new User(1, "bugs.bunny", null, null, testDate);    
    ObjectMapper mapper = new ObjectMapper();
    String userJson = mapper.writeValueAsString(user);
    
    mockMvc.perform(post("/users")
        .contentType(MediaType.APPLICATION_JSON)
        .content(userJson))
    .andExpect(status().isBadRequest())
    .andExpect(jsonPath("$.fieldErrors.email", is("not a well-formed email address")));
  
    Mockito.verify(mockRepository, Mockito.times(0)).save(user);
  }

  @Test
  public void idOfNewUserIsIgnored() throws Exception {
    User user = new User(12, "daffy.duck@acme.org", "Daffy", "Duck", testDate);
    ObjectMapper mapper = new ObjectMapper();
    String userJson = mapper.writeValueAsString(user);
    
    mockMvc.perform(post("/users")
        .contentType(MediaType.APPLICATION_JSON)
        .content(userJson))

    .andExpect(status().isCreated());

    // Check that user id is set to 0 so that it gets generated
    user.setId(0);
    Mockito.verify(mockRepository, Mockito.times(1)).save(user);
  }

  @Test
  public void deleteUser() throws Exception {
    mockMvc.perform(delete("/users/2"))
        .andExpect(status().isOk());

    Mockito.verify(mockRepository, Mockito.times(1)).delete(2L);
  }
  
  @Test
  public void updateExistingUser() throws Exception {
    User user = new User(2, "daffy.duck@acme.org", "Fluffy", "Duck", testDate);
    ObjectMapper mapper = new ObjectMapper();
    String userJson = mapper.writeValueAsString(user);
    
    Mockito.when(mockRepository.exists(2L)).thenReturn(true);

    mockMvc.perform(put("/users/" + user.getId())
        .contentType(MediaType.APPLICATION_JSON)
        .content(userJson))
    .andExpect(status().isOk());

    Mockito.verify(mockRepository, Mockito.times(1)).save(user);
  }
  
  @Test
  public void updateUserWithInvalidDataFails() throws Exception {
    User user = new User(1, null, null, null, null);    
    ObjectMapper mapper = new ObjectMapper();
    String userJson = mapper.writeValueAsString(user);
    
    mockMvc.perform(put("/users/1")
        .contentType(MediaType.APPLICATION_JSON)
        .content(userJson))
    .andExpect(status().isBadRequest())
    .andExpect(jsonPath("$.fieldErrors.email", is("may not be empty")))
    .andExpect(jsonPath("$.fieldErrors.lastName", is("may not be empty")))
    .andExpect(jsonPath("$.fieldErrors.birthdate", is("may not be null")));
  
    Mockito.verify(mockRepository, Mockito.times(0)).save(user);
  }
  
  @Test
  public void changeOfUnknownUserFails() throws Exception {
    User user = new User(2, "bugs.bunny@acme.org", "Fluffy", "Duck", testDate);
    ObjectMapper mapper = new ObjectMapper();
    String userJson = mapper.writeValueAsString(user);
    
    Mockito.when(mockRepository.exists(2L)).thenReturn(false);
    
    mockMvc.perform(put("/users/2")
        .contentType(MediaType.APPLICATION_JSON)
        .content(userJson))
    .andExpect(status().isNotFound())
    .andExpect(jsonPath("$.errorMessage", is("The user with id 2 does not exist")));

    Mockito.verify(mockRepository, Mockito.times(0)).save(user);
  }
  
  @Test
  public void changeEmailToExistingEmailFails() throws Exception {
    User existingUser = new User(1, "bugs.bunny@acme.org", "Bugs", "Bunny", testDate);
    
    User user = new User(2, "bugs.bunny@acme.org", "Duffy", "Duck", testDate);
    ObjectMapper mapper = new ObjectMapper();
    String userJson = mapper.writeValueAsString(user);
    
    Mockito.when(mockRepository.exists(2L)).thenReturn(true);
    Mockito.when(mockRepository.findByEmail(user.getEmail())).thenReturn(existingUser);
    
    mockMvc.perform(put("/users/2")
        .contentType(MediaType.APPLICATION_JSON)
        .content(userJson))
    .andExpect(status().isConflict())
    .andExpect(jsonPath("$.fieldErrors.email", is("User with email 'bugs.bunny@acme.org' already exists")));

    Mockito.verify(mockRepository, Mockito.times(0)).save(user);
  }
}

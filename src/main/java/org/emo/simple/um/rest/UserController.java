package org.emo.simple.um.rest;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.EntityExistsException;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.emo.simple.um.dao.UserRepository;
import org.emo.simple.um.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@Controller
@RequestMapping("/users")
public class UserController {

  private static final String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
      + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

  private UserRepository repository;

  @Autowired
  private UserController(UserRepository repository) {
    this.repository = repository;
  }

  @RequestMapping(method = RequestMethod.GET)
  public @ResponseBody Iterable<User> listAll() {
    Iterable<User> allUsers = repository.findAll();

    return allUsers;
  }

  @RequestMapping(method = RequestMethod.POST)
  @ResponseStatus(HttpStatus.CREATED)
  public @ResponseBody void registerUser(@Valid @RequestBody User user, BindingResult result) throws Exception {
    if (result.hasErrors()) {
      throw new FormValidationException(result);
    }
    
    if (repository.exists(user.getEmail())) {
      throw new EntityExistsException(String.format("User with email '%s' already exists", user.getEmail()));
    }
    
    repository.save(user);
  }

  @RequestMapping(value = "/{email}/", method = RequestMethod.DELETE)
  public @ResponseBody void deleteUser(HttpServletResponse response, @PathVariable("email") String email)
      throws IOException {

    Pattern emailPattern = Pattern.compile(EMAIL_PATTERN);
    Matcher emailMatcher = emailPattern.matcher(email);
    if (!emailMatcher.matches()) {
      throw new IllegalArgumentException(email + " is not a valid email address");
    }

    email = email.toLowerCase();
    repository.delete(new User(email, null, null));
  }

  @ExceptionHandler(IllegalArgumentException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public @ResponseBody String parameterValidationError(IllegalArgumentException ex) {
    return ex.getMessage();
  }
  
  @ExceptionHandler(EntityExistsException.class)
  @ResponseStatus(HttpStatus.CONFLICT)
  public @ResponseBody String userConflictError(EntityExistsException ex) {
    return ex.getMessage();
  }
  
  @ExceptionHandler(FormValidationException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public @ResponseBody Map<String, String> formValidationError(FormValidationException ex) {
    Map<String, String> errors = new HashMap<String, String>();
    
    BindingResult result = ex.getResult();
    for (FieldError error : result.getFieldErrors()) {
      errors.put(error.getField(), error.getDefaultMessage());
    }
    
    return errors;
  }
}

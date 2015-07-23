package org.emo.simple.um.rest;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityExistsException;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.emo.simple.um.dao.UserRepository;
import org.emo.simple.um.entity.User;
import org.hibernate.validator.internal.constraintvalidators.EmailValidator;
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

  @Autowired
  private UserRepository repository;

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

    EmailValidator emailValidator = new EmailValidator();
    if (!emailValidator.isValid(email, null)) {
      throw new IllegalArgumentException(email + " is not a valid email address");
    }

    email = email.toLowerCase();
    repository.delete(new User(email, null, null, null));
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

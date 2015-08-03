package org.emo.simple.um.rest;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityExistsException;
import javax.persistence.EntityNotFoundException;
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

    User existingUser = repository.findByEmail(user.getEmail());
    if (existingUser != null && existingUser.getEmail().equalsIgnoreCase(user.getEmail())) {
      throw new EntityExistsException(String.format("User with email '%s' already exists", user.getEmail()));
    }

    user.setId(0);
    repository.save(user);
  }

  @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
  public @ResponseBody void updateUser(@PathVariable("id") long id, @Valid @RequestBody User user,
      BindingResult result) throws FormValidationException {
    if (result.hasErrors()) {
      throw new FormValidationException(result);
    }
    
    if (!repository.exists(id)) {
      throw new EntityNotFoundException(String.format("The user with id %d does not exist", id));
    }
    
    user.setId(id);

    User existingUser = repository.findByEmail(user.getEmail());
    if (existingUser != null && existingUser.getId() != id) {
      throw new EntityExistsException(String.format("User with email '%s' already exists", user.getEmail()));
    }

    repository.save(user);
  }

  @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
  public @ResponseBody void deleteUser(@PathVariable("id") long id) throws IOException {
    repository.delete(id);
  }

  @ExceptionHandler(EntityExistsException.class)
  @ResponseStatus(HttpStatus.CONFLICT)
  public @ResponseBody ErrorResponse userConflictError(EntityExistsException ex) {
    ErrorResponse response = new ErrorResponse();
    response.getFieldErrors().put("email", ex.getMessage());
    return response;
  }
  
  @ExceptionHandler(EntityNotFoundException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  public @ResponseBody ErrorResponse unknownUser(EntityNotFoundException ex) {
    ErrorResponse response = new ErrorResponse();
    response.setErrorMessage(ex.getMessage());
    return response;
  }

  @ExceptionHandler(FormValidationException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public @ResponseBody ErrorResponse formValidationError(FormValidationException ex) {
    ErrorResponse response = new ErrorResponse();

    BindingResult result = ex.getResult();
    for (FieldError error : result.getFieldErrors()) {
      response.getFieldErrors().put(error.getField(), error.getDefaultMessage());
    }

    return response;
  }

  @SuppressWarnings("unused")
  private class ErrorResponse {
    private String errorMessage;
    private Map<String, String> fieldErrors = new HashMap<String, String>();

    public String getErrorMessage() {
      return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
      this.errorMessage = errorMessage;
    }

    public Map<String, String> getFieldErrors() {
      return fieldErrors;
    }

    public void setFieldErrors(Map<String, String> fieldErrors) {
      this.fieldErrors = fieldErrors;
    }
  }
}

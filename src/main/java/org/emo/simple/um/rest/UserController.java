package org.emo.simple.um.rest;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.emo.simple.um.dao.UserRepository;
import org.emo.simple.um.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

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

  @RequestMapping(value = "/{email}/", method = RequestMethod.DELETE)
  public void deleteUser(HttpServletResponse response, @PathVariable("email") String email) throws IOException {
    if (StringUtils.isEmpty(email)) {
      response.sendError(HttpStatus.BAD_REQUEST.value(), "Invalid email " + email);
      return;
    }
    
    email = email.toLowerCase();
    repository.delete(new User(email, null, null));

    User deletedUser = repository.findByEmail(email);
    
    if (deletedUser != null) {
      response.sendError(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to delete user");
    }
  }
}

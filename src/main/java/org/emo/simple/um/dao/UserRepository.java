package org.emo.simple.um.dao;

import org.emo.simple.um.entity.User;
import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<User, Long> {
  public User findByEmail(String email);
}
package org.emo.simple.um.entity;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.validator.constraints.Email;

@Entity
@Table(name = "users")
public class User {

  @Email
  @Id
  private String email;
  private String firstName;
  private String lastName;

  protected User() {
  }

  public User(String email, String firstName, String lastName) {
    this.setEmail(email);
    this.setFirstName(firstName);
    this.setLastName(lastName);
  }

  @Override
  public String toString() {
    return String.format(
        "User[email=%s, firstName='%s', lastName='%s']",
        email, firstName, lastName);
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }
  
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof User) {
      User that = (User) obj;
      if (this.email == null) {
        return that.email == null;
      } else {
        return this.email.equals(that.email);
      }
    }
    
    return false;
  }
}

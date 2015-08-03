package org.emo.simple.um.entity;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;

import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;

@Entity
@Table(name = "users")
public class User {

  @Id
  @GeneratedValue(strategy=GenerationType.IDENTITY)
  private long id;
  
  @Email
  @NotBlank
  private String email;
  
  private String firstName;
  
  @NotBlank
  private String lastName;
  
  @Temporal(TemporalType.DATE)
  @DateTimeFormat(iso = ISO.DATE)
  @Past
  @NotNull
  private Date birthdate;

  public User() {
  }

  public User(String email, String firstName, String lastName, Date birthdate) {
    this.setEmail(email);
    this.setFirstName(firstName);
    this.setLastName(lastName);
    this.setBirthdate(birthdate);
  }
  
  public User(long id, String email, String firstName, String lastName, Date birthdate) {
    this(email, firstName, lastName, birthdate);
    this.id = id;
  }

  @Override
  public String toString() {
    return String.format(
        "User {id=%s, email=%s, firstName='%s', lastName='%s', birthdate='%s'}",
        id, email, firstName, lastName, birthdate.toString());
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    if (email != null) {
      email = email.toLowerCase();
    }
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

  public Date getBirthdate() {
    return birthdate;
  }

  public void setBirthdate(Date birthdate) {
    this.birthdate = birthdate;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof User) {
      User that = (User) obj;
      return this.id == that.id;
    }
    
    return false;
  }
}

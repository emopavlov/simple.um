package org.emo.simple.um.rest;

import org.springframework.validation.BindingResult;

class FormValidationException extends Exception {

  private static final long serialVersionUID = -3966137688516474630L;
  
  private BindingResult result;

  public FormValidationException(BindingResult result) {
    this.setResult(result);
  }

  public BindingResult getResult() {
    return result;
  }

  public void setResult(BindingResult result) {
    this.result = result;
  }

}

var umModule = angular.module('simpleUm', ['ngDialog']);

umModule.controller('simpleUmController', function($scope, $http, ngDialog) {
  
  $scope.appName = "Simple User Manangement";
  $scope.sortProperty = 'email';
  $scope.reverseSort = false;
  $scope.today = new Date();
  
  $scope.openDialog = function(user) {
    $scope._setCurrentUser(user);
    var dialog = ngDialog.open({
      template : 'registerUserDialog',
      controller: 'simpleUmController',
      className: 'ngdialog-theme-default',
      scope: $scope
    });
  };
  
  $scope._setCurrentUser = function(user) {
    $scope.currentUser = {};
    $scope.currentUser.firstName = user.firstName;
    $scope.currentUser.lastName = user.lastName;
    $scope.currentUser.email = user.email;
    $scope.currentUser.birthdate = new Date(user.birthdate);
  }
  
  $scope.openAddDialog = function() {
    $scope.dialogMode = "add";
    $scope.openDialog({});
  };
  
  $scope.openEditDialog = function(user) {
    $scope.dialogMode = "edit";
    $scope.openDialog(user);
  };
  
  
  $scope.listUsers = function() {
    $http.get("/users").success(function(data) {
      $scope.users = data;
    });
  };
  
  $scope.registerUser = function(newUser) {
    $http.post("/users", newUser).success(function() {
      $scope.closeThisDialog();
    }).error(function(data){
      $scope.setError(data);
    });
  };
  
  $scope.updateUser = function(user) {
    $http.put("/users/" + user.email + "/", user).success(function(data) {
      $scope.closeThisDialog();
    }).error(function(data, status, headers, config){
      $scope.setError(data);
    });
  };
  
  $scope.deleteUser = function(email) {
    $http.delete("/users/" + email + "/").success(function() {
      $scope.closeThisDialog();
    }).error(function(data){
      $scope.setError(data);
    });
  };
  
  $scope.isToShowChevron = function(property, direction) {
    var selectedDirection = $scope.reverseSort ? 'up' : 'down';
    return selectedDirection == direction && $scope.sortProperty == property;
  };
  
  $scope.setSortBy = function(property) {
    if (property != $scope.sortProperty) {
      $scope.sortProperty = property;
      $scope.reverseSort = false;
    } else {
      $scope.reverseSort = !$scope.reverseSort;
    }
  };
  
  $scope.setError = function(data) {
    $scope.errors = {};
    
    if (data.errorMessage) {
      $scope.hasErrors = true;
      $scope.errors.errorText = data.errorMessage;
    }
    
    if (data.fieldErrors) {
      $scope.errors.fieldErrors = data.fieldErrors;
    }
  };
  
  $scope.clearErrors = function(field) {
    if ($scope.errors && $scope.errors.fieldErrors) {
      $scope.errors.fieldErrors[field] = null;
    }
  }

  $scope.listUsers();  
});
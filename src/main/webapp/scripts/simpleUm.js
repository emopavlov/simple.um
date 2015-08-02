var umModule = angular.module('simpleUm', ['ngDialog']);

umModule.controller('simpleUmController', function($scope, $http, ngDialog) {
  
  $scope.sortProperty = 'email';
  $scope.reverseSort = false;
  
  $scope.openDialog = function(user) {
    $scope._setCurrentUser(user);
    var dialog = ngDialog.open({
      template : 'registerUserDialog',
      controller: 'userFormController',
      className: 'ngdialog-theme-default',
      scope: $scope
    });
  };
  
  $scope._setCurrentUser = function(user) {
    this.currentUser = {};
    this.currentUser.firstName = user.firstName;
    this.currentUser.lastName = user.lastName;
    this.currentUser.email = user.email;
    this.currentUser.birthdate = new Date(user.birthdate);
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
  
  $scope.$on('usersUpdatedEvent', function(event, args) {
    $scope.listUsers();
  });

  $scope.listUsers();  
});

umModule.controller('userFormController', function($scope, $http, ngDialog) {
  
  $scope.today = new Date();
  
  $scope.registerUser = function(newUser) {
    $scope.handleServerResponse($http.post("/users", newUser));
  };
  
  $scope.updateUser = function(user) {
    $scope.handleServerResponse($http.put("/users/" + user.email + "/", user));
  };
  
  $scope.deleteUser = function(email) {
    $scope.handleServerResponse($http.delete("/users/" + email + "/"));
  };

  $scope.handleServerResponse = function(httpPromise) {
    httpPromise.success(function(data) {
      $scope.$emit('usersUpdatedEvent', null);
      $scope.closeThisDialog();
    }).error(function(data, status, headers, config){
      $scope.setError(data);
    });
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
});
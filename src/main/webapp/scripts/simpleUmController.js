angular.module('simpleUm', ['ngDialog'])
  .controller('simpleUmController', function($scope, $http, ngDialog) {
  $scope.appName = "Simple User Manangement";

  $scope.openAddDialog = function() {
    ngDialog.open({
      template : 'registerUserDialog',
      controller: 'simpleUmController',
      className: 'ngdialog-theme-default'
    });
  };
  
  $scope.registerUser = function(newUser) {
    $http.post("/users", newUser).success(function(response) {
      
    }).error(function(response) {
      
    });
    
  };
  
  $scope.deleteUser = function(email) {
    $http.delete("/users/" + email + "/").success(function(response) {
      $scope.listUsers();
    });
  };

  $scope.listUsers = function() {
    $http.get("/users").success(function(response) {
      $scope.users = response;
    });
  };
  
  $scope.listUsers();
});
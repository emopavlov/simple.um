angular.module('simpleUm', ['ngDialog'])
  .controller('simpleUmController', function($scope, $http, ngDialog) {
  
  $scope.appName = "Simple User Manangement";
  $scope.sortProperty = 'email';
  $scope.today = new Date();
  
  $scope.openAddDialog = function() {
    var dialog = ngDialog.open({
      template : 'registerUserDialog',
      controller: 'simpleUmController'
    });
  };
  
  $scope.registerUser = function(newUser) {
    $http.post("/users", newUser).success(function(response) {
    	$scope.listUsers();
      $scope.closeThisDialog();
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
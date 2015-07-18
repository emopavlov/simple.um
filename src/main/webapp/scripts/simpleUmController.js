angular.module('simpleUm', ['ngDialog'])
  .controller('simpleUmController', function($scope, $http, ngDialog) {
  $scope.appName = "Simple User Manangement";

  $scope.openAddDialog = function() {
    ngDialog.open({
      template : '<p>This is a simple dialog</p>',
      className: 'ngdialog-theme-default',
      plain : true
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
'use strict';

angular.module('patientviewApp').controller('AccountCtrl', ['localStorageService', 'UserService', 'AuthService', '$scope', '$rootScope', 'UtilService', 'FileUploader',
    function (localStorageService, UserService, AuthService, $scope, $rootScope, UtilService, FileUploader) {

    $scope.pw ='';

    if ($rootScope.loggedInUser == null) {
        $rootScope.logout();
    }

    UserService.get($rootScope.loggedInUser.id).then(function(data) {
        $scope.userdetails = data;
        $scope.userdetails.confirmEmail = $scope.userdetails.email;
        // use date parameter (not used in Spring controller) to force refresh of picture by angular after upload
        $scope.userPicture = '/api/user/' + $rootScope.loggedInUser.id + '/picture?token=' + $rootScope.authToken;
        $scope.datedUserPicture = $scope.userPicture + '&date=' + (new Date()).toString();
    });

    $scope.saveSettings = function () {
        // If the email field has been changed validate emails
        $scope.settingsSuccessMessage = null;
        $scope.settingsErrorMessage = null;

        if (!$scope.userdetails.confirmEmail) {
            $scope.settingsErrorMessage = 'Please confirm the email address';
        } else {
            // Email equal and correct
            if (($scope.userdetails.confirmEmail === $scope.userdetails.email)) {
                if (UtilService.validateEmail($scope.userdetails.email)) {
                    $scope.settingsErrorMessage = 'Invalid format for email';
                } else {
                    UserService.saveOwnSettings($scope.loggedInUser.id, $scope.userdetails).then(function () {
                        $scope.settingsSuccessMessage = 'The settings have been saved';
                        AuthService.getUserInformation($scope.loggedInUser.userInformation.token)
                            .then(function (userInformation) {

                            // get user information, store in session
                            var user = userInformation.user;
                            delete userInformation.user;
                            user.userInformation = userInformation;

                            $rootScope.loggedInUser = user;
                            localStorageService.set('loggedInUser', user);

                        }, function(result) {
                            if (result.data) {
                                $scope.settingsErrorMessage = result.data;
                            } else {
                                delete $scope.settingsErrorMessage;
                            }
                            $scope.loading = false;
                        });

                    }, function (result) {
                        $scope.settingsErrorMessage = 'The settings have not been saved ' + result.data;
                    });
                }
            } else {
                $scope.settingsErrorMessage = 'The emails do not match';
            }
        }
    };

    $scope.savePassword = function () {
        $scope.passwordSuccessMessage = null;
        $scope.passwordErrorMessage = null;

        if ($scope.pw !== $scope.userdetails.confirmPassword) {
            $scope.passwordErrorMessage = 'The passwords do not match';
        } else {
            AuthService.login({'username': $scope.userdetails.username, 'password': $scope.userdetails.currentPassword}).then(function () {
                $scope.userdetails.password =  $scope.pw;

                UserService.changePassword($scope.userdetails).then(function () {
                    $scope.passwordSuccessMessage = 'The password has been saved';
                }, function () {
                    $scope.passwordErrorMessage = 'There was an error';
                });
            }, function (result) {
                if (result.data) {
                    $scope.passwordErrorMessage = 'Current password incorrect';
                } else {
                    $scope.passwordErrorMessage = ' ';
                }
            });
        }
    };

    // configure basic angular-file-upload    
    var uploader = $scope.uploader = new FileUploader({
        // note: ie8 cannot pass custom headers so must be added as query parameter
        url: '/api/user/' + $scope.loggedInUser.id + '/picture?token=' + $rootScope.authToken,
        headers: {'X-Auth-Token': $rootScope.authToken}
    });

    // callback after user selects a file
    uploader.onAfterAddingFile = function() {
        $scope.uploadError = false;
        uploader.uploadAll();
        uploader.queue = [];
    };
    
    // callback if there is a problem with an image
    uploader.onErrorItem = function(fileItem, response, status, headers) {
        $scope.uploadError = true;
        alert(response);
    };
    
    // when all uploads complete, if no error then force refresh of image by appending current date as parameter
    uploader.onCompleteAll = function() {
        if (!$scope.uploadError) {
            $scope.datedUserPicture = $scope.userPicture + '&date=' + (new Date()).toString();
        } else {
            // error during upload
        }
    };
}]);
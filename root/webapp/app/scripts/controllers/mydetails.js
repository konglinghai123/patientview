'use strict';

// invite GP modal instance controller
var InviteGpModalInstanceCtrl = ['$rootScope', '$scope', '$modalInstance','practitioner', 'GpService',
    function ($rootScope, $scope, $modalInstance, practitioner, GpService) {
        $scope.practitioner = practitioner;
        $scope.inviteGp = function () {
            GpService.inviteGp($rootScope.loggedInUser.id, practitioner).then(function() {
                $modalInstance.close();
            });
        };
        $scope.cancel = function () {
            $modalInstance.dismiss('cancel');
        };
    }];

angular.module('patientviewApp').controller('MydetailsCtrl',['$scope', 'PatientService', 'UserService', '$modal', 'GpService',
function ($scope, PatientService, UserService, $modal, GpService) {

    $scope.init = function(){
        var i;
        $scope.moreAboutMeMessage = '';
        $scope.loading = true;
        $scope.moreAboutMe = {};

        // get latest user information
        UserService.getInformation($scope.loggedInUser.id).then(function(userInformation) {

            for (i=0;i<userInformation.length;i++) {
                if (userInformation[i].type === 'SHOULD_KNOW') {
                    $scope.moreAboutMe.shouldKnow = userInformation[i].value;
                }
                if (userInformation[i].type === 'TALK_ABOUT') {
                    $scope.moreAboutMe.talkAbout = userInformation[i].value;
                }
            }

            PatientService.get($scope.loggedInUser.id).then(function (patientDetails) {
                $scope.patientDetails = patientDetails;

                // set checkboxes
                for (var i=0;i<$scope.patientDetails.length;i++) {
                    $scope.patientDetails[i].group.selected = true;

                    if ($scope.patientDetails[i].fhirPatient.gender === 'M') {
                        $scope.patientDetails[i].fhirPatient.gender = 'Male';
                    }

                    if ($scope.patientDetails[i].fhirPatient.gender === 'F') {
                        $scope.patientDetails[i].fhirPatient.gender = 'Female';
                    }
                }

                $scope.loading = false;
            }, function () {
                $scope.loading = false;
                alert('Error getting patient details');
            });
        }, function () {
            $scope.loading = false;
            alert('Error getting user information');
        });
    };

    $scope.saveMoreAboutMe = function() {
        $scope.moreAboutMeMessage = '';
        UserService.saveMoreAboutMe($scope.loggedInUser, $scope.moreAboutMe).then(function() {
            $scope.moreAboutMeMessage = 'Saved your information';
        }, function () {
            $scope.moreAboutMeMessage = 'Error saving your information';
        });
    };

    $scope.inviteGp = function(practitioner) {
        var modalInstance = $modal.open({
            templateUrl: 'inviteGpModal.html',
            controller: InviteGpModalInstanceCtrl,
            resolve: {
                practitioner: function(){
                    return practitioner;
                },
                GpService: function(){
                    return GpService;
                }
            }
        });

        modalInstance.result.then(function () {
            // ok
        }, function () {
            // closed
        });
    };

    $scope.init();
}]);

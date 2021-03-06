'use strict';
angular.module('patientviewApp').controller('MydetailsCtrl',['$scope', 'PatientService', 'UserService', '$modal', 'GpService', '$rootScope',
function ($scope, PatientService, UserService, $modal, GpService, $rootScope) {

    $scope.init = function(){
        var i;
        $scope.moreAboutMeMessage = '';
        $scope.loading = true;
        $scope.moreAboutMe = {};

        // check if viewing as patient
        $scope.isStaff = $rootScope.previousLoggedInUser ? true : false;

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

    $scope.inviteGp = function(fhirPatient, practitioner, groupId) {

        // build simple object to send to back end
        var patient = {};
        patient.forename = fhirPatient.forename;
        patient.surname = fhirPatient.surname;
        if (fhirPatient.dateOfBirthNoTime) {
            patient.dateOfBirth = new Date(fhirPatient.dateOfBirthNoTime);
        }
        patient.practitioners = [];
        patient.practitioners.push(practitioner);
        patient.identifiers = [];
        patient.identifiers.push(fhirPatient.identifiers[0]);
        patient.group = {};
        patient.group.id = groupId;

        var modalInstance = $modal.open({
            templateUrl: 'inviteGpModal.html',
            controller: InviteGpModalInstanceCtrl,
            resolve: {
                patient: function(){
                    return patient;
                },
                GpService: function(){
                    return GpService;
                }
            }
        });

        modalInstance.result.then(function () {

        }, function () {
            // closed
            $scope.init();
        });
    };

    $scope.init();
}]);

'use strict';

angular.module('patientviewApp').controller('ConversationDetailsCtrl', ['$scope', 'ConversationService',
function ($scope, ConversationService) {

    $scope.selectGroup = function(conversation, groupId) {
        $scope.modalLoading = true;

        ConversationService.getRecipients($scope.loggedInUser.id, groupId).then(function (recipientMap) {
            var availableRecipients = [];
            var restangularObjects
                = ['route','reqParams','fromServer','parentResource','restangularCollection','singleOne'];
            var sortOrder = ['Unit Admin', 'Unit Staff', 'Patient'];
            var keys = [];
            var i, j;

            for (var key in recipientMap) {
                if (recipientMap.hasOwnProperty(key) && typeof(recipientMap[key]) !== 'function'
                    && !(restangularObjects.indexOf(key) > -1)) {
                    keys.push(key);
                }
            }

            // order keys accordingly
            var result = [];
            for(i=0; i<sortOrder.length; i++) {
                for (j=0; j<keys.length; j++) {
                    if (keys[j] == sortOrder[i] && !(result.indexOf(keys[j]) > -1)) {
                        result.push(keys[j]);
                    }
                }
            }

            // add any remaining keys
            for (i=0; i<keys.length; i++) {
                if (!(result.indexOf(keys[i]) > -1)) {
                    result.push(keys[i]);
                }
            }

            // add in order to recipients, with disabled option describing role
            for (i=0; i<result.length; i++) {
                if (recipientMap[result[i]] !== undefined) {
                    if (recipientMap[result[i]].length) {
                        var element = {};
                        element.description = ' ';
                        availableRecipients.push(element);
                        element = {};
                        element.description = result[i];
                        availableRecipients.push(element);
                    }

                    var temp = [];
                    for (j = 0; j < recipientMap[result[i]].length; j++) {
                        availableRecipients.push(recipientMap[result[i]][j]);
                        temp.push(recipientMap[result[i]][j]);
                    }
                }
            }

            conversation.availableRecipients = availableRecipients;
            $scope.modalLoading = false;
        }, function (failureResult) {
            if (failureResult.status === 404) {
                $scope.modalLoading = false;
            } else {
                $scope.modalLoading = false;
                alert('Error loading message recipients');
            }
        });
    };

    $scope.addRecipient = function (form, conversation, userId) {

        var found = false;
        var i;

        // check not already added
        for (i = 0; i < conversation.recipients.length; i++) {
            // need to cast string to number using == not === for id
            if (conversation.recipients[i].id == userId) {
                found = true;
            }
        }

        if (!found && userId !== undefined) {
            for (i = 0; i < conversation.availableRecipients.length; i++) {
                if (conversation.availableRecipients[i].id == userId) {
                    conversation.recipients.push(conversation.availableRecipients[i]);
                }
            }
        }
    };

    $scope.removeRecipient = function (form, conversation, user) {
        for (var i = 0; i < conversation.recipients.length; i++) {
            if (conversation.recipients[i].id === user.id) {
                conversation.recipients.splice(i, 1);
            }
        }
    };
}]);

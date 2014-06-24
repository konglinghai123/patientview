'use strict';

angular.module('patientviewApp').factory('UserService', ['$q', 'Restangular', 'UtilService',
function ($q, Restangular, UtilService) {
    return {
        get: function (userId) {
            var deferred = $q.defer();
            Restangular.one('user', userId).get().then(function(res) {
                deferred.resolve(res);
            });
            return deferred.promise;
        },
        delete: function (user) {
            var deferred = $q.defer();
            Restangular.one('user', user.id).get().then(function(user) {
                user.remove().then(function(res) {
                    deferred.resolve(res);
                });
            });
            return deferred.promise;
        },
        resetPassword: function (user) {
            var deferred = $q.defer();
            var generatedPassword = UtilService.generatePassword();
            Restangular.one('user', user.id).post('reset', generatedPassword).then(function(successResult) {
                deferred.resolve(successResult);
                successResult.password = user.password;
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
        sendVerificationEmail: function (user) {
            var deferred = $q.defer();
            Restangular.one('user', user.id).post('reset').then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
        save: function (inputUser) {
            var deferred = $q.defer();

            // clean user object
            var user = {};
            var userFields = ['id', 'username', 'password', 'email', 'name', 'changePassword', 'locked', 'userFeatures'];
            for (var userField in inputUser) {
                if (inputUser.hasOwnProperty(userField) && _.contains(userFields, userField)) {
                    user[userField] = inputUser[userField];
                }
            }

            // clean group roles
            user.groupRoles = [];
            for (var i=0;i<inputUser.groupRoles.length;i++) {
                var inputGroupRole = inputUser.groupRoles[i];
                var groupRole = {};

                // clean role
                var role = {}, roleFields = ['id','name','description','routes'];
                for (var roleField in inputGroupRole.role) {
                    if (inputGroupRole.role.hasOwnProperty(roleField) && _.contains(roleFields, roleField)) {
                        role[roleField] = inputGroupRole.role[roleField];
                    }
                }
                groupRole.role = role;

                // clean group
                var group = {}, groupFields = ['id','name','code','description','groupType','groupFeatures','routes'];
                for (var groupField in inputGroupRole.group) {
                    if (inputGroupRole.group.hasOwnProperty(groupField) && _.contains(groupFields, groupField)) {
                        group[groupField] = inputGroupRole.group[groupField];
                    }
                }
                groupRole.group = group;

                user.groupRoles.push(groupRole);
            }

            // clean user features
            var cleanUserFeatures = [];
            for (var j=0;j<inputUser.userFeatures.length;j++) {
                var userFeature = inputUser.userFeatures[j];
                var feature = {'id':userFeature.feature.id,'name':userFeature.feature.name,'description':''};
                cleanUserFeatures.push({'feature':feature});
            }
            user.userFeatures = cleanUserFeatures;

            // PUT
            Restangular.all('user').customPUT(user).then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });

            return deferred.promise;
        },
        new: function (inputUser) {
            var deferred = $q.defer();
            var userFields = ['username','email','name','groupRoles','userFeatures'];

            // clean group roles
            inputUser.groupRoles = [];
            for (var i=0;i<inputUser.groups.length;i++) {
                var inputGroup = inputUser.groups[i];
                var groupRole = {};

                // clean role
                var role = {}, roleFields = ['id','name','description','routes'];
                for (var roleField in inputGroup.role) {
                    if (inputGroup.role.hasOwnProperty(roleField) && _.contains(roleFields, roleField)) {
                        role[roleField] = inputGroup.role[roleField];
                    }
                }
                groupRole.role = role;

                // clean group
                var group = {}, groupFields = ['id','name','code','description','groupType','groupFeatures','routes'];
                for (var groupField in inputGroup) {
                    if (inputGroup.hasOwnProperty(groupField) && _.contains(groupFields, groupField)) {
                        group[groupField] = inputGroup[groupField];
                    }
                }
                groupRole.group = group;

                inputUser.groupRoles.push(groupRole);
            }

            // clean user features
            var cleanUserFeatures = [];
            for (var j=0;j<inputUser.userFeatures.length;j++) {
                var userFeature = inputUser.userFeatures[j];
                var feature = {'id':userFeature.feature.id,'name':userFeature.feature.name,'description':''};
                cleanUserFeatures.push({'feature':feature});
            }

            // clean base user object
            var user = {};
            for (var field in inputUser) {
                if (inputUser.hasOwnProperty(field) && _.contains(userFields, field)) {
                    user[field] = inputUser[field];
                }
            }

            // add cleaned objects
            user.userFeatures = cleanUserFeatures;
            user.groupRoles = inputUser.groupRoles;

            // generate password
            user.password = UtilService.generatePassword();
            user.changePassword = 'false';
            user.locked = 'false';

            Restangular.all('user').post(user).then(function(successResult) {
                deferred.resolve(successResult);
                successResult.password = user.password;
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        }
    };
}]);

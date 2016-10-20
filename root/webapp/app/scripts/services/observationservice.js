'use strict';

angular.module('patientviewApp').factory('ObservationService', ['$q', 'Restangular', 'UtilService',
function ($q, Restangular, UtilService) {
    return {
        // Migration only
        startObservationMigration: function () {
            var deferred = $q.defer();
            Restangular.one('migrate/observationsfast').get().then(function(res) {
                deferred.resolve(res);
            });
            return deferred.promise;
        },
        getObservation: function (uuid, typeName) {
            var deferred = $q.defer();
            Restangular.one('patient',uuid).getList('observations', {type: typeName}).then(function(res) {
                deferred.resolve(res);
            });
            return deferred.promise;
        },
        getResultTypes: function (uuid) {
            var deferred = $q.defer();
            Restangular.one('patient',uuid).getList('resulttypes').then(function(res) {
                deferred.resolve(res);
            });
            return deferred.promise;
        },
        getByCode: function (userId, code) {
            var deferred = $q.defer();
            // GET /user/{userId}/observations/{code}
            Restangular.one('user', userId).one('observations').one(code).get().then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
        getByCodePatientEntered: function (userId, code) {
            var deferred = $q.defer();
            // GET /user/{userId}/observations/{code}/patiententered
            Restangular.one('user', userId).one('observations').one(code).one('patiententered').get().then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
        getByCodes: function (userId, codes, limit, offset, orderDirection) {
            var deferred = $q.defer();
            // GET /user/{userId}/observations
            Restangular.one('user', userId).one('observations')
                .get({'code':codes, 'limit':limit, 'offset':offset, 'orderDirection':orderDirection})
                .then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
        getSummary: function (userId) {
            var deferred = $q.defer();
            // GET /user/{userId}/observations/summary
            Restangular.one('user', userId).one('observations').one('summary').get().then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
        saveResultClusters: function (userId, resultClusters) {
            var toSend = [];

            for (var i=0;i<resultClusters.length;i++) {
                var userResultCluster = _.clone(resultClusters[i]);
                var values = [];

                for (var key in userResultCluster.values) {
                    if (userResultCluster.values.hasOwnProperty(key)) {
                        values.push({'id':key, 'value':userResultCluster.values[key]});
                    }
                }

                userResultCluster.values = values;
                toSend.push(UtilService.cleanObject(userResultCluster, 'resultCluster'));
            }

            var deferred = $q.defer();
            // POST /user/{userId}/observations/resultclusters
            Restangular.one('user', userId).one('observations').one('resultclusters').customPOST(toSend)
                .then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
        saveResultCluster: function (userId, resultCluster) {

            // var userResultCluster = _.clone(resultCluster);
            var userResultCluster = {};
            userResultCluster.day = resultCluster.day;
            userResultCluster.month = resultCluster.month;
            userResultCluster.year = resultCluster.year;
            userResultCluster.hour = resultCluster.hour;
            userResultCluster.minute = resultCluster.minute;


            var deferred = $q.defer();
            // POST /user/{userId}/observations/patiententered
            Restangular.one('user', userId).one('observations').one('patiententered').customPOST(userResultCluster)
                .then(function(successResult) {
                    deferred.resolve(successResult);
                }, function(failureResult) {
                    deferred.reject(failureResult);
                });
            return deferred.promise;

        },
        // Remove a single result based on userId and uuid
        remove: function (userId, observation) {
            var deferred = $q.defer();
            // DELETE /user/{userId}/observations/{uuid}
            Restangular.one('user', userId).one('observations', observation.temporaryUuid).remove().then(function (successResult) {
                deferred.resolve(successResult);
            }, function (failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        }

    };
}]);

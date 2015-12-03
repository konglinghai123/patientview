'use strict';

angular.module('patientviewApp').factory('DiagnosisService', ['$q', 'Restangular', function ($q, Restangular) {
    return {
        add: function (userId, code) {
            var deferred = $q.defer();
            // POST /user/{userId}/diagnosis/{code}
            Restangular.one('user', userId).one('diagnosis', code).post().then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        }
    };
}]);

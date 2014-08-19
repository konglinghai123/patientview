'use strict';

angular.module('patientviewApp').factory('RouteService', ['$q', 'Restangular', function ($q, Restangular) {
    return {
        getDefault: function() {
            var defaultRoutes = {
                'routes': [{
                    'url': '/verify',
                    'templateUrl': 'views/verify.html',
                    'controller': 'VerifyCtrl',
                    'title': 'Verify',
                    'lookup': {
                        'id': 5,
                        'value': 'NOT_DISPLAYED',
                        'lookupType': {
                            'id': 2,
                            'type': 'MENU',
                            'description': 'Type of menu'
                        }
                    }
                }]
            };

            return defaultRoutes;
        },
        getMainRoute: function() {
            return {
                'url': '/',
                'templateUrl': 'views/main.html',
                'controller': 'MainCtrl',
                'title': 'Home',
                'lookup': {
                    'id': 4,
                    'value': 'TOP',
                    'lookupType': {
                        'id': 2,
                        'type': 'MENU',
                        'description': 'Type of menu'
                    }
                }
            };
        },
        getLoginRoute: function() {
            return {
                'url': '/login',
                'templateUrl': 'views/login.html',
                'controller': 'LoginCtrl',
                'title': 'Login',
                'lookup': {
                    'id': 100,
                    'value': 'NOT_DISPLAYED',
                    'lookupType': {
                        'id': 2,
                        'type': 'MENU',
                        'description': 'Type of menu'
                    }
                }
            };
        },
        getLogoutRoute: function() {
            return {
                'url': '/logout',
                'templateUrl': 'views/logout.html',
                'controller': 'LogoutCtrl',
                'title': 'Log Out',
                'lookup': {
                    'id': 96,
                    'value': 'NOT_DISPLAYED',
                    'lookupType': {
                        'id': 2,
                        'type': 'MENU',
                        'description': 'Type of menu'
                    }
                }
            };
        },
        getAccountRoute: function() {
            return {
                'url': '/settings',
                'templateUrl': 'views/account.html',
                'controller': 'AccountCtrl',
                'title': 'Settings',
                'lookup': {
                    'id': 97,
                    'value': 'NOT_DISPLAYED',
                    'lookupType': {
                        'id': 2,
                        'type': 'MENU',
                        'description': 'Type of menu'
                    }
                }
            };
        },
        getJoinRequestRoute: function() {
            return {
                'url': '/joinrequest',
                'templateUrl': 'views/joinrequest.html',
                'controller': 'JoinRequestCtrl',
                'title': 'Join Request',
                'lookup': {
                    'id': 98,
                    'value': 'NOT_DISPLAYED',
                    'lookupType': {
                        'id': 2,
                        'type': 'MENU',
                        'description': 'Type of menu'
                    }
                }
            };
        },
        getChangePasswordRoute: function() {
            return {
                'url': '/changepassword',
                'templateUrl': 'views/changepassword.html',
                'controller': 'PasswordChangeCtrl',
                'title': 'Change Password',
                'lookup': {
                    'id': 99,
                    'value': 'TOP',
                    'lookupType': {
                        'id': 3,
                        'type': 'MENU',
                        'description': 'Type of menu'
                    }
                }
            };
        },
        getForgottenPasswordRoute: function() {
            return {
                'url': '/forgottenpassword',
                'templateUrl': 'views/forgottenpassword.html',
                'controller': 'ForgottenPasswordCtrl',
                'title': 'Forgotten Password',
                'lookup': {
                    'id': 101,
                    'value': 'NOT_DISPLAYED',
                    'lookupType': {
                        'id': 3,
                        'type': 'MENU',
                        'description': 'Type of menu'
                    }
                }
            };
        },
        getContactUnitRoute: function() {
            return {
                'url': '/contactunit',
                'templateUrl': 'views/contactunit.html',
                'controller': 'ContactUnitCtrl',
                'title': 'Contact Unit',
                'lookup': {
                    'id': 102,
                    'value': 'NOT_DISPLAYED',
                    'lookupType': {
                        'id': 3,
                        'type': 'MENU',
                        'description': 'Type of menu'
                    }
                }
            };
        },
        getRoutes: function (uuid) {
            var deferred = $q.defer();
            Restangular.all('security').one('user', uuid).customGET('routes').then(function (res) {
                deferred.resolve(res);
            });
            return deferred.promise;
        }
    };
}]);

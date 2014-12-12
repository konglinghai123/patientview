'use strict';

// http://blog.brunoscopelliti.com/how-to-defer-route-definition-in-an-angularjs-web-app
var $routeProviderReference;

// helper functions
// http://stackoverflow.com/questions/13153121/how-to-defer-routes-definition-in-angular-js
function pathRegExp(path, opts) {
    var insensitive = opts.caseInsensitiveMatch,
        ret = {
            originalPath: path,
            regexp: path
        },
        keys = ret.keys = [];

    path = path.replace(/([().])/g, '\\$1')
        .replace(/(\/)?:(\w+)([\?\*])?/g, function (_, slash, key, option) {
            var optional = option === '?' ? option : null;
            var star = option === '*' ? option : null;
            keys.push({
                name: key,
                optional: !! optional
            });
            slash = slash || '';
            return '' + (optional ? '' : slash) + '(?:' + (optional ? slash : '') + (star && '(.+?)' || '([^/]+)') + (optional || '') + ')' + (optional || '');
        })
        .replace(/([\/$\*])/g, '\\$1');

    ret.regexp = new RegExp('^' + path + '$', insensitive ? 'i' : '');
    return ret;
}

// module definition
var patientviewApp = angular.module('patientviewApp', [
    'LocalStorageModule',   // angular-local-storage
    'config',               // auto generated by ngConstant
    'restangular',          // restangular rest
    'ui.bootstrap',         // angular ui boostrap
    'ngSanitize',           // angular sanitize for more html parsing
    'ngCookies',
    'ngResource',
    'ngRoute'
]);

patientviewApp.filter('startFrom', function() {
    return function(input, start) {
        if(input) {
            start = +start; //parse to int
            return input.slice(start);
        }
        return [];
    };
});

// load google charts
google.load('visualization', '1', {
    packages: ['corechart','annotationChart','table']
});

patientviewApp.config(['$routeProvider', '$httpProvider', 'RestangularProvider','ENV',
    function ($routeProvider, $httpProvider, RestangularProvider, ENV) {
        $httpProvider.interceptors.push('HttpRequestInterceptor');
        $httpProvider.interceptors.push('HttpResponseInterceptor');
        RestangularProvider.setBaseUrl(ENV.apiEndpoint);
        RestangularProvider.setDefaultHeaders({ 'Content-Type': 'application/json' });
        $routeProviderReference = $routeProvider;
    }]);

patientviewApp.run(['$rootScope', '$timeout', '$location', '$cookieStore', '$cookies', '$sce', 'localStorageService', 'Restangular',
    '$route', 'RouteService', 'ENV', 'ConversationService', 'JoinRequestService', 'UserService', 'AuthService',
    function($rootScope, $timeout, $location, $cookieStore, $cookies, $sce, localStorageService, Restangular, $route,
             RouteService, ENV, ConversationService, JoinRequestService, UserService, AuthService) {

    $rootScope.ieTestMode = false;
    $rootScope.apiEndpoint = ENV.apiEndpoint;

    // rebuild routes from cookie, allow refresh of page
    var buildRoute = function() {
        var data = { 'default': '/' };
        data.routes = [];
        var menu = localStorageService.get('routes');

        if (menu !== null) {
            // handle caching issue where routes are []
            if (menu.length === 0) {
                $rootScope.logout();
            } else {
                data.routes = menu;
            }
        }

        // if cookies is disabled then data.routes will not be an array (will be false)
        if (data !== undefined && data.routes) {

            // add main/login/logout routes (for all users)
            data.routes.push(RouteService.getMainRoute());
            data.routes.push(RouteService.getVerifyRoute());
            data.routes.push(RouteService.getLogoutRoute());
            data.routes.push(RouteService.getLoginRoute());
            data.routes.push(RouteService.getAccountRoute());
            data.routes.push(RouteService.getJoinRequestRoute());
            data.routes.push(RouteService.getForgottenPasswordRoute());
            data.routes.push(RouteService.getContactUnitRoute());
            data.routes.push(RouteService.getTermsRoute());
            data.routes.push(RouteService.getPrivacyRoute());

            for (var j=0 ; j < data.routes.length; j++ ) {

                var path = data.routes[j].url;
                var route = {
                    controller: data.routes[j].controller,
                    templateUrl: data.routes[j].templateUrl,
                    title: data.routes[j].title
                };

                $route.routes[path] = angular.extend(
                    { reloadOnSearch: true },
                    route,
                        path && pathRegExp(path, route)
                );

                // create redirection for trailing slashes
                if (path)
                {
                    var redirectPath = (path[path.length - 1] === '/') ? path.substr(0, path.length - 1) : path + '/';

                    $route.routes[redirectPath] = angular.extend({
                            redirectTo: path
                        },
                        pathRegExp(redirectPath, route)
                    );
                }
            }

            $routeProviderReference.otherwise({ 'redirectTo': '/'});
            $route.reload();
        } else {
            alert("You must have cookies enabled to use PatientView");
        }
    };

    // global function to retrieve number of unread messages
    $rootScope.setUnreadConversationCount = function() {
        if ($rootScope.loggedInUser) {
            ConversationService.getUnreadConversationCount($rootScope.loggedInUser.id).then(function(unreadCount) {
                $rootScope.unreadConversationCount = unreadCount.toString();
            }, function() {

            });
        }
    };

    // global function to retrieve number of submitted join requests
    $rootScope.setSubmittedJoinRequestCount = function() {
        if ($rootScope.loggedInUser) {

            var isSuperAdmin = UserService.checkRoleExists('GLOBAL_ADMIN', $rootScope.loggedInUser);
            var isSpecialtyAdmin = UserService.checkRoleExists('SPECIALTY_ADMIN', $rootScope.loggedInUser);
            var isUnitAdmin = UserService.checkRoleExists('UNIT_ADMIN', $rootScope.loggedInUser);
            var isUnitStaff = UserService.checkRoleExists('UNIT_STAFF', $rootScope.loggedInUser);

            if (isSuperAdmin || isSpecialtyAdmin || isUnitAdmin || isUnitStaff) {
                JoinRequestService.getSubmittedJoinRequestCount($rootScope.loggedInUser.id).then(function (unreadCount) {
                    $rootScope.submittedJoinRequestCount = unreadCount.toString();
                }, function () {

                });
            }
        }
    };

    var stripScripts = function (s) {
        var div = $('<div>').html(s);
        div.find('script').remove();
        return div.html();
    };

    // global function to parse HTML (used in messaging, news)
    $rootScope.parseHTMLText = function (text) {
        if (text) {
            // manage line breaks
            text = text.replace(/(\r\n|\n|\r)/gm, '<br/>');

            // strip <script> (otherwise htmlClean crashes)
            text = stripScripts(text);

            // remove 'javascript' strings
            text = text.replace('javascript','');

            // https://github.com/components/jquery-htmlclean
            // clean html to remove all but certain tags
            var htmlCleanOptions = {
                'allowedTags': ['strong','a','b','i','u','h1','h2','h3','h4','h5','br']
            };

            text = $.htmlClean(text, htmlCleanOptions);

            // trust as html
            return $sce.trustAsHtml(text);
        }
    };

    // global function to order groups by type
    $rootScope.orderGroups = function (group) {
        if (group.groupType) {
            var groupTypes = [];
            groupTypes.SPECIALTY = 1;
            groupTypes.UNIT = 2;
            groupTypes.DISEASE_GROUP = 3;

            if (groupTypes[group.groupType.value]) {
                return groupTypes[group.groupType.value];
            }
        }
        return 0;
    };

    // global function to order group roles by group type
    $rootScope.orderGroupRoles = function (groupRole) {
        if (groupRole.group.groupType) {
            var groupTypes = [];
            groupTypes.SPECIALTY = 1;
            groupTypes.UNIT = 2;
            groupTypes.DISEASE_GROUP = 3;

            if (groupTypes[groupRole.group.groupType.value]) {
                return groupTypes[groupRole.group.groupType.value];
            }
        }
        return 0;
    };

    $rootScope.$on('$locationChangeStart', function() {
        buildRoute();
    });

    $rootScope.$on('$routeChangeSuccess', function(event, currentRoute) {
        $rootScope.title = currentRoute.title;
    });

    $rootScope.$on('$viewContentLoaded', function(){
        $rootScope.setUnreadConversationCount();
        $rootScope.setSubmittedJoinRequestCount();
    });

    $rootScope.logout = function() {
        $timeout(function() {
            delete $rootScope.routes;
            delete $rootScope.loggedInUser;
            delete $rootScope.authToken;
            delete $rootScope.previousAuthToken;
            delete $rootScope.previousLoggedInUser;
            localStorageService.clearAll();
            $location.path('/');
        });
    };

    $rootScope.switchUserBack = function() {
        AuthService.switchUser($rootScope.previousLoggedInUser.id, $rootScope.previousAuthToken).then(
        function(authToken) {

            delete $rootScope.previousLoggedInUser;
            localStorageService.remove('previousLoggedInUser');

            $rootScope.authToken = authToken;
            localStorageService.set('authToken', authToken);

            // get user information, store in session
            AuthService.getUserInformation(authToken).then(function (userInformation) {

                var user = userInformation.user;
                delete userInformation.user;
                user.userInformation = userInformation;

                $rootScope.loggedInUser = user;
                localStorageService.set('loggedInUser', user);

                $rootScope.routes = userInformation.routes;
                localStorageService.set('routes', userInformation.routes);

                $rootScope.setUnreadConversationCount();

                $location.path('/dashboard').search('a');

                delete $rootScope.previousAuthToken;
                localStorageService.remove('previousAuthToken');
            }, function() {
                alert("Error receiving user information");
            });
        }, function() {
            alert("Cannot view patient");
        });
    };

    // get auth token
    var authToken = localStorageService.get('authToken');
    if (authToken !== undefined) {
        $rootScope.authToken = authToken;
    }

    // get previous auth token
    var previousAuthToken = localStorageService.get('previousAuthToken');
    if (previousAuthToken !== undefined) {
        $rootScope.previousAuthToken = previousAuthToken;
    }

    // get previous logged in user
    var previousLoggedInUser = localStorageService.get('previousLoggedInUser');
    if (previousLoggedInUser !== undefined) {
        $rootScope.previousLoggedInUser = previousLoggedInUser;
    }

    // get cookie user
    var loggedInUser = localStorageService.get('loggedInUser');
    if (loggedInUser !== undefined) {
        $rootScope.loggedInUser = loggedInUser;
    }

    // get cookie routes
    var routes = localStorageService.get('routes');
    if (routes !== undefined) {
        $rootScope.routes = routes;
    }

    $rootScope.initialised = true;
    $rootScope.endPoint = ENV.apiEndpoint;

}]);

$('html').click(function(e){
    var target = $(e.target);
    var targetParent = $(e.target).parent();

    if($('.filter-select').hasClass('open')){
        if(!target.hasClass('dropdown-toggle')){
            if (!target.hasClass('a-filter-group') && !targetParent.hasClass('a-filter-group')){
                $('.select-container .btn-group').removeClass('open');
            }
        }
    }

    if(target.hasClass('caret-container') || target.hasClass('caret')){
        var dropdownContainer = target.closest('.filter-select');

        if(!$('.filter-select').hasClass('open')){
            dropdownContainer.addClass('open');
        } else {
            dropdownContainer.removeClass('open');
        }
    }

    var rowElement = target.closest('.faux-row');
    var tableElement = target.closest('.faux-table');

    if(target.hasClass('edit-button')){
        tableElement.find('.faux-row').removeClass('highlight');
        tableElement.find('.item-header').removeClass('open');
        if(!target.hasClass('editing')){
            $('.edit-button').removeClass('editing');
            target.addClass('editing');
//            add class to active row and form
            rowElement.parent().addClass('open');
            rowElement.addClass('highlight');
            tableElement.find('.faux-row').addClass('dull');
        } else{
            $('.edit-button').removeClass('editing');
            tableElement.find('.highlight').removeClass('highlight');
            tableElement.find('.item-header').removeClass('open');
            tableElement.find('.faux-row').removeClass('dull');
        }
    } else if(target.hasClass('close-edit')){
        tableElement.find('.faux-row').removeClass('highlight');
        tableElement.find('.highlight').removeClass('highlight');
        tableElement.find('.item-header').removeClass('open');
        tableElement.find('.faux-row').removeClass('dull');
        tableElement.find('.edit-button').removeClass('editing');
    }
});

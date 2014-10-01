'use strict';

angular.module('patientviewApp').controller('DashboardCtrl', ['UserService','$scope', 'GroupService', 'NewsService',
function (UserService, $scope, GroupService, NewsService) {

    // get graph every time group is changed
    $scope.$watch('graphGroupId', function(newValue) {

        if ($scope.permissions && !$scope.permissions.isPatient) {
            var i;
            $scope.chartLoading = true;

            if (newValue !== undefined) {
                GroupService.getStatistics(newValue).then(function (data) {

                    // now using standard google charts (not angular-google-chart)

                    var chartData = [
                        ['date', 'Patients', 'Unique Logons', 'Logons']
                    ];

                    var minDate = new Date(2999,1,1);

                    for (i = 0; i < data.length; i++) {
                        var row = [];
                        var date = new Date(data[i].endDate);
                        //row[0] = date;
                        row[0] = new Date(date.getFullYear(),date.getMonth()+1,null,null,null,null,null);
                        row[1] = data[i].countOfPatients;
                        row[2] = data[i].countOfUniqueLogons;
                        row[3] = data[i].countOfLogons;
                        chartData.push(row);

                        if (row[0].getTime() < minDate.getTime()) {
                            minDate = row[0];
                        }
                    }

                    // get most recent statistics of user locked and inactive
                    if (data[0]) {
                        $scope.statisticsDate = data[0].endDate;
                        $scope.lockedUsers = data[0].countOfUserLocked;
                        $scope.inactiveUsers = data[0].countOfUserInactive;

                        chartData = new google.visualization.arrayToDataTable(chartData);

                        // set min/max to one month either side of data
                        var minValue = new Date(minDate);
                        var maxValue = new Date($scope.statisticsDate);
                        minValue = new Date(minValue.getTime() - 2592000000);
                        maxValue = new Date(maxValue.getTime() + 2592000000);

                        var options = {
                            'title': null,
                            'isStacked': 'true',
                            'fill': 20,
                            'displayExactValues': true,
                            'vAxis': {
                                'title': null,
                                'pointSize': 5,
                                'gridlines': {
                                    'count': 10,
                                    'color': '#ffffff'
                                },
                                'viewWindow': {
                                    'min': 0
                                }
                            },
                            'hAxis': {
                                'title': null,
                                format: 'MMM-yyyy',
                                minValue: minValue,
                                maxValue: maxValue
                            },
                            'chartArea': {
                                left: '7%',
                                top: '7%',
                                width: '80%',
                                height: '85%'
                            }
                        };

                        $scope.chart.draw(chartData, options);
                    }

                    $scope.chartLoading = false;
                });
            }
        }
    });

    $scope.init = function() {
        $scope.loading = true;

        var chart = new google.visualization.LineChart(document.getElementById('chart_div'));
        $scope.chart = chart;

        $scope.allGroups = [];
        $scope.permissions = {};
        var i;

        $scope.permissions.isPatient = UserService.checkRoleExists('PATIENT', $scope.loggedInUser);

        // set the list of groups to show in the data grid
        $scope.graphGroups = $scope.loggedInUser.userGroups;

        for(i=0;i<$scope.graphGroups.length;i++) {
            $scope.allGroups[$scope.graphGroups[i].id] = $scope.graphGroups[i];
        }

        // set feature (avoid blank option)
        if ($scope.graphGroups && $scope.graphGroups.length > 0) {
            $scope.graphGroupId = $scope.graphGroups[0].id;
        }

        NewsService.getByUser($scope.loggedInUser.id, 0, 5).then(function(page) {
            $scope.newsItems = page.content;
            $scope.loading = false;
        }, function() {
            $scope.loading = false;
        });
    };

    $scope.init();
}]);

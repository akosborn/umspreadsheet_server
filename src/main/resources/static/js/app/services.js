(function (angular) {
    var ShowFactory =  function ($resource) {
        return $resource('/api/shows/:id', null,
            {
                'update': {
                    method: 'PUT'
                }
            });
    };

    ShowFactory.$inject = ['$resource'];
    angular.module('UMspreadsheet.services').factory("Show", ShowFactory);


    var TrackFactory = function ($resource) {
        return $resource('/api/tracks/:id', null,
            {
            'update': {
                method: 'PUT'
            },
            'remove': {
                method: 'DELETE'
            }
        });
    };

    TrackFactory.$inject = ['$resource'];
    angular.module('UMspreadsheet.services').factory("Track", TrackFactory);

    var SetFactory = function ($resource) {
        return $resource('/api/sets/:id', null,
            {
            'update': {
                method: 'PUT'
            },
            'remove': {
                method: 'DELETE'
            }
        });
    };

    SetFactory.$inject = ['$resource'];
    angular.module('UMspreadsheet.services').factory("Set", SetFactory);

    // var TrackReviewFactory = function ($resource) {
    //     return $resource('/api/track-reviews/:id', null,
    //         {
    //         'update': {
    //             method: 'PUT'
    //         },
    //         'remove': {
    //             method: 'DELETE'
    //         }
    //     });
    // };

    var TrackReviewFactory = function ($resource) {
        return $resource('/api/track-reviews/:id', null,
            {
                'update': {
                    method: 'PUT'
                },
                'remove': {
                    method: 'DELETE'
                }
            });


            // Standard: $resource('/api/track-reviews/:id', null,
            //     {
            //         'update': {
            //             method: 'PUT'
            //         },
            //         'remove': {
            //             method: 'DELETE'
            //         }
            //     }),
            // ByUser: $resource('/api/track-reviews-by-user-track/:id', null)
    };

    TrackReviewFactory.$inject = ['$resource'];
    angular.module('UMspreadsheet.services').factory("TrackReview", TrackReviewFactory);


}(angular));
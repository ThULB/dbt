"use strict";

var appName = "MailQueue";
var app = angular.module(appName, [ "pascalprecht.translate", "angularModalService" ]);

app.formatI18N = function(str, args) {
	args = Array.prototype.slice.call(arguments, 1);
	var formatted = str;
	if (formatted !== undefined) {
		for (var i = 0; i < args.length; i++) {
			formatted = formatted.replace(RegExp("\\{" + i + "\\}", "g"), args[i]);
		}
	}
	return formatted;
};

app.joinURI = function(parameters) {
	var uriParts = [];
	for ( var i in parameters) {
		if ({}.hasOwnProperty.call(parameters, i)) {
			var param = parameters[i];
			if (param.name === "uri") {
				uriParts.push(param.value);
			} else if (param.name.indexOf("uri_") === "uri") {
				var ind = param.name.split("uri_")[1];
				uriParts[ind] = param.value;
			}
		}
	}
	return uriParts.join();
};

app.config(function($translateProvider, $translatePartialLoaderProvider) {
	$translatePartialLoaderProvider.addPart("alert.*");
	$translatePartialLoaderProvider.addPart("button.*");
	$translatePartialLoaderProvider.addPart("dataTable.*");
	$translatePartialLoaderProvider.addPart("component.rc.mailqueue.*");
	$translatePartialLoaderProvider.addPart("mir.error.*");

	$translateProvider.useLoader("$translatePartialLoader", {
		urlTemplate : webApplicationBaseURL + "rsc/locale/translate/{lang}/{part}"
	});

	$translateProvider.preferredLanguage(currentLang || "de");
	$translateProvider.fallbackLanguage("en");
});

app.controller("alertCtrl", function($rootScope, $scope, $sce, $translate) {
	$scope.alertObj = {};

	$rootScope.$on("alertEvent", function(event, type, obj) {
		if (obj === null) {
			return;
		}

		$scope.alertObj.type = type;
		$scope.alertObj.show = true;
		if (typeof obj === "string") {
			$scope.alertObj.headline = $translate.instant("alert.type." + type);
			$scope.alertObj.message = obj;
		} else {
			if (obj.status) {
				$scope.alertObj.headline = $translate.instant("mir.error.headline." + obj.status);
				$scope.alertObj.message = $sce.trustAsHtml(app.formatI18N($translate.instant("mir.error.codes." + obj.status), obj.config.url.replace(
						webApplicationBaseURL, "/")));
			} else {
				$scope.alertObj.headline = obj.localizedHeadline ? $translate.instant(obj.localizedHeadline) : undefined ||
						$translate.instant("alert.type." + type);
				$scope.alertObj.message = $sce.trustAsHtml(obj.localizedMessage ? $translate.instant(obj.localizedMessage) : undefined || obj.message);
				$scope.alertObj.stackTrace = obj.stackTrace;
			}
		}
	});

	$scope.clear = function() {
		$scope.alertObj.show = false;
	};
});

app.controller("queueCtrl", function($rootScope, $scope, $translate, $log, $http, ModalService) {
	$scope.Math = window.Math;
	$scope.jobs = {
		loading : true
	};

	$scope.sort = {
		by : [ "id" ],
		reverse : true
	};

	$scope.load = function() {
		$scope.jobs.loading = true;
		$http.get(webApplicationBaseURL + "rsc/jobqueue/de.urmel_dl.dbt.common.MailJob").then(function(result) {
			if (result.status === 200) {
				$scope.jobs = result.data;
				$scope.jobs.total = $scope.jobs.job.length;
				$scope.jobs.limit = $scope.jobs.limit || 50;
				$scope.jobs.start = 0;
			}
			$scope.jobs.loading = false;
		}, function(error) {
			$rootScope.$emit("alertEvent", "error", error);
			$log.error(error);
			$scope.jobs.loading = false;
		});
	};

	$scope.isSort = function(args) {
		args = Array.prototype.slice.call(arguments);
		return angular.toJson($scope.sort.by) === angular.toJson(args);
	};

	$scope.setSort = function(args) {
		args = Array.prototype.slice.call(arguments);
		$scope.sort.reverse = angular.toJson($scope.sort.by) === angular.toJson(args) ? !$scope.sort.reverse : false;
		$scope.sort.by = args;
	};

	$scope.sortBy = function(job) {
		function objValues(obj) {
			var values = [];
			for ( var i in obj) {
				if ({}.hasOwnProperty.call(obj, i)) {
					values.push(obj[i]);
				}
			}
			return values;
		}

		var obj = job;
		var by = $scope.sort.by;
		var parsed = 0;

		if (by !== undefined) {
			for ( var i in by) {
				if (obj[by[i]] !== undefined) {
					obj = obj[by[i]];
					parsed++;
				}
			}

			if (parsed < by.length) {
				for ( var c in obj) {
					if (objValues(obj[c]).indexOf(by[parsed]) !== -1) {
						obj = obj[c].value || obj[c];
						break;
					}
				}
			}
		}

		return obj;
	};

	$scope.pagination = function(pagination) {
		var maxPages = 5;
		var total = Math.floor(pagination.total / pagination.limit) + (pagination.total % pagination.limit > 0 ? 1 : 0);
		var page = pagination.start / pagination.limit + 1;
		if (total > 1) {
			var pS = (page - (maxPages - 1) / 2) - 1;
			if (pS < 0) {
				pS = 0;
			}
			var pE = pS + maxPages;
			if (pE >= total) {
				pE = total;
				pS = pE - (maxPages > total ? total : maxPages);
			}
			pagination = [];
			for (var p = pS; p < pE; p++) {
				pagination.push(p + 1);
			}
			return pagination;
		}
		return [];
	};

	$scope.paginationPage = function(pagination, page) {
		if ($scope.paginationDisabled(pagination, page)) {
			return;
		}

		if (typeof page === "string") {
			page = page[0] === "-" ? (pagination.start / pagination.limit) + 1 - parseInt(page.substring(1))
					: page[0] === "+" ? page = (pagination.start / pagination.limit) + 1 + parseInt(page.substring(1)) : 0;
		}
		var start = (page - 1) * pagination.limit;
		pagination.start = start;
	};

	$scope.paginationActive = function(pagination, page) {
		return pagination.start === (page - 1) * pagination.limit;
	};

	$scope.paginationDisabled = function(pagination, page) {
		if (typeof page === "string") {
			page = page[0] === "-" ? (pagination.start / pagination.limit) + 1 - parseInt(page.substring(1))
					: page[0] === "+" ? page = (pagination.start / pagination.limit) + 1 + parseInt(page.substring(1)) : 0;
		}
		var total = Math.floor(pagination.total / pagination.limit) + (pagination.total % pagination.limit > 0 ? 1 : 0);
		return page <= 0 || page > total;
	};

	$scope.date = function(job, type) {
		for ( var i in job.date) {
			if (job.date[i].type === type) {
				return job.date[i].value;
			}
		}

		return null;
	};

	$scope.slotId = function(job) {
		var regex = new RegExp("slotId=([0-9\.]+)");
		var uri = app.joinURI(job.parameter);
		var m = uri.match(regex);
		return m[1];
	};

	$scope.showMailDialog = function(job) {
		ModalService.showModal({
			templateUrl : webApplicationBaseURL + "dbt/assets/templates/mail-dialog.html",
			controller : "mailDialogCtrl",
			inputs : {
				parameters : angular.copy(job.parameter),
			}
		}).then(function(modal) {
			modal.element.modal();
			modal.close.then(function(p) {
				if (p !== undefined) {
					$scope.updatePackage(p);
				}
			});
		});
	};

	$scope.load();
});

app.controller("mailDialogCtrl", function($scope, $http, $log, $sce, parameters, close) {
	$scope.parameters = parameters;
	$scope.mail = {
		loading : true
	};

	$scope.close = function(result) {
		close(result, 500);
	};

	$scope.load = function() {
		$scope.mail.loading = true;

		$http.post(webApplicationBaseURL + "rsc/rcmail", app.joinURI($scope.parameters), {
			headers : {
				"Content-Type" : undefined
			},
			transformResponse : [ function(data) {
				return $scope.parseData(data);
			} ]
		}).then(function(result) {
			if (result.status === 200) {
				$scope.mail = result.data;
			}
			$scope.mail.loading = false;
		}, function(e) {
			$rootScope.$emit("alertEvent", "error", e.data);
			$log.error(e);
			$scope.mail.loading = false;
		});
	};

	$scope.parseData = function(data) {
		var $xml = $($.parseXML(data));
		var $mail = $xml.find("email");
		var mail = {
			from : $mail.find("from").text(),
			to : $mail.find("to").text(),
			subject : $mail.find("subject").text(),
			body : {
				plain : $mail.find("body[type!=html]").text(),
				html : $sce.trustAsHtml($mail.find("body[type=html]").text())
			}
		};
		return mail;
	};

	$scope.load();
});
$(document).ready(
		function() {
			var videoChooserElement = $("#videoChooser");

			$(".mir-player video, .mir-player audio").ready(function() {

				var videoOptions = videoChooserElement.find("option");
				if (videoOptions.length === 1) {
					videoChooserElement.hide();
				} else {
					videoOptions.filter("[data-is-main-doc=true]").first().prop("selected", true);
				}

				videoChooserElement.change();
			});

			// get all sources of selected item in a var and give it to player
			var hidePlayer = function(player) {
				if (typeof player !== "undefined") {
					player.hide();
					player.pause();
				}
			};

			var sourceCache = {};

			var getVideo = function(currentOption, callback) {
				var src = currentOption.attr("data-src");
				var mimeType = currentOption.attr("data-mime-type");
				var sourceArr = [];
				var lookupKey = currentOption.parent().index() + "_" + currentOption.index();

				if (lookupKey in sourceCache) {
					callback(sourceCache[lookupKey]);
					return;
				}

				if (typeof src === "undefined" || typeof mimeType === "undefined") {
					var sourcesUrl = currentOption.attr("data-sources-url");
					if (typeof sourcesUrl === "undefined") {
						callback([]);
						return;
					}

					$.getJSON(sourcesUrl).done(function(sources) {
						if (sources.source) {
							$.each(sources.source, function(i, src) {
								if (src.type === "video/mp4") {
									src.src = webApplicationBaseURL + "rsc/media/progressiv/" + sources.id + "/" + src.src;
								}
							});
							sourceCache[lookupKey] = sources.source;
							callback(sourceCache[lookupKey]);
						}
					});
				} else {
					sourceArr.push({
						type : mimeType.trim(),
						src : src.trim()
					});
					sourceCache[lookupKey] = sourceArr;
					callback(sourceCache[lookupKey]);
				}
			};

			var thumbCache = {};

			var getThumbs = function(currentOption, callback) {
				var id = currentOption.attr("data-source-id");
				var sourceArr = [];
				var lookupKey = currentOption.parent().index() + "_" + currentOption.index();

				if (lookupKey in thumbCache) {
					callback(thumbCache[lookupKey]);
					return;
				}

				if (typeof id !== "undefined") {
					$.getJSON(webApplicationBaseURL + "rsc/media/thumbs/" + id).done(function(sources) {
						if (sources.source) {
							$.each(sources.source, function(i, src) {
								src.src = webApplicationBaseURL + "rsc/media/thumb/" + id + "/" + src.src;
							});
							thumbCache[lookupKey] = sources.source;
							callback(thumbCache[lookupKey]);
						}
					});
				} else {
					thumbCache[lookupKey] = sourceArr;
					callback(thumbCache[lookupKey]);
				}
			};

			var evtPlay = function() {
				var fnUrl = $(this).data("fileNodeUrl");
				if (fnUrl) {
					$.get(fnUrl, function(data) {
					});
				}
			};

			videoChooserElement.change(function() {
				// reuse player
				var myPlayerVideo, myPlayerAudio;
				var selectElement = $(this);
				var currentOption = selectElement.find(":selected");

				if ($(".mir-player video").length > 0) {
					myPlayerVideo = selectElement.data("playerVideo");
					if (!myPlayerVideo) {
						myPlayerVideo = videojs($(".mir-player video").attr("id"));
						selectElement.data("playerVideo", myPlayerVideo);
					}
				}

				if ($(".mir-player audio").length > 0) {
					myPlayerAudio = selectElement.data("playerAudio");
					if (!myPlayerAudio) {
						myPlayerAudio = videojs($(".mir-player audio").attr("id"));
						selectElement.data("playerAudio", myPlayerAudio);
					}
				}

				getVideo(currentOption, function(sourceArr) {
					sourceArr = sourceArr.sort(function(a, b) {
						if (a.type > b.type) {
							return 1;
						}
						if (a.type < b.type) {
							return -1;
						}
						return 0;
					});

					var playerToHide, playerToShow;
					var isAudio = currentOption.attr("data-audio") === "true";
					var htmlEmbed = jQuery(".mir-player");

					if (isAudio) {
						playerToHide = myPlayerVideo;
						playerToShow = myPlayerAudio;
					} else {
						getThumbs(currentOption, function(sourceArr) {
							myPlayerVideo.poster(sourceArr[0].src);
						});
						playerToShow = myPlayerVideo;
						playerToHide = myPlayerAudio;
					}

					hidePlayer(playerToHide);

					$(playerToShow)
							.data(
									"fileNodeUrl",
									webApplicationBaseURL + "rsc/stat/" + currentOption.parent().attr("label") + "/" + currentOption.text() +
											".css");

					playerToShow.off("play", evtPlay);
					playerToShow.show();
					playerToShow.on("play", evtPlay);
					playerToShow.src(sourceArr);
				});
			});
		});
$(document).ready(function () {
  var sourceCache = {};

  var getVideo = function (player, callback) {
    var sourceArr = [];
    var lookupKey = player.attr("id");

    if (lookupKey in sourceCache) {
      callback(sourceCache[lookupKey]);
      return;
    }

    if (typeof src === "undefined" || typeof mimeType === "undefined") {
      var sourcesUrl = player.attr("data-sources-url");
      if (typeof sourcesUrl === "undefined") {
        callback([]);
        return;
      }

      $.getJSON(sourcesUrl).done(function (sources) {
        if (sources.source) {
          $.each(sources.source, function (_i, src) {
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
        type: mimeType.trim(),
        src: src.trim()
      });
      sourceCache[lookupKey] = sourceArr;
      callback(sourceCache[lookupKey]);
    }
  };

  var thumbCache = {};

  var getThumbs = function (player, callback) {
    var id = player.attr("data-source-id");
    var sourceArr = [];
    var lookupKey = player.attr("id");

    if (lookupKey in thumbCache) {
      callback(thumbCache[lookupKey]);
      return;
    }

    if (typeof id !== "undefined") {
      $.getJSON(webApplicationBaseURL + "rsc/media/thumbs/" + id).done(function (sources) {
        if (sources.source) {
          $.each(sources.source, function (_i, src) {
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

  $(".entry-file video").each(function () {
    var $player = $(this);

    var player = $player.data("player");
    if (!player) {
      player = videojs($player.attr("id"));
      $player.data("player", player);
    }

    $player.ready(function () {
      getVideo($player, function (sourceArr) {
        sourceArr = sourceArr.sort(function (a, b) {
          if (a.type.toLowerCase() === "application/x-mpegurl") {
            return -1;
          } else if (b.type.toLowerCase() === "application/x-mpegurl") {
            return 1;
          }

          if (a.type > b.type) {
            return 1;
          }
          if (a.type < b.type) {
            return -1;
          }
          return 0;
        });

        getThumbs($player, function (sourceArr) {
          if (sourceArr && sourceArr.length > 0) {
            player.poster(sourceArr[0].src);
          }
        });

        player.src(sourceArr);
      });
    });
  });
});
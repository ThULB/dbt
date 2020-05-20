$(document).ready(function () {
  var videoChooserElement = $("#videoChooser");

  $(".mir-player video").ready(function () {
    var videoOptions = videoChooserElement.find("option");
    if (videoOptions.length === 1) {
      videoChooserElement.closest(".card-header").hide() || videoChooserElement.hide();
    } else {
      videoOptions.filter("[data-is-main-doc=true]").first().prop("selected", true);
    }

    videoChooserElement.change();
  });

  // get all sources of selected item in a var and give it to player
  var hidePlayer = function (player) {
    if (typeof player !== "undefined") {
      player.hide();
      player.pause();
    }
  };

  var evtPlay = function () {
    var fnUrl = $(this).data("fileNodeUrl");
    if (fnUrl) {
      $.get(fnUrl, function (_data) { });
    }
  };

  videoChooserElement.change(function () {
    // reuse player
    var myPlayerVideo, myPlayerAudio;
    var mPlayer;
    var selectElement = $(this);
    var currentOption = selectElement.find(":selected");

    if ($(".mir-player video").length > 0) {
      myPlayerVideo = selectElement.data("playerVideo");
      mPlayer = selectElement.data("mediaPlayer");

      if (!mPlayer) {
        var id = myPlayerVideo.id();
        if (myPlayerVideo) {
          var parent = $("#" + id).parent();
          var cls = $("#" + id).attr("class").split(/\s+/);

          myPlayerVideo.reset();
          myPlayerVideo.dispose();

          var newPlayer = $(document.createElement("video"));
          newPlayer.attr("id", id);
          newPlayer.attr("controls", "");
          newPlayer.attr("poster", "");
          newPlayer.attr("preload", "metadata");

          for (var i = 0; i < cls.length; i++) {
            if (cls[i].indexOf("vjs-") !== 0) {
              newPlayer.addClass(cls[i]);
            }
          }
          parent.append(newPlayer);

          myPlayerVideo = undefined;
        }

        mPlayer = mediaPlayer(webApplicationBaseURL, id, {});
      }
      if (!myPlayerVideo) {
        myPlayerVideo = mPlayer.player;
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

    var playerToHide, playerToShow;
    var isAudio = currentOption.attr("data-audio") === "true";

    if (isAudio) {
      playerToHide = myPlayerVideo;
      playerToShow = myPlayerAudio;
    } else {
      playerToShow = myPlayerVideo;
      playerToHide = myPlayerAudio;
      mPlayer.changeOptions({ id: currentOption.data("source-id") });
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
  });
});
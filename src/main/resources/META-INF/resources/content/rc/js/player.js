$(document).ready(function () {
  $("video[data-source-id]").each(function () {
    var playerId = $(this).attr("id");
    var sourceId = $(this).data("source-id");

    player = mediaPlayer(webappBaseURL, playerId, { id: sourceId });

    var $player = $(playerId);
    $player.data("player", player);
    $player.data("share-modal", player.buildShareModal());
  });
});
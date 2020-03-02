var makeTocSticky = function () {
  var $elm = $(".slot-toc");

  if ($elm && window.innerWidth >= 1200) {
    var container = document.getElementById("container-main");
    var tocOffset = container.offsetTop + 10;

    if ($elm.height() < (window.innerHeight - tocOffset)) {
      $elm.addClass("sticky-top");
      $elm.css("top", tocOffset + "px");
    } else {
      $elm.removeClass("sticky-top");
      $elm.css("top");
    }
  }
};

/**
 * The Main on DOM ready.
 */
$(document).ready(function () {
  makeTocSticky();

  window.addEventListener("orientationchange", makeTocSticky());
  window.addEventListener("resize", makeTocSticky());
});
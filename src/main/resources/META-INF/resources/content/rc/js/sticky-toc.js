var makeTocSticky = function () {
  var $elm = $(".slot-toc");
  if ($elm) {
    var container = document.getElementById("container-main");
    var tocOffset = container.offsetTop + 10;

    if ($elm[0].offsetHeight < (window.innerHeight - tocOffset)) {
      console.log($elm[0].offsetHeight, (window.innerHeight - tocOffset));
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
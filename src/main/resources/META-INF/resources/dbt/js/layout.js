/**
 * Detect vendor prefix.
 */
var prefix = function() {
	var styles = window.getComputedStyle(document.documentElement, ""), pre = (Array.prototype.slice.call(styles).join("").match(/-(moz|webkit|ms)-/) || (styles.OLink === "" && [
			"", "o" ]))[1], dom = ("WebKit|Moz|MS|O").match(new RegExp("(" + pre + ")", "i"))[1];
	return {
		dom : dom,
		lowercase : pre,
		css : "-" + pre + "-",
		js : pre[0].toUpperCase() + pre.substr(1)
	};
};

/**
 * Builds a SVG image mask for browser doesn't support -webkit-mask.
 */
var buildSVGImageMask = function(icon, styleClass) {
	var rnd = Math.round(Math.random() * 1E8);
	return "<svg width=\"100%\" height=\"100%\" class=\"" + styleClass + "\"><defs><mask id=\"mask" + rnd +
			"\" maskUnits=\"userSpaceOnUse\" maskContentUnits=\"userSpaceOnUse\"><image width=\"100%\" height=\"100%\" xlink:href=\"" + icon +
			"\"></image></mask></defs><foreignObject width=\"100%\" height=\"100%\" style=\"mask:url(#mask" + rnd + ");\"><div></div></foreignObject></svg>";
};

/**
 * Inits SVG images masks.
 */
var initSVGImageMasks = function() {
	$("img.hit_icon_overlay[src$='.svg']").each(function() {
		var src = $(this).attr("src");
		var icon = $(document.createElement("span"));
		if (window.SVGForeignObjectElement) {
			icon = $(buildSVGImageMask(src, $(this).attr("class")));
		} else if ("webkit" === $prefix.lowercase) {
			icon.css({
				maskImage : "url(" + src + ")",
			});
		} else {
			return;
		}
		$(this).parent().append(icon);
	});
};

var updateOverlayScroller = function() {
	$(".scroll-container").each(function() {
		$this = $(this);
		var $fadeLeft = $(".fade-left", this);
		var $fadeRight = $(".fade-right", this);
		$container = $(".scrolling-container", this);

		var outerWidth = 0;
		$container.children().each(function() {
			outerWidth += $(this).outerWidth(true);
		});

		if (outerWidth > $this.innerWidth()) {
			$fadeRight.addClass("scrolling");

			if (!$container.hasClass("is-init")) {
				$container.on("scroll", function() {
					if ($(this).scrollLeft() > 0) {
						$fadeLeft.addClass("scrolling");
						if ((outerWidth - $(this).scrollLeft()) <= $container.innerWidth()) {
							$fadeRight.removeClass("scrolling");
						}
					} else {
						$fadeLeft.removeClass("scrolling");
						$fadeRight.addClass("scrolling");
					}
				});
				$container.addClass("is-init");
			}
		} else {
			$fadeLeft.removeClass("scrolling");
			$fadeRight.removeClass("scrolling");
		}
	});
};

var initOverlayScroller = function() {
	$("*").each(
			function() {
				var $this = $(this);
				if (!$this.hasClass("table-responsive") && ($this.css("overflow-y") === "hidden") && ($this.css("overflow-x") === "auto")) {
					var $fadeLeft = $(document.createElement("div")).html("<i aria-hidden=\"true\" data-hidden=\"true\" class=\"fa fa-angle-left\"></i>")
							.addClass("fade-left");
					var $fadeRight = $(document.createElement("div")).html("<i aria-hidden=\"true\" data-hidden=\"true\" class=\"fa fa-angle-right\"></i>")
							.addClass("fade-right");
					var $container = $(document.createElement("div")).addClass("scroll-container");

					var $parent = $this.parent();

					$container.append($fadeLeft);
					$container.append($fadeRight);
					$container.append($this.detach());
					$parent.append($container);

					$container.height($this.height());
					$.each([ $fadeLeft, $fadeRight ], function() {
						$(this).children().css("top", Math.floor(($container.innerHeight() - $(this).children().height()) / 2));
					});

					$this.addClass("scrolling-container");

					var outerWidth = 0;
					$this.children().each(function() {
						outerWidth += $(this).outerWidth(true);
					});

					if (outerWidth > $container.innerWidth()) {
						$fadeRight.addClass("scrolling");

						if (!$this.hasClass("is-init")) {
							$this.on("scroll", function() {
								if ($(this).scrollLeft() > 0) {
									$fadeLeft.addClass("scrolling");
									if ((outerWidth - $(this).scrollLeft()) <= $container.innerWidth()) {
										$fadeRight.removeClass("scrolling");
									}
								} else {
									$fadeLeft.removeClass("scrolling");
									$fadeRight.addClass("scrolling");
								}
							});
							$this.addClass("is-init");
						}
					}
				}
			});

	window.addEventListener("resize", updateOverlayScroller);
	window.addEventListener("orientationchange", updateOverlayScroller);
};

/**
 * The Main on DOM ready.
 */
$(document).ready(function() {
	$prefix = prefix();

	$("html").removeClass("no-js");

	// workaround to remove empty querystring param
	$("form[role='search']").submit(function() {
		$("input").each(function(i, elem) {
			if ($(elem).prop("value").length === 0) {
				$(elem).prop("disabled", "disabled");
			}
		});
	});

	initSVGImageMasks();
	initOverlayScroller();
});
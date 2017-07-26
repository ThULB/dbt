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
});
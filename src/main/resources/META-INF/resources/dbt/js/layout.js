if (typeof NamespaceManager == 'undefined')
	var NamespaceManager = {};

NamespaceManager = {
	Register : function(namespace) {
		namespace = namespace.split('.');

		if (!window[namespace[0]])
			window[namespace[0]] = {};

		var strFullNamespace = namespace[0];
		for (var i = 1; i < namespace.length; i++) {
			strFullNamespace += "." + namespace[i];
			eval("if(!window." + strFullNamespace + ")window." + strFullNamespace + "={};");
		}
	}
};

NamespaceManager.Register("utils");
utils.JS = function() {
};
utils.JS.prototype.constructor = utils.JS;

utils.JS.isValid = function(a) {
	return void 0 !== a && null !== a && "undefined" !== typeof a;
};

utils.JS.toBoolean = function(aObj) {
	return (typeof aObj == "string" && aObj == "true") || aObj == true ? true : false;
};

NamespaceManager.Register("components");
components.ModalDialogComponent = function() {
	var that = this;

	this.showDialog = function(aEvent) {
		aEvent.preventDefault();

		var href = jQuery(aEvent.delegateTarget).attr("href");
		var dialogId = "dlg_" + Math.random().toString(36).substring(7);
		jQuery.ajax({
			url : href + (href.indexOf("?") != -1 ? "&" : "?") + "XSL.Style=modalDialog&XSL.dialogId=" + dialogId,
			error : function(aXHR, aErrorCode, aErrorText) {
				console.error(aErrorCode, aErrorText);
				window.location = href;
			},
			success : function(aData, aStatus, aqXHR) {
				if (utils.JS.isValid(aData)) {
					var dialog = jQuery.parseHTML(aData);
					var dialogId = jQuery(dialog).attr("id");

					if (utils.JS.isValid(dialogId) && (typeof jQuery.modal !== "function")) {
						jQuery("body").append(dialog);

						jQuery("#" + dialogId).on('hidden.bs.modal', function() {
							jQuery(this).off();
							jQuery(this).remove();
						}).modal("show");

						return;
					}
				}

				window.location = href;
			}
		});
	};

	(function() {
		jQuery("*[data-action='modalDialog']").each(function() {
			jQuery(this).on("click", that.showDialog);
		});
	})();
};

NamespaceManager.Register("components");
components.textCollapseComponent = function() {
	var _maxLinesCollapsed = 5;
	var _cmpClass = "text-collapse";
	var _cmpOverlayClass = "collapseOverlay";

	(function() {
		jQuery("." + _cmpClass).each(function() {
			var lineHeight = Math.floor(parseFloat(jQuery(this).css("line-height")));
			var exHeight = Math.floor(parseFloat(jQuery(this).css("height")));

			if (exHeight > _maxLinesCollapsed * lineHeight) {
				jQuery(this).css({
					height : _maxLinesCollapsed * lineHeight,
					cursor : "s-resize"
				});

				jQuery(this).on("click", function() {
					if ($.selection().length != 0)
						return;

					if (jQuery(this).hasClass("in")) {
						jQuery(this).animate({
							height : _maxLinesCollapsed * lineHeight
						});
						jQuery(this).toggleClass("in").css({
							cursor : "s-resize"
						});
						jQuery(this).find("div." + _cmpOverlayClass).fadeIn(400, function() {
							jQuery(this).show()
						});
					} else {
						jQuery(this).toggleClass("in").css({
							cursor : "text"
						});
						jQuery(this).animate({
							height : exHeight
						});
						jQuery(this).find("div." + _cmpOverlayClass).fadeOut(400, function() {
							jQuery(this).hide()
						});
					}
				});

				var overlay = jQuery(document.createElement("div"));
				overlay.addClass(_cmpOverlayClass);
				overlay.css({
					height : lineHeight * 1.5
				});

				jQuery(this).append(overlay);
			}
		});
	})();
};

/*
 * DROPDOWN ANIMATION
 * 
 * Description: Set animation style on clicked dropdown menu.
 */
NamespaceManager.Register("components");
components.dropDownAnimationComponent = function() {
	(function() {
		jQuery(".dropdown-toggle").click(function(aEvent) {
			var ddm = jQuery(".dropdown-menu", jQuery(this).parent());
			if (!ddm.hasClass("flipInX")) {
				ddm.addClass("animated fast flipInX");
			} else {
				ddm.removeClass("animated fast flipInX");
			}
			aEvent.preventDefault();
		});
	})();
};

jQuery(document).ready(function() {
	// init layout helpers
	new components.ModalDialogComponent();

	new components.textCollapseComponent();

	// new components.dropDownAnimationComponent();

	/*
	 * TOOLTIP
	 * 
	 * Description: Initialize all Bootstrap Tooltips.
	 */
	$('*[data-toggle="tooltip"]').tooltip();
});
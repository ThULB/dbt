(function(document, history, location) {
	var HISTORY_SUPPORT = !!(history && history.pushState);

	var anchorScrolls = {
		ANCHOR_REGEX : /^#[^ ]+$/,
		OFFSET_HEIGHT_PX : 100,
		OFFSET_SPACER_PX : 25,

		/**
		 * Establish events, and fix initial scroll position if a hash is
		 * provided.
		 */
		init : function() {
			this.scrollToCurrent();
			$(window).on("hashchange", $.proxy(this, "scrollToCurrent"));
			$("body").on("click", "a", $.proxy(this, "delegateAnchors"));
		},

		/**
		 * Return the offset amount to deduct from the normal scroll position.
		 * Modify as appropriate to allow for dynamic calculations
		 */
		getFixedOffset : function() {
			return $("navbar-brand").height() || this.OFFSET_HEIGHT_PX;
		},

		/**
		 * If the provided href is an anchor which resolves to an element on the
		 * page, scroll to it.
		 * 
		 * @param {String}
		 *            href
		 * @return {Boolean} - Was the href an anchor.
		 */
		scrollIfAnchor : function(href, pushToHistory, el) {
			var match, anchorOffset;

			if (!this.ANCHOR_REGEX.test(href) || el && el.getAttribute("data-toggle")) {
				return false;
			}

			match = document.querySelector("#" + href.slice(1)) || document.querySelector("*[name=\"" + href.slice(1) + "\"]");

			if (match) {
				anchorOffset = $(match).offset().top - this.getFixedOffset() - this.OFFSET_SPACER_PX;
				$("html, body").animate({
					scrollTop : anchorOffset
				});

				// Add the state to history as-per normal anchor links
				if (HISTORY_SUPPORT && pushToHistory) {
					history.pushState({}, document.title, location.pathname + href);
				}
			}

			return !!match;
		},

		/**
		 * Attempt to scroll to the current location"s hash.
		 */
		scrollToCurrent : function(e) {
			if (this.scrollIfAnchor(window.location.hash) && e) {
				e.preventDefault();
			}
		},

		/**
		 * If the click event"s target was an anchor, fix the scroll position.
		 */
		delegateAnchors : function(e) {
			var elem = e.target;

			if (this.scrollIfAnchor(elem.getAttribute("href"), true, elem)) {
				e.preventDefault();
			}
		}
	};

	$(document).ready($.proxy(anchorScrolls, "init"));
})(window.document, window.history, window.location);

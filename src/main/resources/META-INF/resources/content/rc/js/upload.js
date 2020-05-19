$(document).ready(function () {
  var loadI18N = function (callback) {
    var lang = $("html").attr("lang");
    $.ajax(webApplicationBaseURL + "rsc/locale/translate/" + lang + "/component.rc.slot.entry.file.errorcode.*").done(function (i18n) {
      callback(i18n);
    });
  };

  var changeButtonState = function (disabled) {
    $("button[name='upload']").prop("disabled", disabled);
  };

  var removeAlert = function (disabled) {
    var alert = $("#error-message");
    if (alert[0]) {
      alert.detach();
    }
    changeButtonState(disabled);
  };

  changeButtonState(true);

  var fi = $("#file").fileinput({
    theme: "fas",
    showUpload: false,
    showAjaxErrorDetails: false,
    hideThumbnailContent: true,
    uploadUrl: servletsBaseURL + "/servlets/RCUploadServlet"
  });

  fi.on("fileclear", function () { removeAlert(true); });
  fi.on("fileselect", function () { removeAlert(false); });

  fi.on("fileuploaded", function (_event, data) {
    var response = data.response && data.response.response;
    if (response && response.redirectUrl) {
      window.location.replace(response.redirectUrl);
    }
  });

  fi.on("fileuploaderror", function (_event, data, _msg) {
    var response = data.jqXHR && data.jqXHR.responseJSON && data.jqXHR.responseJSON.response;
    if (response.errorCode) {
      loadI18N(function (i18n) {
        $.each(i18n, function (key, trans) {
          if (key.lastIndexOf(response.errorCode) !== -1) {
            $(".card-body").prepend("<div id=\"error-message\" class=\"alert alert-danger\">" + trans + "</div>");
          }
        });
      });
    }
  });

  $("button[name='upload']").click(function (evt) {
    var fio = fi.data("fileinput");
    if (fio) {
      evt.preventDefault();
      $(this).prop("disabled", true);

      var form = fi.parents("form");

      var uploadExtraData = {};
      form.find("input, textarea").each(function (_i, input) {
        var $input = $(input);
        var name = $input.attr("name");
        if (name && name !== fi.attr("name")) {
          uploadExtraData[$input.attr("name")] = $input.attr("type") === "checkbox" ? $input.prop("checked") : $input.val();
        }
      });

      fio.uploadAsync = true;
      fio.uploadExtraData = uploadExtraData;
      fio.uploadUrl = fi.parents("form").attr("action");

      fio.upload();
    }
  });
});
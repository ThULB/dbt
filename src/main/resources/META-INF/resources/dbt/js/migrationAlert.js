$(document).ready(function() {
    var url = window.location.href;
    var match = url.match(/\w+_mods_[0-9]+/g);
    if (match != null && match[0] != undefined){
    	match = match [0];
		var idNumber = parseInt(match.replace(/\D/g, ""));
		var migrationId = $("#migrationId").attr("data-migration-id");
		var migrationUrl = $("#migrationUrl").attr("data-migration-url");
		if (migrationId != undefined && migrationUrl != undefined) {
			var migrationNumber = parseInt(migrationId.replace(/\D/g, ""));
    		if (idNumber <= migrationNumber) {
    			$("body").append(`
	      			<div class="alert alert-warning alert-dismissible text-center" role="alert" style="position: fixed; bottom: 0; left: 0; width: 100%; margin-bottom: 0; border-radius: 0;">
	      				<button type="button" class="close" data-dismiss="alert" aria-label="Close"><span aria-hidden="true">&times;</span></button>
	      				Dieses Dokument wurde migriert. Falls Sie Probleme mit der Anzeige des Dokumentes haben, können Sie das Dokument auf dem <a href="`
	      				+ migrationUrl
	      				+ idNumber
	      				+ `" class="alert-link">alten DBT Server</a> ansehen.
	      			</div>
    			`);
    		}
		}
    }
});
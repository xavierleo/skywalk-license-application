(function() {
	'use strict';
	$(document).foundation();
	console.log(window.location.href);
	
	if(window.location.href == "http://"+window.location.host+"/"){
		console.log("http://"+window.location.host);
	    $.ajax({
	        url: 'http://localhost:8080/api/company/exists/',
	        type: 'GET',
	        dataType: 'json',
	        contentType: 'application/json;charset=UTF-8'
	    })
	    .done(function(response){
	        console.log(response);
	         if(response.SUCCESS==true){
	            window.location.href = "initial-setup/create-company.html"
	         }
	    });
	}
})();
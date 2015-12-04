(function() {
	'user strict';
	 //cache dom elements
  	var usernameEL = $("#username");
  	var passwordEL = $("#password");
  	var loginButton = $('#loginButton');
  	var loginForm = $('#login-form');


  	//bind events to elements
  	signInButton.on('click', _signInUser);

  	//event binding functions
  	function _signInUser(){
    
        var key = '{"User" :';
        var userDetails = loginForm.serializeJSON();

        $.ajax({
            url: 'http://localhost:8080/api/user/login'
            method: "POST",
            data:  key+JSON.stringify(userDetails)+'}',
            dataType:"json",
            contentType: "application/json; charset=utf-8",
            success: function(response){
                if(response.SUCCESS){
                    _redirectToDashboard();
                }else{
                    
                }
            },error: function(response){
                
            }
        });
	}
	//private functions
	function _redirectToDashboard(){
	var url = "/dashboard/";
	window.location.href = url;
  	}
 	//public functions
  	return{

  	}
})();
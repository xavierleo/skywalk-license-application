(function() {
	'use strict';
	var registerUserButton = $('#registerUserButton');
    var registerUserForm = $('#registerUserForm');

    registerUserForm.parsley();

    //bind events to elements
    registerUserButton.on('click',_registerNewUser);

    //event binding functions

    function _registerNewUser(){
    
        var key = '{"Company" : {"companyId":"5660a7e2986c768ee331527f"}, "User" :';
        var userDetails = registerUserForm.serializeJSON();
        console.log(userDetails);
        $.ajax({
            url: "http://localhost:8080/api/user",
            
            type: "POST",
            data: key+JSON.stringify(userDetails)+'}',
            dataType:"json",
            contentType: "application/json; charset=utf-8"
        })
            .done(function(response){
            if(response.SUCCESS){
                _redirectToLogin();
            }else{
                alert("Company not created successfully!!!");
            }
        }).fail(function(response){
            alert("Oops something went wrong!!!");
        });
    }
    
})();
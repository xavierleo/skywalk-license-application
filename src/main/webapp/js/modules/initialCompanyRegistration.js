/**
 * Created by xavier on 2015/11/27.
 */
(function() {
  
    'user strict';
    //cache dom elements
    var registerCompanyButton = $('#registerCompanyButton');
    var registerCompanyForm = $('#registerCompanyForm');

    registerCompanyForm.parsley();
    localStorage.setItem("set", "set");

    //bind events to elements
    registerCompanyButton.on('click',_registerNewCompany);

    //event binding functions

    function _registerNewCompany(){

        var key = '{"Company" :';
        var companyDetails = registerCompanyForm.serializeJSON();
        console.log(companyDetails);
        $.ajax({
            url: "http://localhost:8080/api/company",
            type: "POST",
            data: key+JSON.stringify(companyDetails)+'}',
            dataType:"json",
            contentType: "application/json; charset=utf-8"

        })
        .done(function(response){
            alert(response);
            if(response.SUCCESS){
               localStorage.setItem("set2", "set2");
               localStorage.setItem('companyId', response.companyId);
               _redirectToRegisterUser();
            
            }else{
               alert("Company not created successfully!!!");
            }
        })
        .fail(function(response){
            alert("Oops something went wrong!!!");
        });
    }


    //private functions

    function _redirectToRegisterUser(){
        var url = "create-admin.html";
        window.location.href = url;
    }

    //public functions

})();

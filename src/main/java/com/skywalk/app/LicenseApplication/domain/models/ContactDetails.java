package main.java.com.skywalk.app.LicenseApplication.domain.models;


import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Email;

import lombok.Getter;

public class ContactDetails{

    public ContactDetails() {
    }

    	
    public ContactDetails(String email, String mobile) {
		super();
		this.email = email;
		this.mobile = mobile;
	}

    @Email
    @NotNull
	@Getter private String email;

    @Getter private String mobile;
}
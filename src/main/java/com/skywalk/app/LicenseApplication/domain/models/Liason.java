package main.java.com.skywalk.app.LicenseApplication.domain.models;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Email;

import lombok.Getter;

public class Liason{

    public Liason() {
    }

    public Liason(String name, String liasonEmail, String liasonNumber) {
		super();
		this.name = name;
		this.liasonEmail = liasonEmail;
		this.liasonNumber = liasonNumber;
	}


	@NotNull @Getter private String name;

	@NotNull @Email @Getter private String liasonEmail;

	@NotNull @Min(10) @Getter private String liasonNumber;


}
package main.java.com.skywalk.app.LicenseApplication.infrastructure;

import main.java.com.skywalk.app.LicenseApplication.domain.crud.ApplicationCrudService;
import main.java.com.skywalk.app.LicenseApplication.domain.crud.impl.ApplicationCrudServiceImpl;
import main.java.com.skywalk.app.LicenseApplication.domain.models.Application;

public class main {

	public static void main(String[] args) {
		Application a = new Application();
		a.setName("Q-Book");
		a.setShortDescription("Booking System for Service Based Clients");
		a.setLongDescription("Booking System for Service Based Clients Long Description");
		
		Application b = null;
		ApplicationCrudService acs = new ApplicationCrudServiceImpl();
		acs.createEntity(b);
	}

}

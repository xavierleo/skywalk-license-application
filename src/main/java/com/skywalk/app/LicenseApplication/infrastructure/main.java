package main.java.com.skywalk.app.LicenseApplication.infrastructure;

import main.java.com.skywalk.app.LicenseApplication.application.utilities.Link;
import main.java.com.skywalk.app.LicenseApplication.application.utilities.ResponseCodes;

public class main {

	public static void main(String[] args) {
        System.out.println(ResponseCodes.SUCCESS);
        System.out.println(ResponseCodes.ERROR);

        System.out.println(Link.REL);
        System.out.println(Link.HREF);
        System.out.println(Link.METHOD);
        System.out.println(Link.DATATYPE);
	}

}

package main.java.com.skywalk.app.LicenseApplication.application.services;

import javax.json.JsonObject;

/**
 * Created by xavier on 2015/10/28.
 */
public interface CompanyService {

    JsonObject companyExists();

    JsonObject createCompany(JsonObject company);

    JsonObject editCompany(JsonObject company);

    JsonObject viewCompany(String companyId);
}

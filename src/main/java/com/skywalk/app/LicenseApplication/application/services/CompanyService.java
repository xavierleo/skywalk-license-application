package main.java.com.skywalk.app.LicenseApplication.application.services;

import org.bson.types.ObjectId;

import javax.json.JsonObject;

/**
 * Created by xavier on 2015/10/28.
 */
public interface CompanyService {
    JsonObject companyExists(ObjectId companyId);

    JsonObject createCompany(JsonObject company);

    JsonObject editCompany(JsonObject company);

    JsonObject viewCompany(String companyId);
}

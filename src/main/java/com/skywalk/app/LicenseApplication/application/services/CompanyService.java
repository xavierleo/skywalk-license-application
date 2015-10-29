package main.java.com.skywalk.app.LicenseApplication.application.services;

import org.bson.types.ObjectId;

import javax.json.JsonObject;

/**
 * Created by xavier on 2015/10/28.
 */
public interface CompanyService {
    public JsonObject companyExists(ObjectId companyId);

    public JsonObject createCompany(JsonObject company);

    public JsonObject editCompany(JsonObject company);

    public JsonObject viewCompany(String companyId);
}

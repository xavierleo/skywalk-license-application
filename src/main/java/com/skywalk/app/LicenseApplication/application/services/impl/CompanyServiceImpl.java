package main.java.com.skywalk.app.LicenseApplication.application.services.impl;

import main.java.com.skywalk.app.LicenseApplication.application.services.CompanyService;
import main.java.com.skywalk.app.LicenseApplication.domain.crud.CompanyCrudService;
import main.java.com.skywalk.app.LicenseApplication.domain.factory.Factory;
import main.java.com.skywalk.app.LicenseApplication.domain.models.Company;
import org.bson.types.ObjectId;

import javax.json.Json;
import javax.json.JsonObject;

/**
 * Created by xavier on 2015/10/28.
 */
public class CompanyServiceImpl implements CompanyService {
    private CompanyCrudService companyCrudService;
    @Override
    public JsonObject companyExists(ObjectId companyId) {
        Company c = companyCrudService.findEntityById(companyId);

        if(c != null)
            return Json.createObjectBuilder().add("Exists","Company already exists").build();

        return Json.createObjectBuilder().add("Doesnt Exist","Company doesn't exists").build();
    }

    @Override
    public JsonObject createCompany(JsonObject company) {
        Company newCompany = Factory.buildCompany(company);

        companyCrudService.createEntity(newCompany);

        return Json.createObjectBuilder().add("Successful", "Company successfully created").build();
    }

    @Override
    public JsonObject editCompany(JsonObject company) {
        return null;
    }

    @Override
    public JsonObject viewCompany(String companyId) {
        return null;
    }
}

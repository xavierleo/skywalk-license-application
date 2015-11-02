package main.java.com.skywalk.app.LicenseApplication.application.services.impl;

import lombok.extern.java.Log;
import main.java.com.skywalk.app.LicenseApplication.application.services.CompanyService;
import main.java.com.skywalk.app.LicenseApplication.application.utilities.Link;
import main.java.com.skywalk.app.LicenseApplication.application.utilities.ResponseCodes;
import main.java.com.skywalk.app.LicenseApplication.domain.crud.CompanyCrudService;
import main.java.com.skywalk.app.LicenseApplication.domain.factory.Factory;
import main.java.com.skywalk.app.LicenseApplication.domain.models.Company;
import org.bson.types.ObjectId;

import javax.json.Json;
import javax.json.JsonObject;
import javax.ws.rs.core.MediaType;
import java.util.logging.Level;

/**
 * Created by xavier on 2015/10/28.
 */
@Log
public class CompanyServiceImpl implements CompanyService {
    private CompanyCrudService companyCrudService;
    @Override
    public JsonObject companyExists(String companyId) {
        try {
            Company c = companyCrudService.findEntityById(new ObjectId(companyId));

            if(c != null)
                return Json.createObjectBuilder().add("Exists","Company already exists").build();

            return Json.createObjectBuilder().add("Doesnt Exist","Company doesn't exists").build();

        }catch (Exception e){

            return Json.createObjectBuilder().add("Doesnt Exist","Company doesn't exists").build();
        }

    }

    @Override
    public JsonObject createCompany(JsonObject company) {
        try {
            Company newCompany = Factory.buildCompany(company);

            companyCrudService.createEntity(newCompany);

            return Json.createObjectBuilder().add("Successful", "Company successfully created").build();
        }catch (Exception e){
            return Json.createObjectBuilder().add("Successful", "Company successfully created").build();
        }

    }

    @Override
    public JsonObject editCompany(JsonObject company) {
        try {
            Company toBeUpdated = new Company();
            toBeUpdated.setId(new ObjectId(company.getString("companyId")));

            companyCrudService.updateEntity(toBeUpdated);
            return Json.createObjectBuilder()
                    .add(ResponseCodes.SUCCESS.toString(), true)
                    .add(ResponseCodes.SUCCESS_CODE.toString(), 200)
                    .add(ResponseCodes.SUCCESS_MESSAGE.toString(), "User successfully updated.")
                    .build();
        }catch (Exception e){
            return Json.createObjectBuilder()
                    .add(ResponseCodes.SUCCESS.toString(), false)
                    .add(ResponseCodes.ERROR_CODE.toString(), 400)
                    .add(ResponseCodes.ERROR_MESSAGE.toString(), "User was not updated successfully.")
                    .build();
        }
    }

    @Override
    public JsonObject viewCompany(String companyId) {
        try{
            Company toView = companyCrudService.findEntityById(new ObjectId(companyId));

            if(toView == null)
                return Json.createObjectBuilder()
                        .add(ResponseCodes.SUCCESS.toString(), false)
                        .add(ResponseCodes.ERROR_CODE.toString(), 400)
                        .add(ResponseCodes.ERROR_MESSAGE.toString(), "The application was not found. Something went wrong while looking for the Application")
                        .build();

            JsonObject deleteLink = Json.createObjectBuilder()
                    .add(Link.REL.toString(), "REMOVE")
                    .add(Link.DATATYPE.toString(), MediaType.APPLICATION_JSON)
                    .add(Link.HREF.toString(), "http://server.url.com/company/"+toView.getId().toString())
                    .add(Link.METHOD.toString(), "DELETE")
                    .build();

            JsonObject editLink = Json.createObjectBuilder()
                    .add(Link.REL.toString(), "EDIT")
                    .add(Link.DATATYPE.toString(), MediaType.APPLICATION_JSON)
                    .add(Link.HREF.toString(), "http://server.url.com/company/"+toView.getId().toString())
                    .add(Link.METHOD.toString(), "PUT")
                    .build();

            return Json.createObjectBuilder()
                    .add(ResponseCodes.SUCCESS.toString(), true)
                    .add(ResponseCodes.SUCCESS_CODE.toString(), 200)
                    .add(ResponseCodes.SUCCESS_MESSAGE.toString(), "The company was successfully retrieved.")
                    .add("Link", Json.createArrayBuilder()
                            .add(deleteLink)
                            .add(editLink)
                            .build())
                    .build();

        }catch (Exception e){
            log.log(Level.WARNING, "There was an error retrieving the company", e);
            return Json.createObjectBuilder()
                    .add(ResponseCodes.SUCCESS.toString(), false)
                    .add(ResponseCodes.ERROR_CODE.toString(), 400)
                    .add(ResponseCodes.ERROR_MESSAGE.toString(), "The company was not successfully retrieved.")
                    .build();
        }
    }
}

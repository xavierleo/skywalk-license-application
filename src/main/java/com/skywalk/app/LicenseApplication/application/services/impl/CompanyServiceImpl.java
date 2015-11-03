package main.java.com.skywalk.app.LicenseApplication.application.services.impl;

import com.google.gson.Gson;
import lombok.extern.java.Log;
import main.java.com.skywalk.app.LicenseApplication.application.services.CompanyService;
import main.java.com.skywalk.app.LicenseApplication.application.utilities.Link;
import main.java.com.skywalk.app.LicenseApplication.application.utilities.ResponseCodes;
import main.java.com.skywalk.app.LicenseApplication.domain.crud.CompanyCrudService;
import main.java.com.skywalk.app.LicenseApplication.domain.crud.impl.CompanyCrudServiceImpl;
import main.java.com.skywalk.app.LicenseApplication.domain.factory.Factory;
import main.java.com.skywalk.app.LicenseApplication.domain.models.Company;
import main.java.com.skywalk.app.LicenseApplication.domain.models.EmailConfiguration;
import org.bson.types.ObjectId;

import javax.json.Json;
import javax.json.JsonObject;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.logging.Level;

/**
 * Created by xavier on 2015/10/28.
 */
@Log
public class CompanyServiceImpl implements CompanyService {
    private CompanyCrudService companyCrudService;

    public CompanyServiceImpl() {
        companyCrudService = new CompanyCrudServiceImpl();
    }

    @Override
    public JsonObject companyExists() {
        try {
            List<Company> shouldBeNullOrZero = companyCrudService.getAllEntities();

            if(shouldBeNullOrZero != null && shouldBeNullOrZero.size() > 0)
                return Json.createObjectBuilder()
                        .add(ResponseCodes.SUCCESS.toString(), false)
                        .add(ResponseCodes.ERROR_CODE.toString(), 400)
                        .add(ResponseCodes.ERROR_MESSAGE.toString(), "The company does already exist.")
                        .build();

            return Json.createObjectBuilder()
                    .add(ResponseCodes.SUCCESS.toString(), true)
                    .add(ResponseCodes.SUCCESS_CODE.toString(), 200)
                    .add(ResponseCodes.SUCCESS_MESSAGE.toString(), "The company does not exist.")
                    .build();

        }catch (Exception e){
            log.log(Level.WARNING,"There was an error while checking if the company exists. ",e);
            return Json.createObjectBuilder()
                    .add(ResponseCodes.SUCCESS.toString(), false)
                    .add(ResponseCodes.ERROR_CODE.toString(), 400)
                    .add(ResponseCodes.ERROR_MESSAGE.toString(), "The company was not successfully found. Please contact the administrator.")
                    .build();
        }

    }

    @Override
    public JsonObject createCompany(JsonObject company) {
        try {

            if(!companyExists().getBoolean("SUCCESS"))
                return Json.createObjectBuilder()
                        .add(ResponseCodes.SUCCESS.toString(), false)
                        .add(ResponseCodes.ERROR_CODE.toString(), 400)
                        .add(ResponseCodes.ERROR_MESSAGE.toString(), "A company already exists. Please check the credentials for the company")
                        .build();

            Company newCompany = Factory.buildCompany(company.getJsonObject("Company"));

            companyCrudService.createEntity(newCompany);

            Company created = companyCrudService.findEntityById(newCompany.getId());

            if(created == null)
                return Json.createObjectBuilder()
                        .add(ResponseCodes.SUCCESS.toString(), false)
                        .add(ResponseCodes.ERROR_CODE.toString(), 400)
                        .add(ResponseCodes.ERROR_MESSAGE.toString(), "The company was not successfully registered. Please contact the administrator.")
                        .build();

            Gson parser = new Gson();

            JsonObject deleteCompanyLink = Json.createObjectBuilder()
                    .add(Link.REL.toString(), "REMOVE")
                    .add(Link.DATATYPE.toString(), MediaType.APPLICATION_JSON)
                    .add(Link.HREF.toString(), "/api/company/"+created.getId().toString())
                    .add(Link.METHOD.toString(), "DELETE")
                    .build();

            JsonObject viewCompanyLink = Json.createObjectBuilder()
                    .add(Link.REL.toString(), "VIEW")
                    .add(Link.DATATYPE.toString(), MediaType.APPLICATION_JSON)
                    .add(Link.HREF.toString(), "/api/company/"+created.getId().toString())
                    .add(Link.METHOD.toString(), "GET")
                    .build();

            JsonObject updateClientLink = Json.createObjectBuilder()
                    .add(Link.REL.toString(), "EDIT")
                    .add(Link.DATATYPE.toString(), MediaType.APPLICATION_JSON)
                    .add(Link.HREF.toString(), "/api/company/")
                    .add(Link.METHOD.toString(), "PUT")
                    .build();


            return Json.createObjectBuilder()
                    .add(ResponseCodes.SUCCESS.toString(), true)
                    .add(ResponseCodes.SUCCESS_CODE.toString(), 200)
                    .add(ResponseCodes.SUCCESS_MESSAGE.toString(), "The company was successfully registered.")
                    .add("Company", parser.toJson(created))
                    .add("Links", Json.createArrayBuilder()
                        .add(viewCompanyLink)
                        .add(updateClientLink)
                        .add(deleteCompanyLink)
                        .build()
                    )
                    .build();

        }catch (Exception e){
            log.log(Level.WARNING,"There was an error while registering the new company. ",e);
            return Json.createObjectBuilder()
                    .add(ResponseCodes.SUCCESS.toString(), false)
                    .add(ResponseCodes.ERROR_CODE.toString(), 400)
                    .add(ResponseCodes.ERROR_MESSAGE.toString(), "The company was not successfully registered. Please contact the administrator.")
                    .build();
        }

    }

    @Override
    public JsonObject editCompany(JsonObject company) {
        try {

            if(!ObjectId.isValid(company.getJsonObject("Company").getString("companyId")))
                return Json.createObjectBuilder()
                        .add(ResponseCodes.SUCCESS.toString(), false)
                        .add(ResponseCodes.ERROR_CODE.toString(), 400)
                        .add(ResponseCodes.ERROR_MESSAGE.toString(), "The company can't be found. Please check the id.")
                        .build();

            Company toBeUpdated = companyCrudService.findEntityById(new ObjectId(company.getJsonObject("Company").getString("companyId")));

            toBeUpdated.setName(company.getJsonObject("Company").getString("name"));
            toBeUpdated.setIndustry(company.getJsonObject("Company").getString("industry"));
            toBeUpdated.setDescription(company.getJsonObject("Company").getString("industry"));

            EmailConfiguration emailConfiguration = new EmailConfiguration(
                    company.getJsonObject("Company").getJsonObject("EmailConfiguration").getString("smptServer"),
                    company.getJsonObject("Company").getJsonObject("EmailConfiguration").getString("serverPort"),
                    company.getJsonObject("Company").getJsonObject("EmailConfiguration").getString("authUsername"),
                    company.getJsonObject("Company").getJsonObject("EmailConfiguration").getString("authPassword"),
                    company.getJsonObject("Company").getJsonObject("EmailConfiguration").getString("primaryAccountAddress"),
                    company.getJsonObject("Company").getJsonObject("EmailConfiguration").getString("emailSignature"),
                    company.getJsonObject("Company").getJsonObject("EmailConfiguration").getString("enableSMTPAuthentication"),
                    company.getJsonObject("Company").getJsonObject("EmailConfiguration").getString("enableTTLSSupport")
            );

            toBeUpdated.setEmailConfiguration(emailConfiguration);

            companyCrudService.updateEntity(toBeUpdated);

            Gson parser = new Gson();

            JsonObject deleteCompanyLink = Json.createObjectBuilder()
                    .add(Link.REL.toString(), "REMOVE")
                    .add(Link.DATATYPE.toString(), MediaType.APPLICATION_JSON)
                    .add(Link.HREF.toString(), "/api/company/"+toBeUpdated.getId().toString())
                    .add(Link.METHOD.toString(), "DELETE")
                    .build();

            JsonObject viewCompanyLink = Json.createObjectBuilder()
                    .add(Link.REL.toString(), "VIEW")
                    .add(Link.DATATYPE.toString(), MediaType.APPLICATION_JSON)
                    .add(Link.HREF.toString(), "/api/company/"+toBeUpdated.getId().toString())
                    .add(Link.METHOD.toString(), "GET")
                    .build();

            return Json.createObjectBuilder()
                    .add(ResponseCodes.SUCCESS.toString(), true)
                    .add(ResponseCodes.SUCCESS_CODE.toString(), 200)
                    .add(ResponseCodes.SUCCESS_MESSAGE.toString(), "The company was successfully updated.")
                    .add("Company", parser.toJson(toBeUpdated))
                    .add("Links", Json.createArrayBuilder()
                            .add(viewCompanyLink)
                            .add(deleteCompanyLink)
                            .build()
                    )
                    .build();
        }catch (Exception e){
            log.log(Level.WARNING,"There was an error while updating the company exists. ",e);

            return Json.createObjectBuilder()
                    .add(ResponseCodes.SUCCESS.toString(), false)
                    .add(ResponseCodes.ERROR_CODE.toString(), 400)
                    .add(ResponseCodes.ERROR_MESSAGE.toString(), "The company was not updated successfully. Please contact administrator")
                    .build();
        }
    }

    @Override
    public JsonObject viewCompany(String companyId) {
        try{
            if(!ObjectId.isValid(companyId))
            return Json.createObjectBuilder()
                    .add(ResponseCodes.SUCCESS.toString(), false)
                    .add(ResponseCodes.ERROR_CODE.toString(), 400)
                    .add(ResponseCodes.ERROR_MESSAGE.toString(), "The company can't be found. Please check the id.")
                    .build();

            Company toView = companyCrudService.findEntityById(new ObjectId(companyId));

            if(toView == null)
                return Json.createObjectBuilder()
                        .add(ResponseCodes.SUCCESS.toString(), false)
                        .add(ResponseCodes.ERROR_CODE.toString(), 400)
                        .add(ResponseCodes.ERROR_MESSAGE.toString(), "The company was not found. Something went wrong while looking for the company")
                        .build();

            JsonObject deleteLink = Json.createObjectBuilder()
                    .add(Link.REL.toString(), "REMOVE")
                    .add(Link.DATATYPE.toString(), MediaType.APPLICATION_JSON)
                    .add(Link.HREF.toString(), "/api/company/"+toView.getId().toString())
                    .add(Link.METHOD.toString(), "DELETE")
                    .build();

            JsonObject editLink = Json.createObjectBuilder()
                    .add(Link.REL.toString(), "EDIT")
                    .add(Link.DATATYPE.toString(), MediaType.APPLICATION_JSON)
                    .add(Link.HREF.toString(), "/api/company")
                    .add(Link.METHOD.toString(), "PUT")
                    .build();

            return Json.createObjectBuilder()
                    .add(ResponseCodes.SUCCESS.toString(), true)
                    .add(ResponseCodes.SUCCESS_CODE.toString(), 200)
                    .add(ResponseCodes.SUCCESS_MESSAGE.toString(), "The company was successfully retrieved.")
                    .add("Company",new Gson().toJson(toView))
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

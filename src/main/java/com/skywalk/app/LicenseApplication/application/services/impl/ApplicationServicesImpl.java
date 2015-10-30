/**
 * 
 */
package main.java.com.skywalk.app.LicenseApplication.application.services.impl;

import lombok.extern.java.Log;
import main.java.com.skywalk.app.LicenseApplication.application.services.ApplicationServices;
import main.java.com.skywalk.app.LicenseApplication.application.utilities.Link;
import main.java.com.skywalk.app.LicenseApplication.application.utilities.ResponseCodes;
import main.java.com.skywalk.app.LicenseApplication.domain.crud.ApplicationCrudService;
import main.java.com.skywalk.app.LicenseApplication.domain.crud.CompanyCrudService;
import main.java.com.skywalk.app.LicenseApplication.domain.crud.PriceRangeCrudService;
import main.java.com.skywalk.app.LicenseApplication.domain.crud.impl.ApplicationCrudServiceImpl;
import main.java.com.skywalk.app.LicenseApplication.domain.crud.impl.CompanyCrudServiceImpl;
import main.java.com.skywalk.app.LicenseApplication.domain.crud.impl.PriceRangeCrudServiceImpl;
import main.java.com.skywalk.app.LicenseApplication.domain.factory.Factory;
import main.java.com.skywalk.app.LicenseApplication.domain.models.Application;
import main.java.com.skywalk.app.LicenseApplication.domain.models.Company;
import main.java.com.skywalk.app.LicenseApplication.domain.models.PriceRange;
import org.bson.types.ObjectId;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.logging.Level;

/**
 * @author ironhulk
 *
 */
@Log
public class ApplicationServicesImpl implements ApplicationServices {

    private ApplicationCrudService applicationCrudService;
    private CompanyCrudService companyCrudService;
    private PriceRangeCrudService priceRangeCrudService;

    public ApplicationServicesImpl(){
        applicationCrudService = new ApplicationCrudServiceImpl();
        companyCrudService = new CompanyCrudServiceImpl();
        priceRangeCrudService = new PriceRangeCrudServiceImpl();
    }

    @Override
    public JsonObject registerApplication(JsonObject applicationParam) {
        try{
            Application application = Factory.buildApplication(applicationParam);

            //find the company to register the application against
            if(!ObjectId.isValid(applicationParam.getString("companyId")))
                return Json.createObjectBuilder()
                        .add(ResponseCodes.SUCCESS.toString(), false)
                        .add(ResponseCodes.ERROR_CODE.toString(), 400)
                        .add(ResponseCodes.ERROR_MESSAGE.toString(), "The application was not registered, the company id to link it to was not a valid id.")
                        .build();

            //find the company to register the application against
            Company company = companyCrudService.findEntityById(new ObjectId(applicationParam.getString("companyId")));

            if(company == null)
                return Json.createObjectBuilder()
                        .add(ResponseCodes.SUCCESS.toString(), false)
                        .add(ResponseCodes.ERROR_CODE.toString(), 400)
                        .add(ResponseCodes.ERROR_MESSAGE.toString(), "The application was not registered, the company id to link it to was not found.")
                        .build();

            applicationCrudService.createEntity(application);

            Application created = applicationCrudService.findEntityById(application.getId());

            if(created == null)
                return Json.createObjectBuilder()
                        .add(ResponseCodes.SUCCESS.toString(), false)
                        .add(ResponseCodes.ERROR_CODE.toString(), 400)
                        .add(ResponseCodes.ERROR_MESSAGE.toString(), "The application was not successfully registered. Something went wrong while registering.")
                        .build();

            JsonObject applicationLinks = Json.createObjectBuilder()
                    .add(Link.REL.toString(), "VIEW")
                    .add(Link.DATATYPE.toString(), MediaType.APPLICATION_JSON)
                    .add(Link.HREF.toString(), "http://server.url.com/application/id")
                    .add(Link.METHOD.toString(), "GET")
                    .build();

            return Json.createObjectBuilder()
                    .add(ResponseCodes.SUCCESS.toString(), true)
                    .add(ResponseCodes.SUCCESS_CODE.toString(), 200)
                    .add(ResponseCodes.SUCCESS_MESSAGE.toString(), "The application was successfully registered.")
                    .add("Link", applicationLinks)
                    .build();

        }catch (Exception e){
            log.log(Level.WARNING, "There was an error registering the application", e);
            return Json.createObjectBuilder()
                    .add(ResponseCodes.SUCCESS.toString(), false)
                    .add(ResponseCodes.ERROR_CODE.toString(), 400)
                    .add(ResponseCodes.ERROR_MESSAGE.toString(), "The application was not successfully registered.")
                    .build();
        }
    }

    @Override
    public JsonObject editApplication(JsonObject application) {
        try{


            //find the company to register the application against
            if(!ObjectId.isValid(application.getString("applicationId")))
                return Json.createObjectBuilder()
                        .add(ResponseCodes.SUCCESS.toString(), false)
                        .add(ResponseCodes.ERROR_CODE.toString(), 400)
                        .add(ResponseCodes.ERROR_MESSAGE.toString(), "The application was not found, the id is not a valid id.")
                        .build();

            Application toEdit = applicationCrudService.findEntityById(new ObjectId(application.getString("applicationId")));

            if(toEdit == null)
                return Json.createObjectBuilder()
                        .add(ResponseCodes.SUCCESS.toString(), false)
                        .add(ResponseCodes.ERROR_CODE.toString(), 400)
                        .add(ResponseCodes.ERROR_MESSAGE.toString(), "The application was not found. Something went wrong while looking for the Application")
                        .build();

            JsonObject deleteLink = Json.createObjectBuilder()
                    .add(Link.REL.toString(), "REMOVE")
                    .add(Link.DATATYPE.toString(), MediaType.APPLICATION_JSON)
                    .add(Link.HREF.toString(), "http://server.url.com/application/"+application.getString("applicationId"))
                    .add(Link.METHOD.toString(), "DELETE")
                    .build();

            JsonObject viewLink = Json.createObjectBuilder()
                    .add(Link.REL.toString(), "VIEW")
                    .add(Link.DATATYPE.toString(), MediaType.APPLICATION_JSON)
                    .add(Link.HREF.toString(), "http://server.url.com/application/"+application.getString("applicationId"))
                    .add(Link.METHOD.toString(), "GET")
                    .build();

            //update the application
            toEdit.setLongDescription(application.getJsonObject("Application").getString("longDescription"));
            toEdit.setShortDescription(application.getJsonObject("Application").getString("shortDescription"));
            toEdit.setName(application.getJsonObject("Application").getString("name"));

            applicationCrudService.updateEntity(toEdit);


            return Json.createObjectBuilder()
                    .add(ResponseCodes.SUCCESS.toString(), true)
                    .add(ResponseCodes.SUCCESS_CODE.toString(), 200)
                    .add(ResponseCodes.SUCCESS_MESSAGE.toString(), "The application was successfully updated.")
                    .add("Link", Json.createArrayBuilder()
                            .add(deleteLink)
                            .add(viewLink)
                            .build())
                    .build();

        }catch (Exception e){
            log.log(Level.WARNING, "There was an error retrieving the application", e);
            return Json.createObjectBuilder()
                    .add(ResponseCodes.SUCCESS.toString(), false)
                    .add(ResponseCodes.ERROR_CODE.toString(), 400)
                    .add(ResponseCodes.ERROR_MESSAGE.toString(), "The application was not successfully retrieved.")
                    .build();
        }
    }

    @Override
    public JsonObject viewApplication(ObjectId id) {
        try{
            Application toView = applicationCrudService.findEntityById(id);

            if(toView == null)
                return Json.createObjectBuilder()
                        .add(ResponseCodes.SUCCESS.toString(), false)
                        .add(ResponseCodes.ERROR_CODE.toString(), 400)
                        .add(ResponseCodes.ERROR_MESSAGE.toString(), "The application was not found. Something went wrong while looking for the Application")
                        .build();

            JsonObject deleteLink = Json.createObjectBuilder()
                    .add(Link.REL.toString(), "REMOVE")
                    .add(Link.DATATYPE.toString(), MediaType.APPLICATION_JSON)
                    .add(Link.HREF.toString(), "http://server.url.com/application/id")
                    .add(Link.METHOD.toString(), "DELETE")
                    .build();

            JsonObject editLink = Json.createObjectBuilder()
                    .add(Link.REL.toString(), "EDIT")
                    .add(Link.DATATYPE.toString(), MediaType.APPLICATION_JSON)
                    .add(Link.HREF.toString(), "http://server.url.com/application/id")
                    .add(Link.METHOD.toString(), "PUT")
                    .build();

            return Json.createObjectBuilder()
                    .add(ResponseCodes.SUCCESS.toString(), true)
                    .add(ResponseCodes.SUCCESS_CODE.toString(), 200)
                    .add(ResponseCodes.SUCCESS_MESSAGE.toString(), "The application was successfully retrieved.")
                    .add("Link", Json.createArrayBuilder()
                                    .add(deleteLink)
                                    .add(editLink)
                                    .build())
                    .build();

        }catch (Exception e){
            log.log(Level.WARNING, "There was an error retrieving the application", e);
            return Json.createObjectBuilder()
                    .add(ResponseCodes.SUCCESS.toString(), false)
                    .add(ResponseCodes.ERROR_CODE.toString(), 400)
                    .add(ResponseCodes.ERROR_MESSAGE.toString(), "The application was not successfully retrieved.")
                    .build();
        }
    }

    @Override
    public JsonObject viewAllApplication(ObjectId companyId) {
        try{
            Company company = companyCrudService.findEntityById(companyId);

            if(company ==  null)
                return Json.createObjectBuilder()
                        .add(ResponseCodes.SUCCESS.toString(), false)
                        .add(ResponseCodes.ERROR_CODE.toString(), 400)
                        .add(ResponseCodes.ERROR_MESSAGE.toString(), "The applications was not found, the company id to link it to was not found.")
                        .build();

            List<Application> allApplications = company.getApplications();
            JsonArrayBuilder builder = Json.createArrayBuilder();

            for(Application a: allApplications){
                JsonObject deleteLink = Json.createObjectBuilder()
                        .add(Link.REL.toString(), "REMOVE")
                        .add(Link.DATATYPE.toString(), MediaType.APPLICATION_JSON)
                        .add(Link.HREF.toString(), "http://server.url.com/application/" + a.getId().toString())
                        .add(Link.METHOD.toString(), "DELETE")
                        .build();

                JsonObject editLink = Json.createObjectBuilder()
                        .add(Link.REL.toString(), "EDIT")
                        .add(Link.DATATYPE.toString(), MediaType.APPLICATION_JSON)
                        .add(Link.HREF.toString(), "http://server.url.com/application/"+a.getId().toString())
                        .add(Link.METHOD.toString(), "PUT")
                        .build();

                JsonObject viewLink = Json.createObjectBuilder()
                        .add(Link.REL.toString(), "VIEW")
                        .add(Link.DATATYPE.toString(), MediaType.APPLICATION_JSON)
                        .add(Link.HREF.toString(), "http://server.url.com/application/"+a.getId().toString())
                        .add(Link.METHOD.toString(), "GET")
                        .build();

                builder.add(
                        Json.createObjectBuilder()
                                .add("Name", a.getName())
                                .add("ShortDescription", a.getShortDescription())
                                .add("LongDescription", a.getLongDescription())
                                .add("ID", a.getId().toString())
                                .add("Link", Json.createArrayBuilder()
                                                .add(viewLink)
                                                .add(editLink)
                                                .add(deleteLink)
                                                .build()
                                )
                                .build()
                );
            }

            return Json.createObjectBuilder()
                    .add(ResponseCodes.SUCCESS.toString(), true)
                    .add(ResponseCodes.SUCCESS_CODE.toString(), 200)
                    .add(ResponseCodes.SUCCESS_MESSAGE.toString(), "The applications was successfully retrieved.")
                    .add("Applications",builder.build())
                    .build();
        }catch (Exception e){
            log.log(Level.WARNING, "There was an error retrieving the applications", e);
            return Json.createObjectBuilder()
                    .add(ResponseCodes.SUCCESS.toString(), false)
                    .add(ResponseCodes.ERROR_CODE.toString(), 400)
                    .add(ResponseCodes.ERROR_MESSAGE.toString(), "The applications was not successfully retrieved.")
                    .build();
        }
    }

    @Override
    public JsonObject removeApplication(ObjectId applicationId) {
        try{
            Application toRemove = applicationCrudService.findEntityById(applicationId);

            if(toRemove == null)
                return Json.createObjectBuilder()
                        .add(ResponseCodes.SUCCESS.toString(), false)
                        .add(ResponseCodes.ERROR_CODE.toString(), 400)
                        .add(ResponseCodes.ERROR_MESSAGE.toString(), "The application was not removed. Something went wrong while looking for the Application")
                        .build();
            //remove all targets that depend on the application(Price Ranges)
            List<PriceRange> priceRanges = toRemove.getPriceRanges();

            for(PriceRange pr: priceRanges){
                priceRangeCrudService.deleteEntity(pr);
            }

            applicationCrudService.deleteEntity(toRemove);

            Application removed = applicationCrudService.findEntityById(applicationId);

            if(removed!=null)
                return Json.createObjectBuilder()
                        .add(ResponseCodes.SUCCESS.toString(), false)
                        .add(ResponseCodes.ERROR_CODE.toString(), 400)
                        .add(ResponseCodes.ERROR_MESSAGE.toString(), "The application was not successfully removed.")
                        .build();

            return Json.createObjectBuilder()
                    .add(ResponseCodes.SUCCESS.toString(), true)
                    .add(ResponseCodes.SUCCESS_CODE.toString(), 200)
                    .add(ResponseCodes.SUCCESS_MESSAGE.toString(), "The application and its price ranges was successfully removed.")
                    .build();

        }catch (Exception e){
            log.log(Level.WARNING, "There was an error retrieving the application", e);
            return Json.createObjectBuilder()
                    .add(ResponseCodes.SUCCESS.toString(), false)
                    .add(ResponseCodes.ERROR_CODE.toString(), 400)
                    .add(ResponseCodes.ERROR_MESSAGE.toString(), "The application was not successfully removed.")
                    .build();
        }
    }
}

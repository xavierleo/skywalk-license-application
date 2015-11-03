/**
 * 
 */
package main.java.com.skywalk.app.LicenseApplication.application.services.impl;

import com.google.gson.Gson;
import lombok.extern.java.Log;
import main.java.com.skywalk.app.LicenseApplication.application.services.ApplicationServices;
import main.java.com.skywalk.app.LicenseApplication.application.utilities.Link;
import main.java.com.skywalk.app.LicenseApplication.application.utilities.ResponseCodes;
import main.java.com.skywalk.app.LicenseApplication.domain.crud.*;
import main.java.com.skywalk.app.LicenseApplication.domain.crud.impl.*;
import main.java.com.skywalk.app.LicenseApplication.domain.factory.Factory;
import main.java.com.skywalk.app.LicenseApplication.domain.models.*;
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
    private ClientCrudService clientCrudService;
    private ClientApplicationCrudService clientApplicationCrudService;

    public ApplicationServicesImpl(){
        applicationCrudService = new ApplicationCrudServiceImpl();
        companyCrudService = new CompanyCrudServiceImpl();
        priceRangeCrudService = new PriceRangeCrudServiceImpl();
        clientCrudService = new ClientCrudServiceImpl();
        clientApplicationCrudService = new ClientApplicationCrudServiceImpl();
    }

    @Override
    public JsonObject registerApplication(JsonObject applicationParam) {
        try{
            Application application = Factory.buildApplication(applicationParam.getJsonObject("Application"));

            //find the company to register the application against
            if(!ObjectId.isValid(applicationParam.getJsonObject("Company").getString("companyId")))
                return Json.createObjectBuilder()
                        .add(ResponseCodes.SUCCESS.toString(), false)
                        .add(ResponseCodes.ERROR_CODE.toString(), 400)
                        .add(ResponseCodes.ERROR_MESSAGE.toString(), "The application was not registered, the company id to link it to was not a valid id.")
                        .build();

            applicationCrudService.createEntity(application);

            Application created = applicationCrudService.findEntityById(application.getId());

            if(created == null)
                return Json.createObjectBuilder()
                        .add(ResponseCodes.SUCCESS.toString(), false)
                        .add(ResponseCodes.ERROR_CODE.toString(), 400)
                        .add(ResponseCodes.ERROR_MESSAGE.toString(), "The application was not successfully registered. Something went wrong while registering.")
                        .build();

            //find the company to register the application against
            Company company = companyCrudService.findEntityById(new ObjectId(applicationParam.getJsonObject("Company").getString("companyId")));

            if(company == null)
                return Json.createObjectBuilder()
                        .add(ResponseCodes.SUCCESS.toString(), false)
                        .add(ResponseCodes.ERROR_CODE.toString(), 400)
                        .add(ResponseCodes.ERROR_MESSAGE.toString(), "The application was not registered, the company id to link it to was not found.")
                        .build();


            //link application to company
            company.getApplications().add(created);

            companyCrudService.updateEntity(company);

            JsonObject applicationLinks = Json.createObjectBuilder()
                    .add(Link.REL.toString(), "VIEW")
                    .add(Link.DATATYPE.toString(), MediaType.APPLICATION_JSON)
                    .add(Link.HREF.toString(), "/api/application/"+created.getId())
                    .add(Link.METHOD.toString(), "GET")
                    .build();

            return Json.createObjectBuilder()
                    .add(ResponseCodes.SUCCESS.toString(), true)
                    .add(ResponseCodes.SUCCESS_CODE.toString(), 200)
                    .add(ResponseCodes.SUCCESS_MESSAGE.toString(), "The application was successfully registered.")
                    .add("Application", new Gson().toJson(created))
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
            if(!ObjectId.isValid(application.getJsonObject("Application").getString("applicationId")))
                return Json.createObjectBuilder()
                        .add(ResponseCodes.SUCCESS.toString(), false)
                        .add(ResponseCodes.ERROR_CODE.toString(), 400)
                        .add(ResponseCodes.ERROR_MESSAGE.toString(), "The application was not found, the id is not a valid id.")
                        .build();

            Application toEdit = applicationCrudService.findEntityById(new ObjectId(application.getJsonObject("Application").getString("applicationId")));

            if(toEdit == null)
                return Json.createObjectBuilder()
                        .add(ResponseCodes.SUCCESS.toString(), false)
                        .add(ResponseCodes.ERROR_CODE.toString(), 400)
                        .add(ResponseCodes.ERROR_MESSAGE.toString(), "The application was not found. Something went wrong while looking for the Application")
                        .build();

            JsonObject deleteLink = Json.createObjectBuilder()
                    .add(Link.REL.toString(), "REMOVE")
                    .add(Link.DATATYPE.toString(), MediaType.APPLICATION_JSON)
                    .add(Link.HREF.toString(), "/api/application/"+toEdit.getId().toString())
                    .add(Link.METHOD.toString(), "DELETE")
                    .build();

            JsonObject viewLink = Json.createObjectBuilder()
                    .add(Link.REL.toString(), "VIEW")
                    .add(Link.DATATYPE.toString(), MediaType.APPLICATION_JSON)
                    .add(Link.HREF.toString(), "/api/application/"+toEdit.getId().toString())
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
                    .add("Application",new Gson().toJson(toEdit))
                    .add("Link", Json.createArrayBuilder()
                            .add(deleteLink)
                            .add(viewLink)
                            .build())
                    .build();

        }catch (Exception e){
            log.log(Level.WARNING, "There was an error updating the application", e);
            return Json.createObjectBuilder()
                    .add(ResponseCodes.SUCCESS.toString(), false)
                    .add(ResponseCodes.ERROR_CODE.toString(), 400)
                    .add(ResponseCodes.ERROR_MESSAGE.toString(), "The application was not successfully updated.")
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
                    .add(Link.HREF.toString(), "/api/application/"+toView.getId().toString())
                    .add(Link.METHOD.toString(), "DELETE")
                    .build();

            JsonObject editLink = Json.createObjectBuilder()
                    .add(Link.REL.toString(), "EDIT")
                    .add(Link.DATATYPE.toString(), MediaType.APPLICATION_JSON)
                    .add(Link.HREF.toString(), "/api/application/")
                    .add(Link.METHOD.toString(), "PUT")
                    .build();

            Gson pojoParser = new Gson();

            return Json.createObjectBuilder()
                    .add(ResponseCodes.SUCCESS.toString(), true)
                    .add(ResponseCodes.SUCCESS_CODE.toString(), 200)
                    .add(ResponseCodes.SUCCESS_MESSAGE.toString(), "The application was successfully retrieved.")
                    .add("Application", pojoParser.toJson(toView))
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
                        .add(Link.HREF.toString(), "/api/application/" + a.getId().toString())
                        .add(Link.METHOD.toString(), "DELETE")
                        .build();

                JsonObject editLink = Json.createObjectBuilder()
                        .add(Link.REL.toString(), "EDIT")
                        .add(Link.DATATYPE.toString(), MediaType.APPLICATION_JSON)
                        .add(Link.HREF.toString(), "/api/application/")
                        .add(Link.METHOD.toString(), "PUT")
                        .build();

                JsonObject viewLink = Json.createObjectBuilder()
                        .add(Link.REL.toString(), "VIEW")
                        .add(Link.DATATYPE.toString(), MediaType.APPLICATION_JSON)
                        .add(Link.HREF.toString(), "/api/application/"+a.getId().toString())
                        .add(Link.METHOD.toString(), "GET")
                        .build();

                builder.add(
                        Json.createObjectBuilder()
                                .add("name", a.getName())
                                .add("shortDescription", a.getShortDescription())
                                .add("longDescription", a.getLongDescription())
                                .add("applicationId", a.getId().toString())
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

            priceRanges.forEach(priceRangeCrudService::deleteEntity);

            //first remove reference from company
            List<Company> companies = companyCrudService.getAllEntities();

            if(companies == null || companies.size() <= 0)
                return Json.createObjectBuilder()
                        .add(ResponseCodes.SUCCESS.toString(), false)
                        .add(ResponseCodes.ERROR_CODE.toString(), 400)
                        .add(ResponseCodes.ERROR_MESSAGE.toString(), "The user was not removed properly. The company the user registered against does not exist.")
                        .build();

            for(User u:companies.get(0).getUsers()) {
                if (toRemove.getId().equals(u.getId())) {
                    companies.get(0).getUsers().remove(u);
                    companyCrudService.updateEntity(companies.get(0));
                    break;
                }
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

    @Override
    public JsonObject assignClientToApplication(JsonObject application) {
        try{
            //find the application to register the client against
            if(!ObjectId.isValid(application.getString("clientId")))
                return Json.createObjectBuilder()
                        .add(ResponseCodes.SUCCESS.toString(), false)
                        .add(ResponseCodes.ERROR_CODE.toString(), 400)
                        .add(ResponseCodes.ERROR_MESSAGE.toString(), "The client was not found, the id is not a valid id.")
                        .build();

            if(!ObjectId.isValid(application.getString("applicationId")))
                return Json.createObjectBuilder()
                        .add(ResponseCodes.SUCCESS.toString(), false)
                        .add(ResponseCodes.ERROR_CODE.toString(), 400)
                        .add(ResponseCodes.ERROR_MESSAGE.toString(), "The application was not found, the id is not a valid id.")
                        .build();

            Client clientToAssign = clientCrudService.findEntityById(new ObjectId(application.getString("clientId")));

            if(clientToAssign == null)
                return Json.createObjectBuilder()
                        .add(ResponseCodes.SUCCESS.toString(), false)
                        .add(ResponseCodes.ERROR_CODE.toString(), 400)
                        .add(ResponseCodes.ERROR_MESSAGE.toString(), "The client was not found with the id provided")
                        .build();

            Application toEdit = applicationCrudService.findEntityById(new ObjectId(application.getString("applicationId")));

            if(toEdit == null)
                return Json.createObjectBuilder()
                        .add(ResponseCodes.SUCCESS.toString(), false)
                        .add(ResponseCodes.ERROR_CODE.toString(), 400)
                        .add(ResponseCodes.ERROR_MESSAGE.toString(), "The application was not found. Something went wrong while looking for the Application")
                        .build();

            JsonObject deleteApplicationLink = Json.createObjectBuilder()
                    .add(Link.REL.toString(), "REMOVE")
                    .add(Link.DATATYPE.toString(), MediaType.APPLICATION_JSON)
                    .add(Link.HREF.toString(), "http://server.url.com/application/"+application.getString("applicationId"))
                    .add(Link.METHOD.toString(), "DELETE")
                    .build();

            JsonObject viewApplicationLink = Json.createObjectBuilder()
                    .add(Link.REL.toString(), "VIEW")
                    .add(Link.DATATYPE.toString(), MediaType.APPLICATION_JSON)
                    .add(Link.HREF.toString(), "http://server.url.com/application/"+application.getString("applicationId"))
                    .add(Link.METHOD.toString(), "GET")
                    .build();

            JsonObject deleteClientLink = Json.createObjectBuilder()
                    .add(Link.REL.toString(), "REMOVE")
                    .add(Link.DATATYPE.toString(), MediaType.APPLICATION_JSON)
                    .add(Link.HREF.toString(), "http://server.url.com/client/"+application.getString("clientId"))
                    .add(Link.METHOD.toString(), "DELETE")
                    .build();

            JsonObject viewClientLink = Json.createObjectBuilder()
                    .add(Link.REL.toString(), "VIEW")
                    .add(Link.DATATYPE.toString(), MediaType.APPLICATION_JSON)
                    .add(Link.HREF.toString(), "http://server.url.com/client/"+application.getString("clientId"))
                    .add(Link.METHOD.toString(), "GET")
                    .build();

            JsonObject updateClientLink = Json.createObjectBuilder()
                    .add(Link.REL.toString(), "VIEW")
                    .add(Link.DATATYPE.toString(), MediaType.APPLICATION_JSON)
                    .add(Link.HREF.toString(), "http://server.url.com/client/"+application.getString("clientId"))
                    .add(Link.METHOD.toString(), "PUT")
                    .build();

            //first create the bridge
            ClientApplication clientApplication = new ClientApplication();
            clientApplication.setId(new ObjectId());
            clientApplication.setApplication(toEdit);
            clientApplication.setClient(clientToAssign);

            clientApplicationCrudService.createEntity(clientApplication);

            ClientApplication created = clientApplicationCrudService.findEntityById(clientApplication.getId());

            if(created == null)
                return Json.createObjectBuilder()
                        .add(ResponseCodes.SUCCESS.toString(), false)
                        .add(ResponseCodes.ERROR_CODE.toString(), 400)
                        .add(ResponseCodes.ERROR_MESSAGE.toString(), "The application was not successfully linked to the client.")
                        .build();

            toEdit.getClientApplications().add(created);
            //update the application
            applicationCrudService.updateEntity(toEdit);

            clientToAssign.getClientApplications().add(created);
            //update the client
            clientCrudService.updateEntity(clientToAssign);

            return Json.createObjectBuilder()
                    .add(ResponseCodes.SUCCESS.toString(), true)
                    .add(ResponseCodes.SUCCESS_CODE.toString(), 200)
                    .add(ResponseCodes.SUCCESS_MESSAGE.toString(), "The client was successfully added to the application.")
                    .add("Application", Json.createObjectBuilder()
                            .add("Link", Json.createArrayBuilder()
                                    .add(deleteApplicationLink)
                                    .add(viewApplicationLink)
                                    .build())
                            .build())
                    .add("Client", Json.createObjectBuilder()
                            .add("Link", Json.createArrayBuilder()
                                    .add(deleteClientLink)
                                    .add(updateClientLink)
                                    .add(viewClientLink)
                                    .build())
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
    public JsonObject assignPriceRangeToApplication(JsonObject application) {
        try{
            //find the application to register the client against
            if(!ObjectId.isValid(application.getString("priceRangeId")))
                return Json.createObjectBuilder()
                        .add(ResponseCodes.SUCCESS.toString(), false)
                        .add(ResponseCodes.ERROR_CODE.toString(), 400)
                        .add(ResponseCodes.ERROR_MESSAGE.toString(), "The price range was not found, the id is not a valid id.")
                        .build();

            if(!ObjectId.isValid(application.getString("applicationId")))
                return Json.createObjectBuilder()
                        .add(ResponseCodes.SUCCESS.toString(), false)
                        .add(ResponseCodes.ERROR_CODE.toString(), 400)
                        .add(ResponseCodes.ERROR_MESSAGE.toString(), "The application was not found, the id is not a valid id.")
                        .build();

            PriceRange priceRangeToAssign = priceRangeCrudService.findEntityById(new ObjectId(application.getString("priceRangeId")));

            if(priceRangeToAssign == null)
                return Json.createObjectBuilder()
                        .add(ResponseCodes.SUCCESS.toString(), false)
                        .add(ResponseCodes.ERROR_CODE.toString(), 400)
                        .add(ResponseCodes.ERROR_MESSAGE.toString(), "The price range was not found with the id provided")
                        .build();

            Application toEdit = applicationCrudService.findEntityById(new ObjectId(application.getString("applicationId")));

            if(toEdit == null)
                return Json.createObjectBuilder()
                        .add(ResponseCodes.SUCCESS.toString(), false)
                        .add(ResponseCodes.ERROR_CODE.toString(), 400)
                        .add(ResponseCodes.ERROR_MESSAGE.toString(), "The application was not found. Something went wrong while looking for the Application")
                        .build();

            JsonObject deleteApplicationLink = Json.createObjectBuilder()
                    .add(Link.REL.toString(), "REMOVE")
                    .add(Link.DATATYPE.toString(), MediaType.APPLICATION_JSON)
                    .add(Link.HREF.toString(), "http://server.url.com/application/"+application.getString("applicationId"))
                    .add(Link.METHOD.toString(), "DELETE")
                    .build();

            JsonObject viewApplicationLink = Json.createObjectBuilder()
                    .add(Link.REL.toString(), "VIEW")
                    .add(Link.DATATYPE.toString(), MediaType.APPLICATION_JSON)
                    .add(Link.HREF.toString(), "http://server.url.com/application/"+application.getString("applicationId"))
                    .add(Link.METHOD.toString(), "GET")
                    .build();

            JsonObject deletePriceRangeLink = Json.createObjectBuilder()
                    .add(Link.REL.toString(), "REMOVE")
                    .add(Link.DATATYPE.toString(), MediaType.APPLICATION_JSON)
                    .add(Link.HREF.toString(), "http://server.url.com/priceRange/"+application.getString("priceRangeId"))
                    .add(Link.METHOD.toString(), "DELETE")
                    .build();

            JsonObject viewPriceRangeLink = Json.createObjectBuilder()
                    .add(Link.REL.toString(), "VIEW")
                    .add(Link.DATATYPE.toString(), MediaType.APPLICATION_JSON)
                    .add(Link.HREF.toString(), "http://server.url.com/priceRange/"+application.getString("priceRangeId"))
                    .add(Link.METHOD.toString(), "GET")
                    .build();

            JsonObject updatePriceRangeLink = Json.createObjectBuilder()
                    .add(Link.REL.toString(), "VIEW")
                    .add(Link.DATATYPE.toString(), MediaType.APPLICATION_JSON)
                    .add(Link.HREF.toString(), "http://server.url.com/priceRange/"+application.getString("priceRangeId"))
                    .add(Link.METHOD.toString(), "PUT")
                    .build();

            //update the application
            toEdit.getPriceRanges().add(priceRangeToAssign);

            applicationCrudService.updateEntity(toEdit);

            return Json.createObjectBuilder()
                    .add(ResponseCodes.SUCCESS.toString(), true)
                    .add(ResponseCodes.SUCCESS_CODE.toString(), 200)
                    .add(ResponseCodes.SUCCESS_MESSAGE.toString(), "The client was successfully added to the application.")
                    .add("Application", Json.createObjectBuilder()
                            .add("Link", Json.createArrayBuilder()
                                    .add(deleteApplicationLink)
                                    .add(viewApplicationLink)
                                    .build())
                            .build())
                    .add("Client", Json.createObjectBuilder()
                            .add("Link", Json.createArrayBuilder()
                                    .add(deletePriceRangeLink)
                                    .add(updatePriceRangeLink)
                                    .add(viewPriceRangeLink)
                                    .build())
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
}

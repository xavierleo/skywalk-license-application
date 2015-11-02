package main.java.com.skywalk.app.LicenseApplication.application.services.impl;

import com.google.gson.Gson;
import lombok.extern.java.Log;
import main.java.com.skywalk.app.LicenseApplication.application.services.LicenseServices;
import main.java.com.skywalk.app.LicenseApplication.application.utilities.Link;
import main.java.com.skywalk.app.LicenseApplication.application.utilities.ResponseCodes;
import main.java.com.skywalk.app.LicenseApplication.domain.crud.ApplicationCrudService;
import main.java.com.skywalk.app.LicenseApplication.domain.crud.ClientApplicationCrudService;
import main.java.com.skywalk.app.LicenseApplication.domain.crud.ClientCrudService;
import main.java.com.skywalk.app.LicenseApplication.domain.crud.LicenseCrudService;
import main.java.com.skywalk.app.LicenseApplication.domain.crud.impl.ApplicationCrudServiceImpl;
import main.java.com.skywalk.app.LicenseApplication.domain.crud.impl.ClientApplicationCrudServiceImpl;
import main.java.com.skywalk.app.LicenseApplication.domain.crud.impl.ClientCrudServiceImpl;
import main.java.com.skywalk.app.LicenseApplication.domain.crud.impl.LicenseCrudServiceImpl;
import main.java.com.skywalk.app.LicenseApplication.domain.factory.Factory;
import main.java.com.skywalk.app.LicenseApplication.domain.models.*;
import org.bson.types.ObjectId;
import org.joda.time.DateTime;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.logging.Level;

/**
 * Created by ironhulk on 02/11/2015.
 */
@Log
public class LicenseServicesImpl implements LicenseServices{

    private LicenseCrudService licenseCrudService;
    private ApplicationCrudService applicationCrudService;
    private ClientCrudService clientCrudService;
    private ClientApplicationCrudService clientApplicationCrudService;

    public LicenseServicesImpl() {
        licenseCrudService = new LicenseCrudServiceImpl();
        applicationCrudService = new ApplicationCrudServiceImpl();
        clientCrudService = new ClientCrudServiceImpl();
        clientApplicationCrudService = new ClientApplicationCrudServiceImpl();
    }

    @Override
    public JsonObject generateLicenseForClientAndApplication(JsonObject newLicenseDetails) {
        try{
            //check application id is valid
            if(!ObjectId.isValid(newLicenseDetails.getJsonObject("Application").getString("applicationId")))
                return Json.createObjectBuilder()
                        .add(ResponseCodes.SUCCESS.toString(), false)
                        .add(ResponseCodes.ERROR_CODE.toString(), 400)
                        .add(ResponseCodes.ERROR_MESSAGE.toString(), "The application was not found, the application id to was not a valid id.")
                        .build();

            Application toRetrievePriceRanges = applicationCrudService.findEntityById(new ObjectId(newLicenseDetails.getString("applicationId")));
            //check client id is valid
            if(!ObjectId.isValid(newLicenseDetails.getJsonObject("Client").getString("applicationId")))
                return Json.createObjectBuilder()
                        .add(ResponseCodes.SUCCESS.toString(), false)
                        .add(ResponseCodes.ERROR_CODE.toString(), 400)
                        .add(ResponseCodes.ERROR_MESSAGE.toString(), "The client was not found, the client id to was not a valid id.")
                        .build();

            Client toGenerateLicenseFor = clientCrudService.findEntityById(new ObjectId(newLicenseDetails.getJsonObject("Client").getString("clientId")));

            //check if the client is already linked to the application
            ClientApplication clientApplication = clientApplicationCrudService.getClientApplicationByClientAndAppId(toGenerateLicenseFor.getId(),toRetrievePriceRanges.getId());

            if(clientApplication==null){
                //link to the application
                //first create the bridge
                clientApplication = new ClientApplication();
                clientApplication.setId(new ObjectId());
                clientApplication.setApplication(toRetrievePriceRanges);
                clientApplication.setClient(toGenerateLicenseFor);

                clientApplicationCrudService.createEntity(clientApplication);

                ClientApplication created = clientApplicationCrudService.findEntityById(clientApplication.getId());

                if(created == null)
                    return Json.createObjectBuilder()
                            .add(ResponseCodes.SUCCESS.toString(), false)
                            .add(ResponseCodes.ERROR_CODE.toString(), 400)
                            .add(ResponseCodes.ERROR_MESSAGE.toString(), "The application was not successfully linked to the client.")
                            .build();

                toRetrievePriceRanges.getClientApplications().add(created);
                //update the application
                applicationCrudService.updateEntity(toRetrievePriceRanges);

                toGenerateLicenseFor.getClientApplications().add(created);
                //update the client
                clientCrudService.updateEntity(toGenerateLicenseFor);

                //get price ranges for Application
                List<PriceRange> priceRanges = toRetrievePriceRanges.getPriceRanges();

                PriceRange toBeUsed = null;

                for(PriceRange pr:priceRanges){
                    if(newLicenseDetails.getJsonObject("License").getInt("totalRequestedUsers") >= pr.getMinAmountUsers() && newLicenseDetails.getJsonObject("License").getInt("totalRequestedUsers") <= pr.getMaxAmountUsers()) {
                        toBeUsed = pr;
                        break;
                    }
                }

                if(toBeUsed == null)
                    return Json.createObjectBuilder()
                            .add(ResponseCodes.SUCCESS.toString(), false)
                            .add(ResponseCodes.ERROR_CODE.toString(), 400)
                            .add(ResponseCodes.ERROR_MESSAGE.toString(), "There was an error finding a price range for the amount of users requested. Please contact the administrator.")
                            .build();

                //What we get from front end
                //Description, Start Date, End Date, Payment Type, Total Requested Users
                //To Be Generated
                //Next Invoice Date, Total License Fee, Available Users
                JsonObject licenseDetails = Json.createObjectBuilder()
                        .add("description", newLicenseDetails.getJsonObject("License").getString("description"))
                        .add("paymentType", newLicenseDetails.getJsonObject("License").getString("paymentType"))
                        .add("totalRequestedUsers", newLicenseDetails.getJsonObject("License").getInt("totalRequestedUsers"))
                        .add("totalAvailableUsers", toBeUsed.getMaxAmountUsers() - newLicenseDetails.getJsonObject("License").getInt("totalRequestedUsers"))
                        .add("licenseFee", newLicenseDetails.getJsonObject("License").getInt("totalRequestedUsers") * toBeUsed.getFinalPriceWithDiscount())
                        .add("startDate", newLicenseDetails.getJsonObject("License").getString("startDate"))
                        .add("endDate", newLicenseDetails.getJsonObject("License").getString("endDate"))
                        .add("nextInvoiceDate", (DateTime.now().getDayOfMonth()>=22)?""+DateTime.now().plusMonths(1):""+DateTime.now().plus(22 - DateTime.now().getDayOfMonth()))
                        .build();

                License l = Factory.buildLicense(licenseDetails);

                //persist the license
                licenseCrudService.createEntity(l);

                //update client with the new generated license
                License createdLicense = licenseCrudService.findEntityById(l.getId());

                if(createdLicense == null)
                    return Json.createObjectBuilder()
                            .add(ResponseCodes.SUCCESS.toString(), false)
                            .add(ResponseCodes.ERROR_CODE.toString(), 400)
                            .add(ResponseCodes.ERROR_MESSAGE.toString(), "There was an error creating the license. Please contact the administrator.")
                            .build();

                clientApplication.getLicenses().add(createdLicense);

                clientApplicationCrudService.updateEntity(clientApplication);

                Gson pojoParser = new Gson();

                JsonObject deleteLicenseLink = Json.createObjectBuilder()
                        .add(Link.REL.toString(), "REMOVE")
                        .add(Link.DATATYPE.toString(), MediaType.APPLICATION_JSON)
                        .add(Link.HREF.toString(), "http://server.url.com/license/"+created.getId().toString())
                        .add(Link.METHOD.toString(), "DELETE")
                        .build();

                JsonObject viewLicenseLink = Json.createObjectBuilder()
                        .add(Link.REL.toString(), "VIEW")
                        .add(Link.DATATYPE.toString(), MediaType.APPLICATION_JSON)
                        .add(Link.HREF.toString(), "http://server.url.com/license/"+created.getId().toString())
                        .add(Link.METHOD.toString(), "GET")
                        .build();

                JsonObject updateLicenseLink = Json.createObjectBuilder()
                        .add(Link.REL.toString(), "VIEW")
                        .add(Link.DATATYPE.toString(), MediaType.APPLICATION_JSON)
                        .add(Link.HREF.toString(), "http://server.url.com/license/"+created.getId().toString())
                        .add(Link.METHOD.toString(), "PUT")
                        .build();


                return Json.createObjectBuilder()
                        .add(ResponseCodes.SUCCESS.toString(), true)
                        .add(ResponseCodes.SUCCESS_CODE.toString(), 200)
                        .add(ResponseCodes.SUCCESS_MESSAGE.toString(), "The client was successfully registered.")
                        .add("License", pojoParser.toJson(created))
                        .add("Links", Json.createArrayBuilder()
                                .add(viewLicenseLink)
                                .add(deleteLicenseLink)
                                .add(updateLicenseLink)
                                .build())
                        .build();
            }else{
                //check if client already has a license for the application and let the user now
                return Json.createObjectBuilder()
                        .add(ResponseCodes.SUCCESS.toString(), false)
                        .add(ResponseCodes.ERROR_CODE.toString(), 400)
                        .add(ResponseCodes.ERROR_MESSAGE.toString(), "The client is already linked to the application and most likely has a license already please try updated instead.")
                        .build();
            }
        }catch (Exception e){
            log.log(Level.WARNING, "There was an error generating the license for client and application", e);
            return Json.createObjectBuilder()
                    .add(ResponseCodes.SUCCESS.toString(), false)
                    .add(ResponseCodes.ERROR_CODE.toString(), 400)
                    .add(ResponseCodes.ERROR_MESSAGE.toString(), "The license was not successfully generated.")
                    .build();
        }

    }

    @Override
    public JsonObject updateLicenseForClientAndApplication(JsonObject licenseDetails) {
        try{
            //check application id is valid
            if(!ObjectId.isValid(licenseDetails.getJsonObject("Application").getString("applicationId")))
                return Json.createObjectBuilder()
                        .add(ResponseCodes.SUCCESS.toString(), false)
                        .add(ResponseCodes.ERROR_CODE.toString(), 400)
                        .add(ResponseCodes.ERROR_MESSAGE.toString(), "The application was not found, the application id to was not a valid id.")
                        .build();

            Application toRetrievePriceRanges = applicationCrudService.findEntityById(new ObjectId(licenseDetails.getString("applicationId")));
            //check client id is valid
            if(!ObjectId.isValid(licenseDetails.getJsonObject("Client").getString("clientId")))
                return Json.createObjectBuilder()
                        .add(ResponseCodes.SUCCESS.toString(), false)
                        .add(ResponseCodes.ERROR_CODE.toString(), 400)
                        .add(ResponseCodes.ERROR_MESSAGE.toString(), "The client was not found, the client id to was not a valid id.")
                        .build();

            Client toUpdateLicenseFor = clientCrudService.findEntityById(new ObjectId(licenseDetails.getJsonObject("Client").getString("clientId")));

            //check if the client is already linked to the application
            ClientApplication clientApplication = clientApplicationCrudService.getClientApplicationByClientAndAppId(toUpdateLicenseFor.getId(),toRetrievePriceRanges.getId());

            if(clientApplication!=null){

                //get price ranges for Application
                List<PriceRange> priceRanges = toRetrievePriceRanges.getPriceRanges();

                PriceRange toBeUsed = null;

                for(PriceRange pr:priceRanges){
                    if(licenseDetails.getJsonObject("License").getInt("totalRequestedUsers") >= pr.getMinAmountUsers() && licenseDetails.getJsonObject("License").getInt("totalRequestedUsers") <= pr.getMaxAmountUsers()) {
                        toBeUsed = pr;
                        break;
                    }
                }

                if(toBeUsed == null)
                    return Json.createObjectBuilder()
                            .add(ResponseCodes.SUCCESS.toString(), false)
                            .add(ResponseCodes.ERROR_CODE.toString(), 400)
                            .add(ResponseCodes.ERROR_MESSAGE.toString(), "There was an error finding a price range for the amount of users requested. Please contact the administrator.")
                            .build();

                //What we get from front end
                //Description, Start Date, End Date, Payment Type, Total Requested Users
                //To Be Generated
                //Next Invoice Date, Total License Fee, Available Users
                JsonObject updatedLicenseDetails = Json.createObjectBuilder()
                        .add("description", licenseDetails.getJsonObject("License").getString("description"))
                        .add("paymentType", licenseDetails.getJsonObject("License").getString("paymentType"))
                        .add("totalRequestedUsers", licenseDetails.getJsonObject("License").getInt("totalRequestedUsers"))
                        .add("totalAvailableUsers", toBeUsed.getMaxAmountUsers() - licenseDetails.getJsonObject("License").getInt("totalRequestedUsers"))
                        .add("licenseFee", licenseDetails.getJsonObject("License").getInt("totalRequestedUsers") * toBeUsed.getFinalPriceWithDiscount())
                        .add("startDate", licenseDetails.getJsonObject("License").getString("startDate"))
                        .add("endDate", licenseDetails.getJsonObject("License").getString("endDate"))
                        .add("nextInvoiceDate", (DateTime.now().getDayOfMonth()>=22)?""+DateTime.now().plusMonths(1):""+DateTime.now().plus(22 - DateTime.now().getDayOfMonth()))
                        .build();

                License l = Factory.buildLicense(updatedLicenseDetails);

                //persist the license
                licenseCrudService.createEntity(l);

                //update client with the new generated license
                License createdLicense = licenseCrudService.findEntityById(l.getId());

                if(createdLicense == null)
                    return Json.createObjectBuilder()
                            .add(ResponseCodes.SUCCESS.toString(), false)
                            .add(ResponseCodes.ERROR_CODE.toString(), 400)
                            .add(ResponseCodes.ERROR_MESSAGE.toString(), "There was an error creating the license. Please contact the administrator.")
                            .build();

                clientApplication.getLicenses().add(createdLicense);

                clientApplicationCrudService.updateEntity(clientApplication);

                Gson pojoParser = new Gson();

                JsonObject deleteLicenseLink = Json.createObjectBuilder()
                        .add(Link.REL.toString(), "REMOVE")
                        .add(Link.DATATYPE.toString(), MediaType.APPLICATION_JSON)
                        .add(Link.HREF.toString(), "http://server.url.com/license/"+createdLicense.getId().toString())
                        .add(Link.METHOD.toString(), "DELETE")
                        .build();

                JsonObject viewLicenseLink = Json.createObjectBuilder()
                        .add(Link.REL.toString(), "VIEW")
                        .add(Link.DATATYPE.toString(), MediaType.APPLICATION_JSON)
                        .add(Link.HREF.toString(), "http://server.url.com/license/"+createdLicense.getId().toString())
                        .add(Link.METHOD.toString(), "GET")
                        .build();

                JsonObject updateLicenseLink = Json.createObjectBuilder()
                        .add(Link.REL.toString(), "VIEW")
                        .add(Link.DATATYPE.toString(), MediaType.APPLICATION_JSON)
                        .add(Link.HREF.toString(), "http://server.url.com/license/"+createdLicense.getId().toString())
                        .add(Link.METHOD.toString(), "PUT")
                        .build();


                return Json.createObjectBuilder()
                        .add(ResponseCodes.SUCCESS.toString(), true)
                        .add(ResponseCodes.SUCCESS_CODE.toString(), 200)
                        .add(ResponseCodes.SUCCESS_MESSAGE.toString(), "The license was successfully updated.")
                        .add("License", pojoParser.toJson(createdLicense))
                        .add("Links", Json.createArrayBuilder()
                                .add(viewLicenseLink)
                                .add(deleteLicenseLink)
                                .add(updateLicenseLink)
                                .build())
                        .build();
            }else{
                //check if client already has a license for the application and let the user now
                return Json.createObjectBuilder()
                        .add(ResponseCodes.SUCCESS.toString(), false)
                        .add(ResponseCodes.ERROR_CODE.toString(), 400)
                        .add(ResponseCodes.ERROR_MESSAGE.toString(), "The client is not linked to the application, please try linking the client to the application.")
                        .build();
            }
        }catch (Exception e){
            log.log(Level.WARNING, "There was an error updating the license for client and application", e);
            return Json.createObjectBuilder()
                    .add(ResponseCodes.SUCCESS.toString(), false)
                    .add(ResponseCodes.ERROR_CODE.toString(), 400)
                    .add(ResponseCodes.ERROR_MESSAGE.toString(), "The license was not successfully updating.")
                    .build();
        }
    }

    @Override
    public JsonObject viewLicenseForClientAndApplication(JsonObject licenseDetails) {
        try{
            //check application id is valid
            if(!ObjectId.isValid(licenseDetails.getJsonObject("Application").getString("applicationId")))
                return Json.createObjectBuilder()
                        .add(ResponseCodes.SUCCESS.toString(), false)
                        .add(ResponseCodes.ERROR_CODE.toString(), 400)
                        .add(ResponseCodes.ERROR_MESSAGE.toString(), "The application was not found, the application id to was not a valid id.")
                        .build();

            Application toViewLicense = applicationCrudService.findEntityById(new ObjectId(licenseDetails.getString("applicationId")));
            //check client id is valid
            if(!ObjectId.isValid(licenseDetails.getJsonObject("Application").getString("clientId")))
                return Json.createObjectBuilder()
                        .add(ResponseCodes.SUCCESS.toString(), false)
                        .add(ResponseCodes.ERROR_CODE.toString(), 400)
                        .add(ResponseCodes.ERROR_MESSAGE.toString(), "The client was not found, the client id to was not a valid id.")
                        .build();

            Client toViewLicenseFor = clientCrudService.findEntityById(new ObjectId(licenseDetails.getJsonObject("Client").getString("clientId")));


            //find the latest license for the client and application
            ClientApplication forClientAndApp = clientApplicationCrudService.getClientApplicationByClientAndAppId(toViewLicenseFor.getId(),toViewLicense.getId());


            Gson pojoParser = new Gson();

            JsonObject deleteLicenseLink = Json.createObjectBuilder()
                    .add(Link.REL.toString(), "REMOVE")
                    .add(Link.DATATYPE.toString(), MediaType.APPLICATION_JSON)
                    .add(Link.HREF.toString(), "http://server.url.com/license/" + forClientAndApp.getLicenses().get(0).getId().toString())
                    .add(Link.METHOD.toString(), "DELETE")
                    .build();

            JsonObject updateLicenseLink = Json.createObjectBuilder()
                    .add(Link.REL.toString(), "VIEW")
                    .add(Link.DATATYPE.toString(), MediaType.APPLICATION_JSON)
                    .add(Link.HREF.toString(), "http://server.url.com/license/" + forClientAndApp.getLicenses().get(0).getId().toString())
                    .add(Link.METHOD.toString(), "PUT")
                    .build();


            return Json.createObjectBuilder()
                    .add(ResponseCodes.SUCCESS.toString(), true)
                    .add(ResponseCodes.SUCCESS_CODE.toString(), 200)
                    .add(ResponseCodes.SUCCESS_MESSAGE.toString(), "The license for the client was successfully retrieved.")
                    .add("License", pojoParser.toJson(forClientAndApp.getLicenses().get(0)))
                    .add("Links", Json.createArrayBuilder()
                            .add(deleteLicenseLink)
                            .add(updateLicenseLink)
                            .build())
                    .build();

        }catch (Exception e){
            log.log(Level.WARNING, "There was an error retrieving the license for client and application", e);
            return Json.createObjectBuilder()
                    .add(ResponseCodes.SUCCESS.toString(), false)
                    .add(ResponseCodes.ERROR_CODE.toString(), 400)
                    .add(ResponseCodes.ERROR_MESSAGE.toString(), "The license was not successfully retrieved.")
                    .build();
        }
    }

    @Override
    public JsonObject viewAllLicenseForClient(ObjectId clientId) {
        List<ClientApplication> toViewLicensesFor = clientApplicationCrudService.getClientApplicationByClientId(clientId);

        JsonArrayBuilder allLicenses = Json.createArrayBuilder();
        for(ClientApplication clientApp: toViewLicensesFor ){
            for(License license: clientApp.getLicenses()){
                Gson pojoParser = new Gson();

                JsonObject deleteLicenseLink = Json.createObjectBuilder()
                        .add(Link.REL.toString(), "REMOVE")
                        .add(Link.DATATYPE.toString(), MediaType.APPLICATION_JSON)
                        .add(Link.HREF.toString(), "http://server.url.com/license/"+license.getId().toString())
                        .add(Link.METHOD.toString(), "DELETE")
                        .build();

                JsonObject viewLicenseLink = Json.createObjectBuilder()
                        .add(Link.REL.toString(), "VIEW")
                        .add(Link.DATATYPE.toString(), MediaType.APPLICATION_JSON)
                        .add(Link.HREF.toString(), "http://server.url.com/license/"+license.getId().toString())
                        .add(Link.METHOD.toString(), "GET")
                        .build();

                JsonObject updateLicenseLink = Json.createObjectBuilder()
                        .add(Link.REL.toString(), "VIEW")
                        .add(Link.DATATYPE.toString(), MediaType.APPLICATION_JSON)
                        .add(Link.HREF.toString(), "http://server.url.com/license/"+license.getId().toString())
                        .add(Link.METHOD.toString(), "PUT")
                        .build();

                allLicenses.add(Json.createObjectBuilder()
                                .add("License", pojoParser.toJson(license))
                                .add("Links", Json.createArrayBuilder()
                                                .add(deleteLicenseLink)
                                                .add(updateLicenseLink)
                                                .add(viewLicenseLink)
                                ).build()
                );
            }
        }
        JsonArray toReturn = allLicenses.build();

        if(toReturn.size()>0){
            return Json.createObjectBuilder()
                    .add(ResponseCodes.SUCCESS.toString(), true)
                    .add(ResponseCodes.SUCCESS_CODE.toString(), 200)
                    .add(ResponseCodes.SUCCESS_MESSAGE.toString(), "The client's licenses was successfully retrieved.")
                    .add("Licenses",toReturn)
                    .build();
        }else{
            return Json.createObjectBuilder()
                    .add(ResponseCodes.SUCCESS.toString(), false)
                    .add(ResponseCodes.ERROR_CODE.toString(), 200)
                    .add(ResponseCodes.ERROR_MESSAGE.toString(), "There was no licenses retrieved for the client.")
                    .build();
        }
    }

    @Override
    public JsonObject removeLicenseForClientAndApplication(JsonObject licenseDetails) {
        try{
            //check application id is valid
            if(!ObjectId.isValid(licenseDetails.getJsonObject("Application").getString("applicationId")))
                return Json.createObjectBuilder()
                        .add(ResponseCodes.SUCCESS.toString(), false)
                        .add(ResponseCodes.ERROR_CODE.toString(), 400)
                        .add(ResponseCodes.ERROR_MESSAGE.toString(), "The application was not found, the application id to was not a valid id.")
                        .build();

            Application toRemoveClientLicenseFrom = applicationCrudService.findEntityById(new ObjectId(licenseDetails.getString("applicationId")));
            //check client id is valid
            if(!ObjectId.isValid(licenseDetails.getJsonObject("Client").getString("clientId")))
                return Json.createObjectBuilder()
                        .add(ResponseCodes.SUCCESS.toString(), false)
                        .add(ResponseCodes.ERROR_CODE.toString(), 400)
                        .add(ResponseCodes.ERROR_MESSAGE.toString(), "The client was not found, the client id to was not a valid id.")
                        .build();

            Client toRemoveLicenseFrom = clientCrudService.findEntityById(new ObjectId(licenseDetails.getJsonObject("Client").getString("clientId")));
            //find the latest license for the client and application
            ClientApplication forClientAndApp = clientApplicationCrudService.getClientApplicationByClientAndAppId(toRemoveLicenseFrom.getId(),toRemoveClientLicenseFrom.getId());

            //remove all the licenses and the client application link
            forClientAndApp.getLicenses().forEach(
                    licenseCrudService::deleteEntity
            );

            //remove the client application link
            clientApplicationCrudService.deleteEntity(forClientAndApp);

            return Json.createObjectBuilder()
                    .add(ResponseCodes.SUCCESS.toString(), true)
                    .add(ResponseCodes.SUCCESS_CODE.toString(), 200)
                    .add(ResponseCodes.SUCCESS_MESSAGE.toString(), "The client licenses was successfully removed.")
                    .build();

        }catch (Exception e){
            log.log(Level.WARNING, "There was an error removing the licenses for the client and application", e);
            return Json.createObjectBuilder()
                    .add(ResponseCodes.SUCCESS.toString(), false)
                    .add(ResponseCodes.ERROR_CODE.toString(), 400)
                    .add(ResponseCodes.ERROR_MESSAGE.toString(), "The license was not successfully removed.")
                    .build();
        }
    }
}

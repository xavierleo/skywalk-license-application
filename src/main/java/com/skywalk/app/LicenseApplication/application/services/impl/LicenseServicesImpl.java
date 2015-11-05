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

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.ws.rs.core.MediaType;
import java.text.SimpleDateFormat;
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

            Application toRetrievePriceRanges = applicationCrudService.findEntityById(new ObjectId(newLicenseDetails.getJsonObject("Application").getString("applicationId")));
            //check client id is valid
            if(!ObjectId.isValid(newLicenseDetails.getJsonObject("Client").getString("clientId")))
                return Json.createObjectBuilder()
                        .add(ResponseCodes.SUCCESS.toString(), false)
                        .add(ResponseCodes.ERROR_CODE.toString(), 400)
                        .add(ResponseCodes.ERROR_MESSAGE.toString(), "The client was not found, the client id to was not a valid id.")
                        .build();

            Client toGenerateLicenseFor = clientCrudService.findEntityById(new ObjectId(newLicenseDetails.getJsonObject("Client").getString("clientId")));

            //link to the application
            //first create the bridge
            ClientApplication clientApplication = null;
            if (toGenerateLicenseFor == null){
                clientApplication = new ClientApplication();
                clientApplication.setId(new ObjectId());
                clientApplication.setApplication(toRetrievePriceRanges);
                clientApplication.setClient(toGenerateLicenseFor);

                clientApplicationCrudService.createEntity(clientApplication);
            }

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
                    .add("nextInvoiceDate", newLicenseDetails.getJsonObject("License").getString("nextInvoiceDate"))
                    .build();
            License l = Factory.buildLicense(licenseDetails);


            if(clientApplication == null) {
                ClientApplication exists = clientApplicationCrudService.getClientApplicationByClientAndAppId(toGenerateLicenseFor.getId(),toRetrievePriceRanges.getId());
                l.setClientApplication(exists);
                licenseCrudService.createEntity(l);
            }else{
                l.setClientApplication(clientApplication);
                licenseCrudService.createEntity(l);
            }

            //update client with the new generated license
            License createdLicense = licenseCrudService.findEntityById(l.getId());

            if(createdLicense == null)
                return Json.createObjectBuilder()
                        .add(ResponseCodes.SUCCESS.toString(), false)
                        .add(ResponseCodes.ERROR_CODE.toString(), 400)
                        .add(ResponseCodes.ERROR_MESSAGE.toString(), "There was an error creating the license. Please contact the administrator.")
                        .build();

            Gson pojoParser = new Gson();

            JsonObject deleteLicenseLink = Json.createObjectBuilder()
                    .add(Link.REL.toString(), "REMOVE")
                    .add(Link.DATATYPE.toString(), MediaType.APPLICATION_JSON)
                    .add(Link.HREF.toString(), "/api/license/"+createdLicense.getId().toString())
                    .add(Link.METHOD.toString(), "DELETE")
                    .build();

            JsonObject viewLicenseLink = Json.createObjectBuilder()
                    .add(Link.REL.toString(), "VIEW")
                    .add(Link.DATATYPE.toString(), MediaType.APPLICATION_JSON)
                    .add(Link.HREF.toString(), "/api/license/"+createdLicense.getId().toString())
                    .add(Link.METHOD.toString(), "GET")
                    .build();

            JsonObject updateLicenseLink = Json.createObjectBuilder()
                    .add(Link.REL.toString(), "VIEW")
                    .add(Link.DATATYPE.toString(), MediaType.APPLICATION_JSON)
                    .add(Link.HREF.toString(), "/api/license/")
                    .add(Link.METHOD.toString(), "PUT")
                    .build();


            return Json.createObjectBuilder()
                    .add(ResponseCodes.SUCCESS.toString(), true)
                    .add(ResponseCodes.SUCCESS_CODE.toString(), 200)
                    .add(ResponseCodes.SUCCESS_MESSAGE.toString(), "The license was successfully registered.")
                    .add("License", Json.createObjectBuilder()
                            .add("description", newLicenseDetails.getJsonObject("License").getString("description"))
                            .add("paymentType", newLicenseDetails.getJsonObject("License").getString("paymentType"))
                            .add("totalRequestedUsers", newLicenseDetails.getJsonObject("License").getInt("totalRequestedUsers"))
                            .add("totalAvailableUsers", toBeUsed.getMaxAmountUsers() - newLicenseDetails.getJsonObject("License").getInt("totalRequestedUsers"))
                            .add("licenseFee", newLicenseDetails.getJsonObject("License").getInt("totalRequestedUsers") * toBeUsed.getFinalPriceWithDiscount())
                            .add("startDate", newLicenseDetails.getJsonObject("License").getString("startDate"))
                            .add("endDate", newLicenseDetails.getJsonObject("License").getString("endDate"))
                            .add("nextInvoiceDate", newLicenseDetails.getJsonObject("License").getString("nextInvoiceDate"))
                            .build())
                    .add("Links", Json.createArrayBuilder()
                            .add(viewLicenseLink)
                            .add(deleteLicenseLink)
                            .add(updateLicenseLink)
                            .build())
                    .build();

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

            Application toRetrievePriceRanges = applicationCrudService.findEntityById(new ObjectId(licenseDetails.getJsonObject("Application").getString("applicationId")));
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

                SimpleDateFormat format = new SimpleDateFormat("yyyy/mm/dd");

                //update client with the new generated license
                License licenseToUpdate = licenseCrudService.findEntityById(new ObjectId(licenseDetails.getJsonObject("License").getString("licenseId")));
                licenseToUpdate.setDescription(licenseDetails.getJsonObject("License").getString("description"));
                licenseToUpdate.setPaymentType(licenseDetails.getJsonObject("License").getString("paymentType"));
                licenseToUpdate.setTotalRequestedUsers(licenseDetails.getJsonObject("License").getInt("totalRequestedUsers"));
                licenseToUpdate.setTotalAvailableUsers(toBeUsed.getMaxAmountUsers() - licenseDetails.getJsonObject("License").getInt("totalRequestedUsers"));
                licenseToUpdate.setLicenseFee(licenseDetails.getJsonObject("License").getInt("totalRequestedUsers") * toBeUsed.getFinalPriceWithDiscount());
                licenseToUpdate.setStartDate(format.parse(licenseDetails.getJsonObject("License").getString("startDate")));
                licenseToUpdate.setEndDate((format.parse(licenseDetails.getJsonObject("License").getString("endDate"))));
                licenseToUpdate.setInvoiceDate(format.parse(licenseDetails.getJsonObject("License").getString("nextInvoiceDate")));

                licenseCrudService.updateEntity(licenseToUpdate);

                Gson pojoParser = new Gson();

                JsonObject deleteLicenseLink = Json.createObjectBuilder()
                        .add(Link.REL.toString(), "REMOVE")
                        .add(Link.DATATYPE.toString(), MediaType.APPLICATION_JSON)
                        .add(Link.HREF.toString(), "/api/license/"+licenseToUpdate.getId().toString())
                        .add(Link.METHOD.toString(), "DELETE")
                        .build();

                JsonObject viewLicenseLink = Json.createObjectBuilder()
                        .add(Link.REL.toString(), "VIEW")
                        .add(Link.DATATYPE.toString(), MediaType.APPLICATION_JSON)
                        .add(Link.HREF.toString(), "/api/license/"+licenseToUpdate.getId().toString())
                        .add(Link.METHOD.toString(), "GET")
                        .build();

                JsonObject updateLicenseLink = Json.createObjectBuilder()
                        .add(Link.REL.toString(), "VIEW")
                        .add(Link.DATATYPE.toString(), MediaType.APPLICATION_JSON)
                        .add(Link.HREF.toString(), "/api/license/"+licenseToUpdate.getId().toString())
                        .add(Link.METHOD.toString(), "PUT")
                        .build();


                return Json.createObjectBuilder()
                        .add(ResponseCodes.SUCCESS.toString(), true)
                        .add(ResponseCodes.SUCCESS_CODE.toString(), 200)
                        .add(ResponseCodes.SUCCESS_MESSAGE.toString(), "The license was successfully updated.")
                        .add("License", pojoParser.toJson(licenseToUpdate))
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

            //check client id is valid
            if(!ObjectId.isValid(licenseDetails.getJsonObject("Client").getString("clientId")))
                return Json.createObjectBuilder()
                        .add(ResponseCodes.SUCCESS.toString(), false)
                        .add(ResponseCodes.ERROR_CODE.toString(), 400)
                        .add(ResponseCodes.ERROR_MESSAGE.toString(), "The client was not found, the client id to was not a valid id.")
                        .build();

            License toView = licenseCrudService.getLicenseForApplicationByClientAndAppId(new ObjectId(licenseDetails.getJsonObject("Client").getString("clientId")),new ObjectId(licenseDetails.getJsonObject("Application").getString("applicationId")));

            if(toView==null)
                return Json.createObjectBuilder()
                        .add(ResponseCodes.SUCCESS.toString(), false)
                        .add(ResponseCodes.ERROR_CODE.toString(), 400)
                        .add(ResponseCodes.ERROR_MESSAGE.toString(), "The license was not successfully retrieved. The license could not be found with the client and application id provided.")
                        .build();

            Gson pojoParser = new Gson();

            JsonObject deleteLicenseLink = Json.createObjectBuilder()
                    .add(Link.REL.toString(), "REMOVE")
                    .add(Link.DATATYPE.toString(), MediaType.APPLICATION_JSON)
                    .add(Link.HREF.toString(), "/api/license/" + toView.getId().toString())
                    .add(Link.METHOD.toString(), "DELETE")
                    .build();

            JsonObject updateLicenseLink = Json.createObjectBuilder()
                    .add(Link.REL.toString(), "UPDATE")
                    .add(Link.DATATYPE.toString(), MediaType.APPLICATION_JSON)
                    .add(Link.HREF.toString(), "/api/license/")
                    .add(Link.METHOD.toString(), "PUT")
                    .build();


            return Json.createObjectBuilder()
                    .add(ResponseCodes.SUCCESS.toString(), true)
                    .add(ResponseCodes.SUCCESS_CODE.toString(), 200)
                    .add(ResponseCodes.SUCCESS_MESSAGE.toString(), "The license for the client was successfully retrieved.")
                    .add("License", pojoParser.toJson(toView))
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

        List<License> toView = licenseCrudService.getLicenseByClientId(clientId);
        JsonArrayBuilder allLicenses = Json.createArrayBuilder();
        for(License license: toView){
            Gson pojoParser = new Gson();

            JsonObject deleteLicenseLink = Json.createObjectBuilder()
                    .add(Link.REL.toString(), "REMOVE")
                    .add(Link.DATATYPE.toString(), MediaType.APPLICATION_JSON)
                    .add(Link.HREF.toString(), "/api/license/" + license.getId().toString())
                    .add(Link.METHOD.toString(), "DELETE")
                    .build();

            JsonObject viewLicenseLink = Json.createObjectBuilder()
                    .add(Link.REL.toString(), "UPDATE")
                    .add(Link.DATATYPE.toString(), MediaType.APPLICATION_JSON)
                    .add(Link.HREF.toString(), "/api/license/")
                    .add(Link.METHOD.toString(), "PUT")
                    .build();

            JsonObject updateLicenseLink = Json.createObjectBuilder()
                    .add(Link.REL.toString(), "VIEW")
                    .add(Link.DATATYPE.toString(), MediaType.APPLICATION_JSON)
                    .add(Link.HREF.toString(), "/api/license/")
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
    public JsonObject removeLicenseForClientAndApplication(ObjectId licenseId) {
        try{
            licenseCrudService.deleteEntityById(licenseId);

            License removed = licenseCrudService.findEntityById(licenseId);

            if(removed != null)
                return Json.createObjectBuilder()
                        .add(ResponseCodes.SUCCESS.toString(), false)
                        .add(ResponseCodes.ERROR_CODE.toString(), 400)
                        .add(ResponseCodes.ERROR_MESSAGE.toString(), "The license was not successfully removed. Something went wrong after removal please contact administrator.")
                        .build();

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

    @Override
    public JsonObject checkClientAvailabilityForApplication(JsonObject clientDetails) {
        try{
            //check application id is valid
            if(!ObjectId.isValid(clientDetails.getJsonObject("Application").getString("applicationId")))
                return Json.createObjectBuilder()
                        .add(ResponseCodes.SUCCESS.toString(), false)
                        .add(ResponseCodes.ERROR_CODE.toString(), 400)
                        .add(ResponseCodes.ERROR_MESSAGE.toString(), "The application was not found, the application id to was not a valid id.")
                        .build();

            //check client id is valid
            if(!ObjectId.isValid(clientDetails.getJsonObject("Client").getString("clientId")))
                return Json.createObjectBuilder()
                        .add(ResponseCodes.SUCCESS.toString(), false)
                        .add(ResponseCodes.ERROR_CODE.toString(), 400)
                        .add(ResponseCodes.ERROR_MESSAGE.toString(), "The client was not found, the client id to was not a valid id.")
                        .build();

            License toView = licenseCrudService.getLicenseForApplicationByClientAndAppId(new ObjectId(clientDetails.getJsonObject("Client").getString("clientId")),new ObjectId(clientDetails.getJsonObject("Application").getString("applicationId")));

            if(toView==null)
                return Json.createObjectBuilder()
                        .add(ResponseCodes.SUCCESS.toString(), false)
                        .add(ResponseCodes.ERROR_CODE.toString(), 400)
                        .add(ResponseCodes.ERROR_MESSAGE.toString(), "The license was not successfully retrieved. The license could not be found with the client and application id provided.")
                        .build();

            return Json.createObjectBuilder()
                    .add(ResponseCodes.SUCCESS.toString(), true)
                    .add(ResponseCodes.SUCCESS_CODE.toString(), 200)
                    .add(ResponseCodes.SUCCESS_MESSAGE.toString(), "The license for the client was successfully retrieved.")
                    .add("License", Json.createObjectBuilder()
                        .add("description", toView.getDescription())
                        .add("startDate", "" + toView.getStartDate())
                        .add("endDate", "" + toView.getEndDate())
                        .add("nextInvoiceDate", "" + toView.getInvoiceDate())
                        .add("totalAvailableUsers", toView.getTotalAvailableUsers())
                        .add("totalRequestedUsers", toView.getTotalRequestedUsers())
                        .add("licenseFee", toView.getLicenseFee())
                        .add("paymentType", toView.getPaymentType())
                    )
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
}

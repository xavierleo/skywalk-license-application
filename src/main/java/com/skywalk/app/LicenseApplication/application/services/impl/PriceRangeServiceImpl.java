package main.java.com.skywalk.app.LicenseApplication.application.services.impl;

import com.google.gson.Gson;
import lombok.extern.java.Log;
import main.java.com.skywalk.app.LicenseApplication.application.services.PriceRangeServices;
import main.java.com.skywalk.app.LicenseApplication.application.utilities.Link;
import main.java.com.skywalk.app.LicenseApplication.application.utilities.ResponseCodes;
import main.java.com.skywalk.app.LicenseApplication.domain.crud.ApplicationCrudService;
import main.java.com.skywalk.app.LicenseApplication.domain.crud.PriceRangeCrudService;
import main.java.com.skywalk.app.LicenseApplication.domain.crud.impl.ApplicationCrudServiceImpl;
import main.java.com.skywalk.app.LicenseApplication.domain.crud.impl.PriceRangeCrudServiceImpl;
import main.java.com.skywalk.app.LicenseApplication.domain.factory.Factory;
import main.java.com.skywalk.app.LicenseApplication.domain.models.Application;
import main.java.com.skywalk.app.LicenseApplication.domain.models.PriceRange;
import org.bson.types.ObjectId;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.ws.rs.core.MediaType;
import java.util.logging.Level;

/**
 * Created by xavier on 2015/11/02.
 */
@Log
public class PriceRangeServiceImpl implements PriceRangeServices {
    private PriceRangeCrudService priceRangeCrudService;
    private ApplicationCrudService applicationCrudService;

    public PriceRangeServiceImpl(){
        priceRangeCrudService = new PriceRangeCrudServiceImpl();
        applicationCrudService = new ApplicationCrudServiceImpl();
    }

    @Override
    public JsonObject createPriceRange(JsonObject priceRange) {
        try {
            PriceRange newPriceRange = Factory.buildPriceRange(priceRange.getJsonObject("PriceRange"));

            Application app = applicationCrudService.findEntityById(new ObjectId(priceRange.getJsonObject("Application").getString("applicationId")));

            if(app == null)
                return Json.createObjectBuilder()
                        .add(ResponseCodes.SUCCESS.toString(), false)
                        .add(ResponseCodes.ERROR_CODE.toString(), 400)
                        .add(ResponseCodes.ERROR_MESSAGE.toString(), "The price range was not registered, the application id to link it to was not found.")
                        .build();

            priceRangeCrudService.createEntity(newPriceRange);

            //Add price range to application
            PriceRange dbPriceRange = priceRangeCrudService.findEntityById(newPriceRange.getId());

            if(dbPriceRange == null)
                return Json.createObjectBuilder()
                        .add(ResponseCodes.SUCCESS.toString(), false)
                        .add(ResponseCodes.ERROR_CODE.toString(), 400)
                        .add(ResponseCodes.ERROR_MESSAGE.toString(), "The price range was not found, the price range was not created successfully.")
                        .build();

            app.getPriceRanges().add(dbPriceRange);
            applicationCrudService.updateEntity(app);

            JsonObject deleteLink = Json.createObjectBuilder()
                    .add(Link.REL.toString(), "REMOVE")
                    .add(Link.DATATYPE.toString(), MediaType.APPLICATION_JSON)
                    .add(Link.HREF.toString(), "/api/pricerange/"+dbPriceRange.getId().toString())
                    .add(Link.METHOD.toString(), "DELETE")
                    .build();

            JsonObject viewLink = Json.createObjectBuilder()
                    .add(Link.REL.toString(), "VIEW")
                    .add(Link.DATATYPE.toString(), MediaType.APPLICATION_JSON)
                    .add(Link.HREF.toString(), "/api/pricerange/" + dbPriceRange.getId().toString())
                    .add(Link.METHOD.toString(), "GET")
                    .build();

            JsonObject updateLink = Json.createObjectBuilder()
                    .add(Link.REL.toString(), "UPDATE")
                    .add(Link.DATATYPE.toString(), MediaType.APPLICATION_JSON)
                    .add(Link.HREF.toString(), "/api/pricerange/")
                    .add(Link.METHOD.toString(), "PUT")
                    .build();

            return Json.createObjectBuilder()
                    .add(ResponseCodes.SUCCESS.toString(), true)
                    .add(ResponseCodes.SUCCESS_CODE.toString(), 200)
                    .add(ResponseCodes.SUCCESS_MESSAGE.toString(), "Price Range successfully created.")
                    .add("PriceRange", new Gson().toJson(dbPriceRange))
                    .add("Link", Json.createArrayBuilder()
                            .add(deleteLink)
                            .add(viewLink)
                            .add(updateLink)
                            .build())
                    .build();

        }catch(Exception e){
            log.log(Level.WARNING, "There was an error registering the price range", e);
            return Json.createObjectBuilder()
                    .add(ResponseCodes.SUCCESS.toString(), false)
                    .add(ResponseCodes.ERROR_CODE.toString(), 400)
                    .add(ResponseCodes.ERROR_MESSAGE.toString(), "The price range was not successfully registered.")
                    .build();
        }
    }

    @Override
    public JsonObject linkPriceRangeToApplication(JsonObject priceRange) {
        try {
            if(!ObjectId.isValid(priceRange.getJsonObject("PriceRange").getString("priceRangeId")))
                return Json.createObjectBuilder()
                        .add(ResponseCodes.SUCCESS.toString(), false)
                        .add(ResponseCodes.ERROR_CODE.toString(), 400)
                        .add(ResponseCodes.ERROR_MESSAGE.toString(), "The price range was not found, the id is not a valid id.")
                        .build();

            PriceRange toBeUpdated = priceRangeCrudService.findEntityById(new ObjectId(priceRange.getJsonObject("PriceRange").getString("priceRangeId")));

            if(!ObjectId.isValid(priceRange.getJsonObject("Application").getString("applicationId")))
                return Json.createObjectBuilder()
                        .add(ResponseCodes.SUCCESS.toString(), false)
                        .add(ResponseCodes.ERROR_CODE.toString(), 400)
                        .add(ResponseCodes.ERROR_MESSAGE.toString(), "The application was not found, the id is not a valid id.")
                        .build();

            Application toBeLinkedTo = applicationCrudService.findEntityById(new ObjectId(priceRange.getJsonObject("Application").getString("applicationId")));

            if(toBeUpdated == null)
                return Json.createObjectBuilder()
                        .add(ResponseCodes.SUCCESS.toString(), false)
                        .add(ResponseCodes.ERROR_CODE.toString(), 400)
                        .add(ResponseCodes.ERROR_MESSAGE.toString(), "The price range was not found, something went wrong.")
                        .build();

            if(toBeLinkedTo == null)
                return Json.createObjectBuilder()
                        .add(ResponseCodes.SUCCESS.toString(), false)
                        .add(ResponseCodes.ERROR_CODE.toString(), 400)
                        .add(ResponseCodes.ERROR_MESSAGE.toString(), "The application was not found, something went wrong.")
                        .build();

            toBeLinkedTo.getPriceRanges().add(toBeUpdated);

            JsonObject deleteLink = Json.createObjectBuilder()
                    .add(Link.REL.toString(), "REMOVE")
                    .add(Link.DATATYPE.toString(), MediaType.APPLICATION_JSON)
                    .add(Link.HREF.toString(), "/api/pricerange/"+toBeUpdated.getId().toString())
                    .add(Link.METHOD.toString(), "DELETE")
                    .build();

            JsonObject viewLink = Json.createObjectBuilder()
                    .add(Link.REL.toString(), "VIEW")
                    .add(Link.DATATYPE.toString(), MediaType.APPLICATION_JSON)
                    .add(Link.HREF.toString(), "/api/pricerange/"+toBeUpdated.getId().toString())
                    .add(Link.METHOD.toString(), "GET")
                    .build();

            applicationCrudService.updateEntity(toBeLinkedTo);

            return Json.createObjectBuilder()
                    .add(ResponseCodes.SUCCESS.toString(), true)
                    .add(ResponseCodes.SUCCESS_CODE.toString(), 200)
                    .add(ResponseCodes.SUCCESS_MESSAGE.toString(), "Price Range successfully linked to the application.")
                    .add("PriceRange", new Gson().toJson(toBeUpdated))
                    .add("Link", Json.createArrayBuilder()
                            .add(deleteLink)
                            .add(viewLink)
                            .build())
                    .build();
        }catch (Exception e){
            log.log(Level.WARNING, "There was an error linking the price range to the application", e);
            return Json.createObjectBuilder()
                    .add(ResponseCodes.SUCCESS.toString(), false)
                    .add(ResponseCodes.ERROR_CODE.toString(), 400)
                    .add(ResponseCodes.ERROR_MESSAGE.toString(), "Price range was not linked successfully to the application.")
                    .build();
        }
    }

    @Override
    public JsonObject editPriceRange(JsonObject priceRange) {
        try {
            if(!ObjectId.isValid(priceRange.getJsonObject("PriceRange").getString("priceRangeId")))
                return Json.createObjectBuilder()
                        .add(ResponseCodes.SUCCESS.toString(), false)
                        .add(ResponseCodes.ERROR_CODE.toString(), 400)
                        .add(ResponseCodes.ERROR_MESSAGE.toString(), "The price range was not found, the id is not a valid id.")
                        .build();

            PriceRange toBeUpdated = priceRangeCrudService.findEntityById(new ObjectId(priceRange.getJsonObject("PriceRange").getString("priceRangeId")));

            if(toBeUpdated == null)
                return Json.createObjectBuilder()
                        .add(ResponseCodes.SUCCESS.toString(), false)
                        .add(ResponseCodes.ERROR_CODE.toString(), 400)
                        .add(ResponseCodes.ERROR_MESSAGE.toString(), "The price range was not found, something went wrong.")
                        .build();

            JsonObject deleteLink = Json.createObjectBuilder()
                    .add(Link.REL.toString(), "REMOVE")
                    .add(Link.DATATYPE.toString(), MediaType.APPLICATION_JSON)
                    .add(Link.HREF.toString(), "/api/pricerange/"+toBeUpdated.getId().toString())
                    .add(Link.METHOD.toString(), "DELETE")
                    .build();

            JsonObject viewLink = Json.createObjectBuilder()
                    .add(Link.REL.toString(), "VIEW")
                    .add(Link.DATATYPE.toString(), MediaType.APPLICATION_JSON)
                    .add(Link.HREF.toString(), "/api/pricerange/"+toBeUpdated.getId().toString())
                    .add(Link.METHOD.toString(), "GET")
                    .build();

            toBeUpdated.setDiscountPercentage(Double.valueOf(priceRange.getJsonObject("PriceRange").getString("discountPercentage")));
            toBeUpdated.setMinAmountUsers(priceRange.getJsonObject("PriceRange").getInt("minAmountUsers"));
            toBeUpdated.setMaxAmountUsers(priceRange.getJsonObject("PriceRange").getInt("maxAmountUsers"));
            toBeUpdated.setPriceForUserInRange(Double.valueOf(priceRange.getJsonObject("PriceRange").getString("priceForUsersInRange")));
            toBeUpdated.setFinalPriceWithDiscount(
                    (Double.valueOf(priceRange.getJsonObject("PriceRange").getString("priceForUsersInRange")) * (Double.valueOf(priceRange.getJsonObject("PriceRange").getString("discountPercentage"))/100)) + Double.valueOf(priceRange.getJsonObject("PriceRange").getString("priceForUsersInRange")));

            priceRangeCrudService.updateEntity(toBeUpdated);

            return Json.createObjectBuilder()
                    .add(ResponseCodes.SUCCESS.toString(), true)
                    .add(ResponseCodes.SUCCESS_CODE.toString(), 200)
                    .add(ResponseCodes.SUCCESS_MESSAGE.toString(), "Price Range successfully updated.")
                    .add("PriceRange", new Gson().toJson(toBeUpdated))
                    .add("Link", Json.createArrayBuilder()
                            .add(deleteLink)
                            .add(viewLink)
                            .build())
                    .build();
        }catch (Exception e){
            log.log(Level.WARNING, "There was an error retrieving the price range", e);
            return Json.createObjectBuilder()
                    .add(ResponseCodes.SUCCESS.toString(), false)
                    .add(ResponseCodes.ERROR_CODE.toString(), 400)
                    .add(ResponseCodes.ERROR_MESSAGE.toString(), "Price range was not updated successfully.")
                    .build();
        }
    }

    @Override
    public JsonObject deletePriceRange(JsonObject priceRange) {
        try{
            if(!ObjectId.isValid(priceRange.getJsonObject("PriceRange").getString("priceRangeId")))
                return Json.createObjectBuilder()
                        .add(ResponseCodes.SUCCESS.toString(), false)
                        .add(ResponseCodes.ERROR_CODE.toString(), 400)
                        .add(ResponseCodes.ERROR_MESSAGE.toString(), "The price range was not found, the id is not a valid id.")
                        .build();

            PriceRange toRemove = priceRangeCrudService.findEntityById(new ObjectId(priceRange.getJsonObject("PriceRange").getString("priceRangeId")));

            if(toRemove == null)
                return Json.createObjectBuilder()
                        .add(ResponseCodes.SUCCESS.toString(), false)
                        .add(ResponseCodes.ERROR_CODE.toString(), 400)
                        .add(ResponseCodes.ERROR_MESSAGE.toString(), "The price range was not removed. Something went wrong while looking for the Application")
                        .build();

            //remove reference to application
            if(!ObjectId.isValid(priceRange.getJsonObject("Application").getString("applicationId")))
                return Json.createObjectBuilder()
                        .add(ResponseCodes.SUCCESS.toString(), false)
                        .add(ResponseCodes.ERROR_CODE.toString(), 400)
                        .add(ResponseCodes.ERROR_MESSAGE.toString(), "The application was not found, the id is not a valid id.")
                        .build();

            Application toRemoveReferenceFrom = applicationCrudService.findEntityById(new ObjectId(priceRange.getJsonObject("Application").getString("applicationId")));

            for(PriceRange pr: toRemoveReferenceFrom.getPriceRanges()){
                if(pr.getId().equals(toRemove.getId())){
                    toRemoveReferenceFrom.getPriceRanges().remove(pr);
                    applicationCrudService.updateEntity(toRemoveReferenceFrom);
                    break;
                }
            }

            priceRangeCrudService.deleteEntity(toRemove);

            return Json.createObjectBuilder()
                    .add(ResponseCodes.SUCCESS.toString(), true)
                    .add(ResponseCodes.SUCCESS_CODE.toString(), 200)
                    .add(ResponseCodes.SUCCESS_MESSAGE.toString(), "The price range was successfully removed.")
                    .build();

        }catch (Exception e){
            log.log(Level.WARNING, "There was an error retrieving the price range", e);
            return Json.createObjectBuilder()
                    .add(ResponseCodes.SUCCESS.toString(), false)
                    .add(ResponseCodes.ERROR_CODE.toString(), 400)
                    .add(ResponseCodes.ERROR_MESSAGE.toString(), "The price range was not successfully removed.")
                    .build();
        }
    }

    @Override
    public JsonObject viewPriceRange(String priceRangeId) {
        try{
            if(!ObjectId.isValid(priceRangeId))
                return Json.createObjectBuilder()
                        .add(ResponseCodes.SUCCESS.toString(), false)
                        .add(ResponseCodes.ERROR_CODE.toString(), 400)
                        .add(ResponseCodes.ERROR_MESSAGE.toString(), "The price range was not found, the id is not a valid id.")
                        .build();

            PriceRange toView = priceRangeCrudService.findEntityById(new ObjectId(priceRangeId));

            if(toView == null)
                return Json.createObjectBuilder()
                        .add(ResponseCodes.SUCCESS.toString(), false)
                        .add(ResponseCodes.ERROR_CODE.toString(), 400)
                        .add(ResponseCodes.ERROR_MESSAGE.toString(), "The price range was not found. Something went wrong while looking for the Application")
                        .build();

            JsonObject deleteLink = Json.createObjectBuilder()
                    .add(Link.REL.toString(), "REMOVE")
                    .add(Link.DATATYPE.toString(), MediaType.APPLICATION_JSON)
                    .add(Link.HREF.toString(), "/api/pricerange/"+toView.getId().toString())
                    .add(Link.METHOD.toString(), "DELETE")
                    .build();

            JsonObject editLink = Json.createObjectBuilder()
                    .add(Link.REL.toString(), "EDIT")
                    .add(Link.DATATYPE.toString(), MediaType.APPLICATION_JSON)
                    .add(Link.HREF.toString(), "/api/pricerange/")
                    .add(Link.METHOD.toString(), "PUT")
                    .build();

            return Json.createObjectBuilder()
                    .add(ResponseCodes.SUCCESS.toString(), true)
                    .add(ResponseCodes.SUCCESS_CODE.toString(), 200)
                    .add(ResponseCodes.SUCCESS_MESSAGE.toString(), "The price range was successfully retrieved.")
                    .add("PriceRange", new Gson().toJson(toView))
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
                    .add(ResponseCodes.ERROR_MESSAGE.toString(), "The price range was not successfully retrieved.")
                    .build();
        }
    }

    @Override
    public JsonObject viewAllPriceRangesForApplication(String applicationId) {
        try{
            if(!ObjectId.isValid(applicationId))
                return Json.createObjectBuilder()
                        .add(ResponseCodes.SUCCESS.toString(), false)
                        .add(ResponseCodes.ERROR_CODE.toString(), 400)
                        .add(ResponseCodes.ERROR_MESSAGE.toString(), "The price range was not found, the id is not a valid id.")
                        .build();

            Application toViewRangeFor = applicationCrudService.findEntityById(new ObjectId(applicationId));

            if(toViewRangeFor == null)
                return Json.createObjectBuilder()
                        .add(ResponseCodes.SUCCESS.toString(), false)
                        .add(ResponseCodes.ERROR_CODE.toString(), 400)
                        .add(ResponseCodes.ERROR_MESSAGE.toString(), "The price range was not found. Something went wrong while looking for the Application")
                        .build();

            JsonArrayBuilder builder = Json.createArrayBuilder();

            for(PriceRange pr: toViewRangeFor.getPriceRanges()){
                JsonObject deleteLink = Json.createObjectBuilder()
                        .add(Link.REL.toString(), "REMOVE")
                        .add(Link.DATATYPE.toString(), MediaType.APPLICATION_JSON)
                        .add(Link.HREF.toString(), "/api/pricerange/"+toViewRangeFor.getId().toString())
                        .add(Link.METHOD.toString(), "DELETE")
                        .build();

                JsonObject editLink = Json.createObjectBuilder()
                        .add(Link.REL.toString(), "EDIT")
                        .add(Link.DATATYPE.toString(), MediaType.APPLICATION_JSON)
                        .add(Link.HREF.toString(), "/api/pricerange/")
                        .add(Link.METHOD.toString(), "PUT")
                        .build();

                JsonObject viewLink = Json.createObjectBuilder()
                        .add(Link.REL.toString(), "VIEW")
                        .add(Link.DATATYPE.toString(), MediaType.APPLICATION_JSON)
                        .add(Link.HREF.toString(), "/api/pricerange/"+toViewRangeFor.getId().toString())
                        .add(Link.METHOD.toString(), "GET")
                        .build();

                builder.add(Json.createObjectBuilder()
                                .add("PriceRange", new Gson().toJson(pr))
                                .add("Link", Json.createArrayBuilder()
                                        .add(deleteLink)
                                        .add(editLink)
                                        .add(viewLink)
                                        .build())
                                .build()
                );
            }


            return Json.createObjectBuilder()
                    .add(ResponseCodes.SUCCESS.toString(), true)
                    .add(ResponseCodes.SUCCESS_CODE.toString(), 200)
                    .add(ResponseCodes.SUCCESS_MESSAGE.toString(), "The price ranges was successfully retrieved.")
                    .add("PriceRanges", builder.build())
                    .build();

        }catch (Exception e){
            log.log(Level.WARNING, "There was an error retrieving the price ranges", e);
            return Json.createObjectBuilder()
                    .add(ResponseCodes.SUCCESS.toString(), false)
                    .add(ResponseCodes.ERROR_CODE.toString(), 400)
                    .add(ResponseCodes.ERROR_MESSAGE.toString(), "The price ranges was not successfully retrieved.")
                    .build();
        }
    }
}

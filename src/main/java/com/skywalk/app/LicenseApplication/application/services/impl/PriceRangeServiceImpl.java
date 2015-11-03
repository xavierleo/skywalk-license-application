package main.java.com.skywalk.app.LicenseApplication.application.services.impl;

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
            PriceRange newPriceRange = Factory.buildPriceRange(priceRange);

            if(!ObjectId.isValid(priceRange.getString("priceRangeId")))
                return Json.createObjectBuilder()
                        .add(ResponseCodes.SUCCESS.toString(), false)
                        .add(ResponseCodes.ERROR_CODE.toString(), 400)
                        .add(ResponseCodes.ERROR_MESSAGE.toString(), "The price range was not created.")
                        .build();

            Application app = applicationCrudService.findEntityById(new ObjectId(priceRange.getString("applicationId")));

            if(app == null)
                return Json.createObjectBuilder()
                        .add(ResponseCodes.SUCCESS.toString(), false)
                        .add(ResponseCodes.ERROR_CODE.toString(), 400)
                        .add(ResponseCodes.ERROR_MESSAGE.toString(), "The price range was not registered, the application id to link it to was not found.")
                        .build();

            //Check if user exists
            PriceRange u = priceRangeCrudService.findEntityById(newPriceRange.getId());
            if(u != null)
                return Json.createObjectBuilder()
                        .add(ResponseCodes.SUCCESS.toString(), false)
                        .add(ResponseCodes.ERROR_CODE.toString(), 400)
                        .add(ResponseCodes.ERROR_MESSAGE.toString(), "The user was not registered, the user is already registered.")
                        .build();

            //Create user
            priceRangeCrudService.createEntity(newPriceRange);

            //Add price range to application
            PriceRange dbPriceRange = priceRangeCrudService.findEntityById(newPriceRange.getId());
            if(dbPriceRange == null)
                return Json.createObjectBuilder()
                        .add(ResponseCodes.SUCCESS.toString(), false)
                        .add(ResponseCodes.ERROR_CODE.toString(), 400)
                        .add(ResponseCodes.ERROR_MESSAGE.toString(), "The user was not found, the user was not created successfully.")
                        .build();

            app.getPriceRanges().add(dbPriceRange);
            applicationCrudService.updateEntity(app);

            JsonObject deleteLink = Json.createObjectBuilder()
                    .add(Link.REL.toString(), "REMOVE")
                    .add(Link.DATATYPE.toString(), MediaType.APPLICATION_JSON)
                    .add(Link.HREF.toString(), "http://server.url.com/pricerange/"+priceRange.getString("priceRangeId"))
                    .add(Link.METHOD.toString(), "DELETE")
                    .build();

            JsonObject viewLink = Json.createObjectBuilder()
                    .add(Link.REL.toString(), "VIEW")
                    .add(Link.DATATYPE.toString(), MediaType.APPLICATION_JSON)
                    .add(Link.HREF.toString(), "http://server.url.com/pricerange/"+priceRange.getString("priceRangeId"))
                    .add(Link.METHOD.toString(), "GET")
                    .build();

            return Json.createObjectBuilder()
                    .add(ResponseCodes.SUCCESS.toString(), true)
                    .add(ResponseCodes.SUCCESS_CODE.toString(), 200)
                    .add(ResponseCodes.SUCCESS_MESSAGE.toString(), "Price Range successfully updated.")
                    .add("Link", Json.createArrayBuilder()
                            .add(deleteLink)
                            .add(viewLink)
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
    public JsonObject editPriceRange(JsonObject priceRange) {
        try {
            if(!ObjectId.isValid(priceRange.getString("priceRangeId")))
                return Json.createObjectBuilder()
                        .add(ResponseCodes.SUCCESS.toString(), false)
                        .add(ResponseCodes.ERROR_CODE.toString(), 400)
                        .add(ResponseCodes.ERROR_MESSAGE.toString(), "The price range was not found, the id is not a valid id.")
                        .build();

            PriceRange toBeUpdated = priceRangeCrudService.findEntityById(new ObjectId(priceRange.getString("priceRangeId")));

            if(toBeUpdated == null)
                return Json.createObjectBuilder()
                        .add(ResponseCodes.SUCCESS.toString(), false)
                        .add(ResponseCodes.ERROR_CODE.toString(), 400)
                        .add(ResponseCodes.ERROR_MESSAGE.toString(), "The price range was not found, something went wrong.")
                        .build();

            JsonObject deleteLink = Json.createObjectBuilder()
                    .add(Link.REL.toString(), "REMOVE")
                    .add(Link.DATATYPE.toString(), MediaType.APPLICATION_JSON)
                    .add(Link.HREF.toString(), "http://server.url.com/pricerange/"+priceRange.getString("priceRangeId"))
                    .add(Link.METHOD.toString(), "DELETE")
                    .build();

            JsonObject viewLink = Json.createObjectBuilder()
                    .add(Link.REL.toString(), "VIEW")
                    .add(Link.DATATYPE.toString(), MediaType.APPLICATION_JSON)
                    .add(Link.HREF.toString(), "http://server.url.com/pricerange/"+priceRange.getString("priceRangeId"))
                    .add(Link.METHOD.toString(), "GET")
                    .build();

            priceRangeCrudService.updateEntity(toBeUpdated);
            return Json.createObjectBuilder()
                    .add(ResponseCodes.SUCCESS.toString(), true)
                    .add(ResponseCodes.SUCCESS_CODE.toString(), 200)
                    .add(ResponseCodes.SUCCESS_MESSAGE.toString(), "Price Range successfully updated.")
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
            PriceRange toRemove = priceRangeCrudService.findEntityById(new ObjectId(priceRange.getString("priceRangeId")));

            if(toRemove == null)
                return Json.createObjectBuilder()
                        .add(ResponseCodes.SUCCESS.toString(), false)
                        .add(ResponseCodes.ERROR_CODE.toString(), 400)
                        .add(ResponseCodes.ERROR_MESSAGE.toString(), "The price range was not removed. Something went wrong while looking for the Application")
                        .build();

            priceRangeCrudService.deleteEntity(toRemove);

            Application removed = applicationCrudService.findEntityById(new ObjectId(priceRange.getString("priceRangeId")));

            if(removed!=null)
                return Json.createObjectBuilder()
                        .add(ResponseCodes.SUCCESS.toString(), false)
                        .add(ResponseCodes.ERROR_CODE.toString(), 400)
                        .add(ResponseCodes.ERROR_MESSAGE.toString(), "The price range was not successfully removed.")
                        .build();

            return Json.createObjectBuilder()
                    .add(ResponseCodes.SUCCESS.toString(), true)
                    .add(ResponseCodes.SUCCESS_CODE.toString(), 200)
                    .add(ResponseCodes.SUCCESS_MESSAGE.toString(), "The price range and its price ranges was successfully removed.")
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
                    .add(Link.HREF.toString(), "http://server.url.com/pricerange/"+toView.getId().toString())
                    .add(Link.METHOD.toString(), "DELETE")
                    .build();

            JsonObject editLink = Json.createObjectBuilder()
                    .add(Link.REL.toString(), "EDIT")
                    .add(Link.DATATYPE.toString(), MediaType.APPLICATION_JSON)
                    .add(Link.HREF.toString(), "http://server.url.com/pricerange/"+toView.getId().toString())
                    .add(Link.METHOD.toString(), "PUT")
                    .build();

            return Json.createObjectBuilder()
                    .add(ResponseCodes.SUCCESS.toString(), true)
                    .add(ResponseCodes.SUCCESS_CODE.toString(), 200)
                    .add(ResponseCodes.SUCCESS_MESSAGE.toString(), "The price range was successfully retrieved.")
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
}
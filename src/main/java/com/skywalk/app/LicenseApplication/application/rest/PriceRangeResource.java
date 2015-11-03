package main.java.com.skywalk.app.LicenseApplication.application.rest;

import lombok.extern.java.Log;
import main.java.com.skywalk.app.LicenseApplication.application.services.PriceRangeServices;

import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.StringReader;

/**
 * Created by ironhulk on 03/11/2015.
 */
@Log
@Path("pricerange")
public class PriceRangeResource {

    @Inject private PriceRangeServices priceRangeServices;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response newPriceRange(String priceRange){
        JsonReader reader = Json.createReader(new StringReader(priceRange));
        JsonObject output = priceRangeServices.createPriceRange(reader.readObject());

        return Response.ok(output.toString()).build();
    }

}

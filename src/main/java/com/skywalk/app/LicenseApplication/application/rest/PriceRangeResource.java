package main.java.com.skywalk.app.LicenseApplication.application.rest;

import lombok.extern.java.Log;
import main.java.com.skywalk.app.LicenseApplication.application.services.PriceRangeServices;

import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.ws.rs.*;
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

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updatePriceRange(String priceRange){
        JsonReader reader = Json.createReader(new StringReader(priceRange));
        JsonObject output = priceRangeServices.editPriceRange(reader.readObject());

        return Response.ok(output.toString()).build();
    }

    @GET
    @Path("{id}")
    @Produces
    public Response viewPriceRange(@PathParam("id") String id){
        JsonObject output = priceRangeServices.viewPriceRange(id);

        return Response.ok(output.toString()).build();
    }

    @GET
    @Path("application/{id}")
    @Produces
    public Response viewPriceRangesForApplication(@PathParam("id") String id){
        JsonObject output = priceRangeServices.viewAllPriceRangesForApplication(id);

        return Response.ok(output.toString()).build();
    }

    @DELETE
    @Produces
    public Response deletePriceRange(String priceRange){
        JsonReader reader = Json.createReader(new StringReader(priceRange));
        JsonObject output = priceRangeServices.deletePriceRange(reader.readObject());

        return Response.ok(output.toString()).build();
    }
}

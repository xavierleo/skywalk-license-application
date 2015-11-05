package main.java.com.skywalk.app.LicenseApplication.application.rest;

import lombok.extern.java.Log;
import main.java.com.skywalk.app.LicenseApplication.application.services.LicenseServices;
import org.bson.types.ObjectId;

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
@Path("license")
public class LicenseResource {

    @Inject private LicenseServices licenseServices;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response generateLicense(String license){
        JsonReader reader = Json.createReader(new StringReader(license));
        JsonObject output = licenseServices.generateLicenseForClientAndApplication(reader.readObject());

        return Response.ok(output.toString()).build();
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateLicense(String license){
        JsonReader reader = Json.createReader(new StringReader(license));
        JsonObject output = licenseServices.updateLicenseForClientAndApplication(reader.readObject());

        return Response.ok(output.toString()).build();
    }

    @PUT
    @Path("view")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response viewLicenseByClientAndApplicationId(String license){
        JsonReader reader = Json.createReader(new StringReader(license));
        JsonObject output = licenseServices.viewLicenseForClientAndApplication(reader.readObject());

        return Response.ok(output.toString()).build();
    }

    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response viewAllForClient(@PathParam("id") String id){
        JsonObject output = licenseServices.viewAllLicenseForClient(new ObjectId(id));

        return Response.ok(output.toString()).build();
    }

    @DELETE
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response removeLicense(@PathParam("id") String id){
        JsonObject output = licenseServices.removeLicenseForClientAndApplication(new ObjectId(id));

        return Response.ok(output.toString()).build();
    }

    @PUT
    @Path("remaining")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response viewRemainingUsersForClientAndApplication(String license){
        JsonReader reader = Json.createReader(new StringReader(license));
        JsonObject output = licenseServices.checkClientAvailabilityForApplication(reader.readObject());

        return Response.ok(output.toString()).build();
    }
}

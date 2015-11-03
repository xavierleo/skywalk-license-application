package main.java.com.skywalk.app.LicenseApplication.application.rest;

import lombok.extern.java.Log;
import main.java.com.skywalk.app.LicenseApplication.application.services.ApplicationServices;
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
@Path("application")
public class ApplicationResource {

    @Inject private ApplicationServices applicationServices;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response registerApplication(String application){
        log.info("ApplicationResource entered at /registerApplication");

        JsonReader reader = Json.createReader(new StringReader(application));
        JsonObject output = applicationServices.registerApplication(reader.readObject());

        return Response.ok(output.toString()).build();
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateApplication(String application){
        log.info("ApplicationResource entered at /updateApplication");

        JsonReader reader = Json.createReader(new StringReader(application));
        JsonObject output = applicationServices.editApplication(reader.readObject());

        return Response.ok(output.toString()).build();
    }

    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response viewApplication(@PathParam("id") String id){
        log.info("ApplicationResource entered at /viewApplication");
        JsonObject output = applicationServices.viewApplication(new ObjectId(id));

        return Response.ok(output.toString()).build();
    }

    @GET
    @Path("company/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response viewAllApplications(@PathParam("id") String id){
        log.info("ApplicationResource entered at /viewAllApplications");
        JsonObject output = applicationServices.viewAllApplication(new ObjectId(id));

        return Response.ok(output.toString()).build();
    }
}

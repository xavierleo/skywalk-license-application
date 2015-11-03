package main.java.com.skywalk.app.LicenseApplication.application.rest;

import lombok.extern.java.Log;
import main.java.com.skywalk.app.LicenseApplication.application.services.CompanyService;

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
@Path("company")
@Log
public class CompanyResource {

    @Inject
    private CompanyService companyService;

    @GET
    @Path("exists")
    @Produces(MediaType.APPLICATION_JSON)
    public Response companyExists(){
        log.info("CompanyResource entered at /exists");

        JsonObject output = companyService.companyExists();

        return Response.ok(output.toString()).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response newCompany(String company){
        log.info("CompanyResource entered at /newCompany");

        JsonReader reader = Json.createReader(new StringReader(company));
        JsonObject output = companyService.createCompany(reader.readObject());

        return Response.ok(output.toString()).build();
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateCompany(String company){
        log.info("CompanyResource entered at /updateCompany");

        JsonReader reader = Json.createReader(new StringReader(company));
        JsonObject output = companyService.editCompany(reader.readObject());

        return Response.ok(output.toString()).build();
    }

    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response viewCompany(@PathParam("id") String id){
        log.info("CompanyResource entered at /viewCompany");
        JsonObject output = companyService.viewCompany(id);

        return Response.ok(output.toString()).build();
    }
}

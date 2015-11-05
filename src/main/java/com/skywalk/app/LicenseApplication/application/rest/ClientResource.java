package main.java.com.skywalk.app.LicenseApplication.application.rest;

import lombok.extern.java.Log;
import main.java.com.skywalk.app.LicenseApplication.application.services.ClientServices;
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
@Path("client")
public class ClientResource {

    @Inject private ClientServices clientServices;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response newClient(String client){
        log.info("ClientResource entered at /newClient");
        JsonReader reader = Json.createReader(new StringReader(client));
        JsonObject output = clientServices.registerClient(reader.readObject());

        return Response.ok(output.toString()).build();
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateClient(String client){
        log.info("ClientResource entered at /updateClient");
        JsonReader reader = Json.createReader(new StringReader(client));
        JsonObject output = clientServices.editClient(reader.readObject());

        return Response.ok(output.toString()).build();
    }

    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response viewClient(@PathParam("id") String id){
         log.info("ClientResource entered at /viewClient");
        JsonObject output = clientServices.viewClient(new ObjectId(id));

        return Response.ok(output.toString()).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response viewAllClients(){
        log.info("ClientResource entered at /viewAllClients");
        JsonObject output = clientServices.viewAllClients();

        return Response.ok(output.toString()).build();
    }

    @DELETE
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response removeClient(String client){
        log.info("ClientResource entered at /removeClient");
        JsonReader reader = Json.createReader(new StringReader(client));
        JsonObject output = clientServices.removeClient(reader.readObject());

        return Response.ok(output.toString()).build();
    }

    @GET
    @Path("query/{name}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response searchClientByName(@PathParam("name") String name){
        log.info("ClientResource entered at /searchClientByName");
        JsonObject output = clientServices.searchClientByName(name);

        return Response.ok(output.toString()).build();
    }
}

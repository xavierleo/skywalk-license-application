package main.java.com.skywalk.app.LicenseApplication.application.rest;

import lombok.extern.java.Log;
import main.java.com.skywalk.app.LicenseApplication.application.services.UserService;

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
@Path("user")
public class UserResource {

    @Inject private UserService userService;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response newUser(String user){
        log.info("UserResource entered at /newUser");

        JsonReader reader = Json.createReader(new StringReader(user));
        JsonObject output = userService.registerUser(reader.readObject());

        return Response.ok(output.toString()).build();
    }

    @PUT
    @Path("login")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response loginUser(String user){
        log.info("UserResource entered at /loginUser");

        JsonReader reader = Json.createReader(new StringReader(user));
        JsonObject output = userService.loginUser(reader.readObject());

        return Response.ok(output.toString()).build();
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateUser(String user){
        log.info("UserResource entered at /updateUser");

        JsonReader reader = Json.createReader(new StringReader(user));
        JsonObject output = userService.editUser(reader.readObject());

        return Response.ok(output.toString()).build();
    }

    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response viewUser(@PathParam("id") String id){
        log.info("UserResource entered at /viewUser");
        JsonObject output = userService.viewUser(id);

        return Response.ok(output.toString()).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response viewAllUser(){
        log.info("UserResource entered at /viewAllUser");
        JsonObject output = userService.viewAllUsers();

        return Response.ok(output.toString()).build();
    }

    @DELETE
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response removeUser(@PathParam("id") String id){
        log.info("UserResource entered at /removeUser");
        JsonObject output = userService.deleteUser(id);

        return Response.ok(output.toString()).build();
    }
}

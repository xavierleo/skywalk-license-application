package main.java.com.skywalk.app.LicenseApplication.application.services.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.java.Log;
import main.java.com.skywalk.app.LicenseApplication.application.services.ClientServices;
import main.java.com.skywalk.app.LicenseApplication.application.utilities.Link;
import main.java.com.skywalk.app.LicenseApplication.application.utilities.ResponseCodes;
import main.java.com.skywalk.app.LicenseApplication.domain.crud.ClientCrudService;
import main.java.com.skywalk.app.LicenseApplication.domain.crud.impl.ClientCrudServiceImpl;
import main.java.com.skywalk.app.LicenseApplication.domain.factory.Factory;
import main.java.com.skywalk.app.LicenseApplication.domain.models.*;
import org.bson.types.ObjectId;

import javax.json.Json;
import javax.json.JsonObject;
import javax.ws.rs.core.MediaType;
import java.util.logging.Level;

/**
 * Created by ironhulk on 31/10/2015.
 */
@Log
public class ClientServicesImpl implements ClientServices {

    private ClientCrudService clientCrudService;

    public ClientServicesImpl() {
        clientCrudService = new ClientCrudServiceImpl();
    }

    @Override
    public JsonObject registerClient(JsonObject clientParam) {
        try{
            Client client = Factory.buildClient(clientParam);

            //check if the company is already registered on the application
            if(clientCrudService.findEntityByProperty("name",client.getName()) == null)
                return Json.createObjectBuilder()
                        .add(ResponseCodes.SUCCESS.toString(), false)
                        .add(ResponseCodes.ERROR_CODE.toString(), 400)
                        .add(ResponseCodes.ERROR_MESSAGE.toString(), "The client with that name was already registered.")
                        .build();

            clientCrudService.createEntity(client);

            Client created = clientCrudService.findEntityById(client.getId());

            if(created == null)
                return Json.createObjectBuilder()
                        .add(ResponseCodes.SUCCESS.toString(), false)
                        .add(ResponseCodes.ERROR_CODE.toString(), 400)
                        .add(ResponseCodes.ERROR_MESSAGE.toString(), "The client was not successfully registered. Something went wrong while registering.")
                        .build();

            JsonObject clientLink = Json.createObjectBuilder()
                    .add(Link.REL.toString(), "VIEW")
                    .add(Link.DATATYPE.toString(), MediaType.APPLICATION_JSON)
                    .add(Link.HREF.toString(), "http://server.url.com/client/"+created.getId().toString())
                    .add(Link.METHOD.toString(), "GET")
                    .build();

            return Json.createObjectBuilder()
                    .add(ResponseCodes.SUCCESS.toString(), true)
                    .add(ResponseCodes.SUCCESS_CODE.toString(), 200)
                    .add(ResponseCodes.SUCCESS_MESSAGE.toString(), "The client was successfully registered.")
                    .add("Client", Json.createObjectBuilder()
                            .add("ID",created.getId().toString()))
                    .add("Link", clientLink)
                    .build();

        }catch (Exception e){
            log.log(Level.WARNING, "There was an error registering the client", e);
            return Json.createObjectBuilder()
                    .add(ResponseCodes.SUCCESS.toString(), false)
                    .add(ResponseCodes.ERROR_CODE.toString(), 400)
                    .add(ResponseCodes.ERROR_MESSAGE.toString(), "The client was not successfully registered. Please contact administrator.")
                    .build();
        }
    }

    @Override
    public JsonObject editClient(JsonObject client) {
        try{
            if(!ObjectId.isValid(client.getString("clientId")))
                return Json.createObjectBuilder()
                        .add(ResponseCodes.SUCCESS.toString(), false)
                        .add(ResponseCodes.ERROR_CODE.toString(), 400)
                        .add(ResponseCodes.ERROR_MESSAGE.toString(), "The client was not found, the id is not a valid id.")
                        .build();

            Client toEdit = clientCrudService.findEntityById(new ObjectId(client.getString("clientId")));

            if(toEdit == null)
                return Json.createObjectBuilder()
                        .add(ResponseCodes.SUCCESS.toString(), false)
                        .add(ResponseCodes.ERROR_CODE.toString(), 400)
                        .add(ResponseCodes.ERROR_MESSAGE.toString(), "The Client was not found. Something went wrong while looking for the Client")
                        .build();

            JsonObject deleteLink = Json.createObjectBuilder()
                    .add(Link.REL.toString(), "REMOVE")
                    .add(Link.DATATYPE.toString(), MediaType.APPLICATION_JSON)
                    .add(Link.HREF.toString(), "http://server.url.com/client/"+client.getString("clientId"))
                    .add(Link.METHOD.toString(), "DELETE")
                    .build();

            JsonObject viewLink = Json.createObjectBuilder()
                    .add(Link.REL.toString(), "VIEW")
                    .add(Link.DATATYPE.toString(), MediaType.APPLICATION_JSON)
                    .add(Link.HREF.toString(), "http://server.url.com/client/"+client.getString("clientId"))
                    .add(Link.METHOD.toString(), "GET")
                    .build();

            //embedabbles
            ContactDetails c = new ContactDetails(
                    client.getJsonObject("Client").getJsonObject("ContactDetails").getString("email"),
                    client.getJsonObject("Client").getJsonObject("ContactDetails").getString("mobile"));

            Liason l = new Liason(
                    client.getJsonObject("Client").getJsonObject("Liason").getString("name"),
                    client.getJsonObject("Client").getJsonObject("Liason").getString("liasonEmail"),
                    client.getJsonObject("Client").getJsonObject("Liason").getString("liasonNumber")
            );
            //update the application
            toEdit.setName(client.getJsonObject("Client").getString("name"));
            toEdit.setSize(client.getJsonObject("Client").getString("size"));
            toEdit.setContactDetails(c);
            toEdit.setLiason(l);

            clientCrudService.updateEntity(toEdit);


            return Json.createObjectBuilder()
                    .add(ResponseCodes.SUCCESS.toString(), true)
                    .add(ResponseCodes.SUCCESS_CODE.toString(), 200)
                    .add(ResponseCodes.SUCCESS_MESSAGE.toString(), "The client was successfully updated.")
                    .add("Link", Json.createArrayBuilder()
                            .add(deleteLink)
                            .add(viewLink)
                            .build())
                    .build();

        }catch (Exception e){
            log.log(Level.WARNING, "There was an error retrieving the client", e);
            return Json.createObjectBuilder()
                    .add(ResponseCodes.SUCCESS.toString(), false)
                    .add(ResponseCodes.ERROR_CODE.toString(), 400)
                    .add(ResponseCodes.ERROR_MESSAGE.toString(), "The client was not successfully retrieved.")
                    .build();
        }
    }

    @Override
    public JsonObject viewClient(ObjectId id) {
        try{
            Client toView = clientCrudService.findEntityById(id);

            if(toView == null)
                return Json.createObjectBuilder()
                        .add(ResponseCodes.SUCCESS.toString(), false)
                        .add(ResponseCodes.ERROR_CODE.toString(), 400)
                        .add(ResponseCodes.ERROR_MESSAGE.toString(), "The client was not found. Something went wrong while looking for the client.")
                        .build();

            JsonObject deleteLink = Json.createObjectBuilder()
                    .add(Link.REL.toString(), "REMOVE")
                    .add(Link.DATATYPE.toString(), MediaType.APPLICATION_JSON)
                    .add(Link.HREF.toString(), "http://server.url.com/client/"+id.toString())
                    .add(Link.METHOD.toString(), "DELETE")
                    .build();

            JsonObject editLink = Json.createObjectBuilder()
                    .add(Link.REL.toString(), "EDIT")
                    .add(Link.DATATYPE.toString(), MediaType.APPLICATION_JSON)
                    .add(Link.HREF.toString(), "http://server.url.com/client/"+id.toString())
                    .add(Link.METHOD.toString(), "PUT")
                    .build();

            Gson pojoParser = new Gson();

            return Json.createObjectBuilder()
                    .add(ResponseCodes.SUCCESS.toString(), true)
                    .add(ResponseCodes.SUCCESS_CODE.toString(), 200)
                    .add(ResponseCodes.SUCCESS_MESSAGE.toString(), "The client was successfully retrieved.")
                    .add("Client", pojoParser.toJson(toView))
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
                    .add(ResponseCodes.ERROR_MESSAGE.toString(), "The application was not successfully retrieved.")
                    .build();
        }
    }

    @Override
    public JsonObject viewAllClients(ObjectId companyId) {
        return null;
    }

    @Override
    public JsonObject removeClient(ObjectId clientId) {
        return null;
    }
}

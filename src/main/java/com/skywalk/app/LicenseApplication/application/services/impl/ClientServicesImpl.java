package main.java.com.skywalk.app.LicenseApplication.application.services.impl;

import com.google.gson.Gson;
import lombok.extern.java.Log;
import main.java.com.skywalk.app.LicenseApplication.application.services.ClientServices;
import main.java.com.skywalk.app.LicenseApplication.application.utilities.Link;
import main.java.com.skywalk.app.LicenseApplication.application.utilities.ResponseCodes;
import main.java.com.skywalk.app.LicenseApplication.domain.crud.ApplicationCrudService;
import main.java.com.skywalk.app.LicenseApplication.domain.crud.ClientApplicationCrudService;
import main.java.com.skywalk.app.LicenseApplication.domain.crud.ClientCrudService;
import main.java.com.skywalk.app.LicenseApplication.domain.crud.LicenseCrudService;
import main.java.com.skywalk.app.LicenseApplication.domain.crud.impl.ApplicationCrudServiceImpl;
import main.java.com.skywalk.app.LicenseApplication.domain.crud.impl.ClientApplicationCrudServiceImpl;
import main.java.com.skywalk.app.LicenseApplication.domain.crud.impl.ClientCrudServiceImpl;
import main.java.com.skywalk.app.LicenseApplication.domain.crud.impl.LicenseCrudServiceImpl;
import main.java.com.skywalk.app.LicenseApplication.domain.factory.Factory;
import main.java.com.skywalk.app.LicenseApplication.domain.models.*;
import org.bson.types.ObjectId;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.logging.Level;

/**
 * Created by ironhulk on 31/10/2015.
 */
@Log
public class ClientServicesImpl implements ClientServices {

    private ClientCrudService clientCrudService;
    private LicenseCrudService licenseCrudService;
    private ClientApplicationCrudService clientApplicationCrudService;
    private ApplicationCrudService applicationCrudService;

    public ClientServicesImpl() {
        clientCrudService = new ClientCrudServiceImpl();
        licenseCrudService = new LicenseCrudServiceImpl();
        clientApplicationCrudService = new ClientApplicationCrudServiceImpl();
        applicationCrudService = new ApplicationCrudServiceImpl();
    }

    @Override
    public JsonObject registerClient(JsonObject clientParam) {
        Gson pojoParser = new Gson();

        try{
            Client client = Factory.buildClient(clientParam.getJsonObject("Client"));

            ContactDetails c = new ContactDetails(
                    clientParam.getJsonObject("Client").getJsonObject("ContactDetails").getString("email"),
                    clientParam.getJsonObject("Client").getJsonObject("ContactDetails").getString("mobile"));

            Liason l = new Liason(
                    clientParam.getJsonObject("Client").getJsonObject("Liason").getString("name"),
                    clientParam.getJsonObject("Client").getJsonObject("Liason").getString("liasonEmail"),
                    clientParam.getJsonObject("Client").getJsonObject("Liason").getString("liasonNumber")
            );

            client.setLiason(l);
            client.setContactDetails(c);

            //check if the client is already registered on the application
            if(clientCrudService.findEntityByProperty("name",client.getName()) != null)
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

            Application toEdit = applicationCrudService.findEntityById(new ObjectId(clientParam.getJsonObject("Application").getString("applicationId")));

            if(toEdit == null)
                return Json.createObjectBuilder()
                        .add(ResponseCodes.SUCCESS.toString(), false)
                        .add(ResponseCodes.ERROR_CODE.toString(), 400)
                        .add(ResponseCodes.ERROR_MESSAGE.toString(), "The application was not found. Something went wrong while looking for the Application")
                        .build();

            //first create the bridge
            ClientApplication clientApplication = new ClientApplication();
            clientApplication.setId(new ObjectId());
            clientApplication.setApplication(toEdit);
            clientApplication.setClient(created);

            clientApplicationCrudService.createEntity(clientApplication);

            ClientApplication createdLink = clientApplicationCrudService.findEntityById(clientApplication.getId());

            if(createdLink == null)
                return Json.createObjectBuilder()
                        .add(ResponseCodes.SUCCESS.toString(), false)
                        .add(ResponseCodes.ERROR_CODE.toString(), 400)
                        .add(ResponseCodes.ERROR_MESSAGE.toString(), "The application was not successfully linked to the client.")
                        .build();

//            toEdit.getClientApplications().add(createdLink);
//            //update the application
//            applicationCrudService.updateEntity(toEdit);
//
//            created.getClientApplications().add(createdLink);
            //update the client
//            clientCrudService.updateEntity(created);

            JsonObject clientViewLink = Json.createObjectBuilder()
                    .add(Link.REL.toString(), "VIEW")
                    .add(Link.DATATYPE.toString(), MediaType.APPLICATION_JSON)
                    .add(Link.HREF.toString(), "/api/client/"+created.getId().toString())
                    .add(Link.METHOD.toString(), "GET")
                    .build();

            JsonObject clientUpdateLink = Json.createObjectBuilder()
                    .add(Link.REL.toString(), "UPDATE")
                    .add(Link.DATATYPE.toString(), MediaType.APPLICATION_JSON)
                    .add(Link.HREF.toString(), "/api/client/")
                    .add(Link.METHOD.toString(), "PUT")
                    .build();

            JsonObject clientDeleteLink = Json.createObjectBuilder()
                    .add(Link.REL.toString(), "REMOVE")
                    .add(Link.DATATYPE.toString(), MediaType.APPLICATION_JSON)
                    .add(Link.HREF.toString(), "/api/client/"+created.getId().toString())
                    .add(Link.METHOD.toString(), "DELETE")
                    .build();

            return Json.createObjectBuilder()
                    .add(ResponseCodes.SUCCESS.toString(), true)
                    .add(ResponseCodes.SUCCESS_CODE.toString(), 200)
                    .add(ResponseCodes.SUCCESS_MESSAGE.toString(), "The client was successfully registered.")
                    .add("Client", pojoParser.toJson(created))
                    .add("Link", Json.createArrayBuilder()
                        .add(clientViewLink)
                        .add(clientDeleteLink)
                        .add(clientUpdateLink)
                        .build()
                    )
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
            if(!ObjectId.isValid(client.getJsonObject("Client").getString("clientId")))
                return Json.createObjectBuilder()
                        .add(ResponseCodes.SUCCESS.toString(), false)
                        .add(ResponseCodes.ERROR_CODE.toString(), 400)
                        .add(ResponseCodes.ERROR_MESSAGE.toString(), "The client was not found, the id is not a valid id.")
                        .build();

            Client toEdit = clientCrudService.findEntityById(new ObjectId(client.getJsonObject("Client").getString("clientId")));

            if(toEdit == null)
                return Json.createObjectBuilder()
                        .add(ResponseCodes.SUCCESS.toString(), false)
                        .add(ResponseCodes.ERROR_CODE.toString(), 400)
                        .add(ResponseCodes.ERROR_MESSAGE.toString(), "The Client was not found. Something went wrong while looking for the Client")
                        .build();

            JsonObject deleteLink = Json.createObjectBuilder()
                    .add(Link.REL.toString(), "REMOVE")
                    .add(Link.DATATYPE.toString(), MediaType.APPLICATION_JSON)
                    .add(Link.HREF.toString(), "/api/client/"+toEdit.getId().toString())
                    .add(Link.METHOD.toString(), "DELETE")
                    .build();

            JsonObject viewLink = Json.createObjectBuilder()
                    .add(Link.REL.toString(), "VIEW")
                    .add(Link.DATATYPE.toString(), MediaType.APPLICATION_JSON)
                    .add(Link.HREF.toString(), "/api/client/"+toEdit.getId().toString())
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

            Gson pojoParser = new Gson();

            return Json.createObjectBuilder()
                    .add(ResponseCodes.SUCCESS.toString(), true)
                    .add(ResponseCodes.SUCCESS_CODE.toString(), 200)
                    .add(ResponseCodes.SUCCESS_MESSAGE.toString(), "The client was successfully updated.")
                    .add("Client",pojoParser.toJson(toEdit))
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
                    .add(Link.HREF.toString(), "/api/client/"+id.toString())
                    .add(Link.METHOD.toString(), "DELETE")
                    .build();

            JsonObject editLink = Json.createObjectBuilder()
                    .add(Link.REL.toString(), "EDIT")
                    .add(Link.DATATYPE.toString(), MediaType.APPLICATION_JSON)
                    .add(Link.HREF.toString(), "/api/client/")
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
    public JsonObject viewAllClients() {
        try{
            List<Client> toView = clientCrudService.getAllEntities();

            if(toView == null && toView.size() <= 0)
                return Json.createObjectBuilder()
                        .add(ResponseCodes.SUCCESS.toString(), false)
                        .add(ResponseCodes.ERROR_CODE.toString(), 400)
                        .add(ResponseCodes.ERROR_MESSAGE.toString(), "The clients were not found. Something went wrong while looking for the client.")
                        .build();

            Gson pojoParser = new Gson();

            JsonArrayBuilder builder = Json.createArrayBuilder();
            for(Client c: toView){
                JsonObject deleteLink = Json.createObjectBuilder()
                        .add(Link.REL.toString(), "REMOVE")
                        .add(Link.DATATYPE.toString(), MediaType.APPLICATION_JSON)
                        .add(Link.HREF.toString(), "/api/client/"+c.getId().toString())
                        .add(Link.METHOD.toString(), "DELETE")
                        .build();

                JsonObject editLink = Json.createObjectBuilder()
                        .add(Link.REL.toString(), "EDIT")
                        .add(Link.DATATYPE.toString(), MediaType.APPLICATION_JSON)
                        .add(Link.HREF.toString(), "/api/client/"+c.getId().toString())
                        .add(Link.METHOD.toString(), "PUT")
                        .build();

                JsonObject viewLink = Json.createObjectBuilder()
                        .add(Link.REL.toString(), "VIEW")
                        .add(Link.DATATYPE.toString(), MediaType.APPLICATION_JSON)
                        .add(Link.HREF.toString(), "/api/client/"+c.getId().toString())
                        .add(Link.METHOD.toString(), "GET")
                        .build();

                builder.add(Json.createObjectBuilder()
                        .add("Client", pojoParser.toJson(toView))
                        .add("Link", Json.createArrayBuilder()
                                .add(deleteLink)
                                .add(editLink)
                                .add(viewLink)
                                .build())
                        .build());
            }

            return Json.createObjectBuilder()
                    .add(ResponseCodes.SUCCESS.toString(), true)
                    .add(ResponseCodes.SUCCESS_CODE.toString(), 200)
                    .add(ResponseCodes.SUCCESS_MESSAGE.toString(), "The clients was successfully retrieved.")
                    .add("Clients", builder.build())
                    .build();

        }catch (Exception e){
            log.log(Level.WARNING, "There was an error retrieving the clients", e);
            return Json.createObjectBuilder()
                    .add(ResponseCodes.SUCCESS.toString(), false)
                    .add(ResponseCodes.ERROR_CODE.toString(), 400)
                    .add(ResponseCodes.ERROR_MESSAGE.toString(), "The clients was not successfully retrieved.")
                    .build();
        }
    }

    @Override
    public JsonObject removeClient(JsonObject clientToRemove) {
        try{

            if(!ObjectId.isValid(clientToRemove.getJsonObject("Client").getString("clientId")))
                return Json.createObjectBuilder()
                        .add(ResponseCodes.SUCCESS.toString(), false)
                        .add(ResponseCodes.ERROR_CODE.toString(), 400)
                        .add(ResponseCodes.ERROR_MESSAGE.toString(), "The client was not removed. The id provided is invalid.")
                        .build();

            Client toRemove = clientCrudService.findEntityById(new ObjectId(clientToRemove.getJsonObject("Client").getString("clientId")));

            if(!ObjectId.isValid(clientToRemove.getJsonObject("Application").getString("applicationId")))
                return Json.createObjectBuilder()
                        .add(ResponseCodes.SUCCESS.toString(), false)
                        .add(ResponseCodes.ERROR_CODE.toString(), 400)
                        .add(ResponseCodes.ERROR_MESSAGE.toString(), "The client was not removed. The id provided is invalid.")
                        .build();



            //find the application to remove the reference from
//            Application toRemoveReferenceFrom = applicationCrudService.findEntityById(new ObjectId(clientToRemove.getJsonObject("Application").getString("applicationId")));

            //find the clientApplication to remove
            ClientApplication clientApplication = clientApplicationCrudService.getClientApplicationByClientAndAppId(new ObjectId(clientToRemove.getJsonObject("Client").getString("clientId")),new ObjectId(clientToRemove.getJsonObject("Application").getString("applicationId")));

            //remove clientApplication reference from application
//            toRemoveReferenceFrom.getClientApplications().remove(clientApplication);

            //update application with removed clientApplication Reference
//            applicationCrudService.updateEntity(toRemoveReferenceFrom);

            //remove the link between application and client
            clientApplicationCrudService.deleteEntity(clientApplication);

            //remove the actual client object
            clientCrudService.deleteEntity(toRemove);

            Client removed = clientCrudService.findEntityById(toRemove.getId());

            if(removed!=null)
                return Json.createObjectBuilder()
                        .add(ResponseCodes.SUCCESS.toString(), false)
                        .add(ResponseCodes.ERROR_CODE.toString(), 400)
                        .add(ResponseCodes.ERROR_MESSAGE.toString(), "The client was not successfully removed.")
                        .build();

            return Json.createObjectBuilder()
                    .add(ResponseCodes.SUCCESS.toString(), true)
                    .add(ResponseCodes.SUCCESS_CODE.toString(), 200)
                    .add(ResponseCodes.SUCCESS_MESSAGE.toString(), "The client and its licenses was successfully removed.")
                    .build();

        }catch (Exception e){
            log.log(Level.WARNING, "There was an error retrieving the client", e);
            return Json.createObjectBuilder()
                    .add(ResponseCodes.SUCCESS.toString(), false)
                    .add(ResponseCodes.ERROR_CODE.toString(), 400)
                    .add(ResponseCodes.ERROR_MESSAGE.toString(), "The client was not successfully removed.")
                    .build();
        }
    }

    @Override
    public JsonObject assignClientToApplication(JsonObject application) {
        try{
            //find the application to register the client against
            if(!ObjectId.isValid(application.getString("clientId")))
                return Json.createObjectBuilder()
                        .add(ResponseCodes.SUCCESS.toString(), false)
                        .add(ResponseCodes.ERROR_CODE.toString(), 400)
                        .add(ResponseCodes.ERROR_MESSAGE.toString(), "The client was not found, the id is not a valid id.")
                        .build();

            if(!ObjectId.isValid(application.getString("applicationId")))
                return Json.createObjectBuilder()
                        .add(ResponseCodes.SUCCESS.toString(), false)
                        .add(ResponseCodes.ERROR_CODE.toString(), 400)
                        .add(ResponseCodes.ERROR_MESSAGE.toString(), "The application was not found, the id is not a valid id.")
                        .build();

            Client clientToAssign = clientCrudService.findEntityById(new ObjectId(application.getString("clientId")));

            if(clientToAssign == null)
                return Json.createObjectBuilder()
                        .add(ResponseCodes.SUCCESS.toString(), false)
                        .add(ResponseCodes.ERROR_CODE.toString(), 400)
                        .add(ResponseCodes.ERROR_MESSAGE.toString(), "The client was not found with the id provided")
                        .build();

            Application toEdit = applicationCrudService.findEntityById(new ObjectId(application.getString("applicationId")));

            if(toEdit == null)
                return Json.createObjectBuilder()
                        .add(ResponseCodes.SUCCESS.toString(), false)
                        .add(ResponseCodes.ERROR_CODE.toString(), 400)
                        .add(ResponseCodes.ERROR_MESSAGE.toString(), "The application was not found. Something went wrong while looking for the Application")
                        .build();

            JsonObject deleteApplicationLink = Json.createObjectBuilder()
                    .add(Link.REL.toString(), "REMOVE")
                    .add(Link.DATATYPE.toString(), MediaType.APPLICATION_JSON)
                    .add(Link.HREF.toString(), "http://server.url.com/application/"+application.getString("applicationId"))
                    .add(Link.METHOD.toString(), "DELETE")
                    .build();

            JsonObject viewApplicationLink = Json.createObjectBuilder()
                    .add(Link.REL.toString(), "VIEW")
                    .add(Link.DATATYPE.toString(), MediaType.APPLICATION_JSON)
                    .add(Link.HREF.toString(), "http://server.url.com/application/"+application.getString("applicationId"))
                    .add(Link.METHOD.toString(), "GET")
                    .build();

            JsonObject deleteClientLink = Json.createObjectBuilder()
                    .add(Link.REL.toString(), "REMOVE")
                    .add(Link.DATATYPE.toString(), MediaType.APPLICATION_JSON)
                    .add(Link.HREF.toString(), "http://server.url.com/client/"+application.getString("clientId"))
                    .add(Link.METHOD.toString(), "DELETE")
                    .build();

            JsonObject viewClientLink = Json.createObjectBuilder()
                    .add(Link.REL.toString(), "VIEW")
                    .add(Link.DATATYPE.toString(), MediaType.APPLICATION_JSON)
                    .add(Link.HREF.toString(), "http://server.url.com/client/"+application.getString("clientId"))
                    .add(Link.METHOD.toString(), "GET")
                    .build();

            JsonObject updateClientLink = Json.createObjectBuilder()
                    .add(Link.REL.toString(), "VIEW")
                    .add(Link.DATATYPE.toString(), MediaType.APPLICATION_JSON)
                    .add(Link.HREF.toString(), "http://server.url.com/client/"+application.getString("clientId"))
                    .add(Link.METHOD.toString(), "PUT")
                    .build();

            //first create the bridge
            ClientApplication clientApplication = new ClientApplication();
            clientApplication.setId(new ObjectId());
            clientApplication.setApplication(toEdit);
            clientApplication.setClient(clientToAssign);

            clientApplicationCrudService.createEntity(clientApplication);

            ClientApplication created = clientApplicationCrudService.findEntityById(clientApplication.getId());

            if(created == null)
                return Json.createObjectBuilder()
                        .add(ResponseCodes.SUCCESS.toString(), false)
                        .add(ResponseCodes.ERROR_CODE.toString(), 400)
                        .add(ResponseCodes.ERROR_MESSAGE.toString(), "The application was not successfully linked to the client.")
                        .build();

            toEdit.getClientApplications().add(created);
            //update the application
            applicationCrudService.updateEntity(toEdit);

            clientToAssign.getClientApplications().add(created);
            //update the client
            clientCrudService.updateEntity(clientToAssign);

            return Json.createObjectBuilder()
                    .add(ResponseCodes.SUCCESS.toString(), true)
                    .add(ResponseCodes.SUCCESS_CODE.toString(), 200)
                    .add(ResponseCodes.SUCCESS_MESSAGE.toString(), "The client was successfully added to the application.")
                    .add("Application", Json.createObjectBuilder()
                            .add("Link", Json.createArrayBuilder()
                                    .add(deleteApplicationLink)
                                    .add(viewApplicationLink)
                                    .build())
                            .build())
                    .add("Client", Json.createObjectBuilder()
                            .add("Link", Json.createArrayBuilder()
                                    .add(deleteClientLink)
                                    .add(updateClientLink)
                                    .add(viewClientLink)
                                    .build())
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

}

package main.java.com.skywalk.app.LicenseApplication.application.services.impl;

import lombok.extern.java.Log;
import main.java.com.skywalk.app.LicenseApplication.application.services.UserService;
import main.java.com.skywalk.app.LicenseApplication.application.utilities.Link;
import main.java.com.skywalk.app.LicenseApplication.application.utilities.ResponseCodes;
import main.java.com.skywalk.app.LicenseApplication.domain.crud.CompanyCrudService;
import main.java.com.skywalk.app.LicenseApplication.domain.crud.UserCrudService;
import main.java.com.skywalk.app.LicenseApplication.domain.crud.impl.CompanyCrudServiceImpl;
import main.java.com.skywalk.app.LicenseApplication.domain.crud.impl.UserCrudServiceImpl;
import main.java.com.skywalk.app.LicenseApplication.domain.factory.Factory;
import main.java.com.skywalk.app.LicenseApplication.domain.models.Company;
import main.java.com.skywalk.app.LicenseApplication.domain.models.User;
import org.bson.types.ObjectId;
import org.jasypt.util.password.StrongPasswordEncryptor;

import javax.json.Json;
import javax.json.JsonObject;
import javax.ws.rs.core.MediaType;
import java.util.logging.Level;

/**
 * Created by xavier on 2015/10/28.
 */
@Log
public class UserServiceImpl implements UserService {

    private UserCrudService userCrudService;
    private CompanyCrudService companyCrudService;

    public UserServiceImpl(){
        userCrudService = new UserCrudServiceImpl();
        companyCrudService = new CompanyCrudServiceImpl();
    }

    @Override
    public JsonObject registerUser(JsonObject user) {
        try {
            User newUser = Factory.buildUser(user);

            if(!ObjectId.isValid(user.getString("companyId")))
                return Json.createObjectBuilder()
                        .add(ResponseCodes.SUCCESS.toString(), false)
                        .add(ResponseCodes.ERROR_CODE.toString(), 400)
                        .add(ResponseCodes.ERROR_MESSAGE.toString(), "The user was not registered, the company id to link it to was not a valid id.")
                        .build();

            Company company = companyCrudService.findEntityById(new ObjectId(user.getString("companyId")));

            if(company == null)
                return Json.createObjectBuilder()
                        .add(ResponseCodes.SUCCESS.toString(), false)
                        .add(ResponseCodes.ERROR_CODE.toString(), 400)
                        .add(ResponseCodes.ERROR_MESSAGE.toString(), "The application was not registered, the company id to link it to was not found.")
                        .build();

            //Check if user exists
            User u = userCrudService.findEntityByProperty("username", newUser.getUsername());
            if(u != null)
                return Json.createObjectBuilder()
                        .add(ResponseCodes.SUCCESS.toString(), false)
                        .add(ResponseCodes.ERROR_CODE.toString(), 400)
                        .add(ResponseCodes.ERROR_MESSAGE.toString(), "The user was not registered, the user is already registered.")
                        .build();

            //Encrypt password
            StrongPasswordEncryptor encryptor = new StrongPasswordEncryptor();
            encryptor.encryptPassword(newUser.getPassword());

            //Create user
            userCrudService.createEntity(newUser);

            //Add user to company
            User dbUser = userCrudService.findEntityByProperty("username", newUser.getUsername());
            if(dbUser == null)
                return Json.createObjectBuilder()
                        .add(ResponseCodes.SUCCESS.toString(), false)
                        .add(ResponseCodes.ERROR_CODE.toString(), 400)
                        .add(ResponseCodes.ERROR_MESSAGE.toString(), "The user was not found, the user was not created successfully.")
                        .build();

            company.getUsers().add(dbUser);
            companyCrudService.updateEntity(company);

            return Json.createObjectBuilder().add("Successful", "User successfully created").build();

        }catch(Exception e){
            log.log(Level.WARNING, "There was an error registering the application", e);
            return Json.createObjectBuilder()
                    .add(ResponseCodes.SUCCESS.toString(), false)
                    .add(ResponseCodes.ERROR_CODE.toString(), 400)
                    .add(ResponseCodes.ERROR_MESSAGE.toString(), "The application was not successfully registered.")
                    .build();
        }
    }

    @Override
    public JsonObject loginUser(JsonObject user) {
        if(userCrudService.findEntityByProperty("username", user.getString("username"))!= null){
            StrongPasswordEncryptor encryptor = new StrongPasswordEncryptor();
            User usernameExists = userCrudService.findEntityByProperty("username", user.getString("username"));
            if(user.getString("username").equals(usernameExists.getUsername()) && encryptor.checkPassword(user.getString("password"), usernameExists.getPassword())){
                if(usernameExists==null)
                    return Json.createObjectBuilder()
                            .add(ResponseCodes.SUCCESS.toString(), false)
                            .add(ResponseCodes.ERROR_CODE.toString(), 400)
                            .add(ResponseCodes.ERROR_MESSAGE.toString(), "The application was not successfully registered.")
                            .build();
                else
                    return Json.createObjectBuilder()
                            .add(ResponseCodes.SUCCESS.toString(), true)
                            .add(ResponseCodes.SUCCESS_CODE.toString(), 200)
                            .add(ResponseCodes.SUCCESS_MESSAGE.toString(), "Login successful.")
                            .build();
            }else{
                return Json.createObjectBuilder()
                        .add(ResponseCodes.SUCCESS.toString(), false)
                        .add(ResponseCodes.ERROR_CODE.toString(), 400)
                        .add(ResponseCodes.ERROR_MESSAGE.toString(), "Username incorrect or does not exist.")
                        .build();
            }
        }else{
            return Json.createObjectBuilder()
                    .add(ResponseCodes.SUCCESS.toString(), false)
                    .add(ResponseCodes.ERROR_CODE.toString(), 400)
                    .add(ResponseCodes.ERROR_MESSAGE.toString(), "Username incorrect or does not exist.")
                    .build();
        }
    }

    @Override
    public JsonObject editUser(JsonObject user) {
        try {
            if(!ObjectId.isValid(user.getString("userId")))
                return Json.createObjectBuilder()
                        .add(ResponseCodes.SUCCESS.toString(), false)
                        .add(ResponseCodes.ERROR_CODE.toString(), 400)
                        .add(ResponseCodes.ERROR_MESSAGE.toString(), "The user was not found, the id is not a valid id.")
                        .build();

            User toBeUpdated = userCrudService.findEntityById(new ObjectId(user.getString("userId")));

            if(toBeUpdated == null)
                return Json.createObjectBuilder()
                        .add(ResponseCodes.SUCCESS.toString(), false)
                        .add(ResponseCodes.ERROR_CODE.toString(), 400)
                        .add(ResponseCodes.ERROR_MESSAGE.toString(), "The user was not found, something went wrong.")
                        .build();

            JsonObject deleteLink = Json.createObjectBuilder()
                    .add(Link.REL.toString(), "REMOVE")
                    .add(Link.DATATYPE.toString(), MediaType.APPLICATION_JSON)
                    .add(Link.HREF.toString(), "http://server.url.com/user/"+user.getString("userId"))
                    .add(Link.METHOD.toString(), "DELETE")
                    .build();

            JsonObject viewLink = Json.createObjectBuilder()
                    .add(Link.REL.toString(), "VIEW")
                    .add(Link.DATATYPE.toString(), MediaType.APPLICATION_JSON)
                    .add(Link.HREF.toString(), "http://server.url.com/user/"+user.getString("userId"))
                    .add(Link.METHOD.toString(), "GET")
                    .build();

            userCrudService.updateEntity(toBeUpdated);
            return Json.createObjectBuilder()
                    .add(ResponseCodes.SUCCESS.toString(), true)
                    .add(ResponseCodes.SUCCESS_CODE.toString(), 200)
                    .add(ResponseCodes.SUCCESS_MESSAGE.toString(), "User successfully updated.")
                    .add("Link", Json.createArrayBuilder()
                            .add(deleteLink)
                            .add(viewLink)
                            .build())
                    .build();
        }catch (Exception e){
            log.log(Level.WARNING, "There was an error retrieving the user", e);
            return Json.createObjectBuilder()
                    .add(ResponseCodes.SUCCESS.toString(), false)
                    .add(ResponseCodes.ERROR_CODE.toString(), 400)
                    .add(ResponseCodes.ERROR_MESSAGE.toString(), "User was not updated successfully.")
                    .build();
        }

    }

    @Override
    public JsonObject deleteUser(JsonObject user) {
        try{
            User toRemove = userCrudService.findEntityById(new ObjectId(user.getString("userId")));

            if(toRemove == null)
                return Json.createObjectBuilder()
                        .add(ResponseCodes.SUCCESS.toString(), false)
                        .add(ResponseCodes.ERROR_CODE.toString(), 400)
                        .add(ResponseCodes.ERROR_MESSAGE.toString(), "The user was not removed. Something went wrong while looking for the Application")
                        .build();

            userCrudService.deleteEntity(toRemove);

            User removed = userCrudService.findEntityById(new ObjectId(user.getString("userId")));

            if(removed!=null)
                return Json.createObjectBuilder()
                        .add(ResponseCodes.SUCCESS.toString(), false)
                        .add(ResponseCodes.ERROR_CODE.toString(), 400)
                        .add(ResponseCodes.ERROR_MESSAGE.toString(), "The user was not successfully removed.")
                        .build();

            return Json.createObjectBuilder()
                    .add(ResponseCodes.SUCCESS.toString(), true)
                    .add(ResponseCodes.SUCCESS_CODE.toString(), 200)
                    .add(ResponseCodes.SUCCESS_MESSAGE.toString(), "The user was successfully removed.")
                    .build();

        }catch (Exception e){
            log.log(Level.WARNING, "There was an error retrieving the user", e);
            return Json.createObjectBuilder()
                    .add(ResponseCodes.SUCCESS.toString(), false)
                    .add(ResponseCodes.ERROR_CODE.toString(), 400)
                    .add(ResponseCodes.ERROR_MESSAGE.toString(), "The user was not successfully removed.")
                    .build();
        }
    }

    @Override
    public JsonObject viewUser(String userId) {
        try{
            User toView = userCrudService.findEntityById(new ObjectId(userId));

            if(toView == null)
                return Json.createObjectBuilder()
                        .add(ResponseCodes.SUCCESS.toString(), false)
                        .add(ResponseCodes.ERROR_CODE.toString(), 400)
                        .add(ResponseCodes.ERROR_MESSAGE.toString(), "The user was not found. Something went wrong while looking for the Application")
                        .build();

            JsonObject deleteLink = Json.createObjectBuilder()
                    .add(Link.REL.toString(), "REMOVE")
                    .add(Link.DATATYPE.toString(), MediaType.APPLICATION_JSON)
                    .add(Link.HREF.toString(), "http://server.url.com/user/"+toView.getId().toString())
                    .add(Link.METHOD.toString(), "DELETE")
                    .build();

            JsonObject editLink = Json.createObjectBuilder()
                    .add(Link.REL.toString(), "EDIT")
                    .add(Link.DATATYPE.toString(), MediaType.APPLICATION_JSON)
                    .add(Link.HREF.toString(), "http://server.url.com/user/"+toView.getId().toString())
                    .add(Link.METHOD.toString(), "PUT")
                    .build();

            return Json.createObjectBuilder()
                    .add(ResponseCodes.SUCCESS.toString(), true)
                    .add(ResponseCodes.SUCCESS_CODE.toString(), 200)
                    .add(ResponseCodes.SUCCESS_MESSAGE.toString(), "The user was successfully retrieved.")
                    .add("Link", Json.createArrayBuilder()
                            .add(deleteLink)
                            .add(editLink)
                            .build())
                    .build();

        }catch (Exception e){
            log.log(Level.WARNING, "There was an error retrieving the user", e);
            return Json.createObjectBuilder()
                    .add(ResponseCodes.SUCCESS.toString(), false)
                    .add(ResponseCodes.ERROR_CODE.toString(), 400)
                    .add(ResponseCodes.ERROR_MESSAGE.toString(), "The user was not successfully retrieved.")
                    .build();
        }
    }
}

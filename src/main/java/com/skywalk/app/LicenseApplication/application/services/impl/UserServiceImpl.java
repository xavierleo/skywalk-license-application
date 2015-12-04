package main.java.com.skywalk.app.LicenseApplication.application.services.impl;

import com.google.gson.Gson;
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
import main.java.com.skywalk.app.LicenseApplication.domain.models.ContactDetails;
import main.java.com.skywalk.app.LicenseApplication.domain.models.User;
import org.bson.types.ObjectId;
import org.jasypt.util.password.StrongPasswordEncryptor;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.ws.rs.core.MediaType;
import java.util.List;
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
            User newUser = Factory.buildUser(user.getJsonObject("User"));

            if(!ObjectId.isValid(user.getJsonObject("Company").getString("companyId")))
                return Json.createObjectBuilder()
                        .add(ResponseCodes.SUCCESS.toString(), false)
                        .add(ResponseCodes.ERROR_CODE.toString(), 400)
                        .add(ResponseCodes.ERROR_MESSAGE.toString(), "The user was not registered, the company id to link it to was not a valid id.")
                        .build();

            Company company = companyCrudService.findEntityById(new ObjectId(user.getJsonObject("Company").getString("companyId")));

            if(company == null)
                return Json.createObjectBuilder()
                        .add(ResponseCodes.SUCCESS.toString(), false)
                        .add(ResponseCodes.ERROR_CODE.toString(), 400)
                        .add(ResponseCodes.ERROR_MESSAGE.toString(), "The company was not registered, the company does not exist.")
                        .build();

            //Check if user exists
            User u = userCrudService.findEntityByProperty("username", newUser.getUsername());
            if(u != null)
                return Json.createObjectBuilder()
                        .add(ResponseCodes.SUCCESS.toString(), false)
                        .add(ResponseCodes.ERROR_CODE.toString(), 400)
                        .add(ResponseCodes.ERROR_MESSAGE.toString(), "The user was not registered, the username already exists.")
                        .build();

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

            JsonObject deleteUserLink = Json.createObjectBuilder()
                    .add(Link.REL.toString(), "REMOVE")
                    .add(Link.DATATYPE.toString(), MediaType.APPLICATION_JSON)
                    .add(Link.HREF.toString(), "/api/user/"+dbUser.getId().toString())
                    .add(Link.METHOD.toString(), "DELETE")
                    .build();

            JsonObject viewUserLink = Json.createObjectBuilder()
                    .add(Link.REL.toString(), "VIEW")
                    .add(Link.DATATYPE.toString(), MediaType.APPLICATION_JSON)
                    .add(Link.HREF.toString(), "/api/user/"+dbUser.getId().toString())
                    .add(Link.METHOD.toString(), "GET")
                    .build();

            JsonObject updateUserLink = Json.createObjectBuilder()
                    .add(Link.REL.toString(), "EDIT")
                    .add(Link.DATATYPE.toString(), MediaType.APPLICATION_JSON)
                    .add(Link.HREF.toString(), "/api/user/")
                    .add(Link.METHOD.toString(), "PUT")
                    .build();

            return Json.createObjectBuilder()
                    .add(ResponseCodes.SUCCESS.toString(), true)
                    .add(ResponseCodes.SUCCESS_CODE.toString(), 200)
                    .add(ResponseCodes.SUCCESS_MESSAGE.toString(), "User was successfully registered.")
                    .add("User", new Gson().toJson(dbUser))
                    .add("Links", Json.createArrayBuilder()
                                    .add(deleteUserLink)
                                    .add(viewUserLink)
                                    .add(updateUserLink)
                                    .build()
                    )
                    .build();

        }catch(Exception e){
            log.log(Level.WARNING, "There was an error registering the user", e);
            return Json.createObjectBuilder()
                    .add(ResponseCodes.SUCCESS.toString(), false)
                    .add(ResponseCodes.ERROR_CODE.toString(), 400)
                    .add(ResponseCodes.ERROR_MESSAGE.toString(), "The user was not successfully registered.")
                    .build();
        }
    }

    @Override
    public JsonObject loginUser(JsonObject user) {
        User usernameExists = userCrudService.findEntityByProperty("username", user.getJsonObject("User").getString("username"));

        if(usernameExists!= null){
            StrongPasswordEncryptor encryptor = new StrongPasswordEncryptor();

            if(user.getJsonObject("User").getString("username").equals(usernameExists.getUsername()) && encryptor.checkPassword(user.getJsonObject("User").getString("password"), usernameExists.getPassword())){
                return Json.createObjectBuilder()
                    .add(ResponseCodes.SUCCESS.toString(), true)
                    .add(ResponseCodes.SUCCESS_CODE.toString(), 200)
                    .add(ResponseCodes.SUCCESS_MESSAGE.toString(), "Login successful.")
                    .add("Username",new Gson().toJson(usernameExists))
                    .build();
            }else{
                return Json.createObjectBuilder()
                        .add(ResponseCodes.SUCCESS.toString(), false)
                        .add(ResponseCodes.ERROR_CODE.toString(), 400)
                        .add(ResponseCodes.ERROR_MESSAGE.toString(), "The credentials is incorrect.")
                        .build();
            }
        }else{
            return Json.createObjectBuilder()
                    .add(ResponseCodes.SUCCESS.toString(), false)
                    .add(ResponseCodes.ERROR_CODE.toString(), 400)
                    .add(ResponseCodes.ERROR_MESSAGE.toString(), "The username is incorrect or does not exist.")
                    .build();
        }
    }

    @Override
    public JsonObject editUser(JsonObject user) {
        try {
            if(!ObjectId.isValid(user.getJsonObject("User").getString("userId")))
                return Json.createObjectBuilder()
                        .add(ResponseCodes.SUCCESS.toString(), false)
                        .add(ResponseCodes.ERROR_CODE.toString(), 400)
                        .add(ResponseCodes.ERROR_MESSAGE.toString(), "The user was not found, the id is not a valid id.")
                        .build();

            User toBeUpdated = userCrudService.findEntityById(new ObjectId(user.getJsonObject("User").getString("userId")));

            if(toBeUpdated == null)
                return Json.createObjectBuilder()
                        .add(ResponseCodes.SUCCESS.toString(), false)
                        .add(ResponseCodes.ERROR_CODE.toString(), 400)
                        .add(ResponseCodes.ERROR_MESSAGE.toString(), "The user was not found, something went wrong.")
                        .build();

            JsonObject deleteLink = Json.createObjectBuilder()
                    .add(Link.REL.toString(), "REMOVE")
                    .add(Link.DATATYPE.toString(), MediaType.APPLICATION_JSON)
                    .add(Link.HREF.toString(), "/api/user/"+toBeUpdated.getId())
                    .add(Link.METHOD.toString(), "DELETE")
                    .build();

            JsonObject viewLink = Json.createObjectBuilder()
                    .add(Link.REL.toString(), "VIEW")
                    .add(Link.DATATYPE.toString(), MediaType.APPLICATION_JSON)
                    .add(Link.HREF.toString(), "/api/user/"+toBeUpdated.getId())
                    .add(Link.METHOD.toString(), "GET")
                    .build();

            ContactDetails c = new ContactDetails(
                    user.getJsonObject("User").getJsonObject("ContactDetails").getString("email"),
                    user.getJsonObject("User").getJsonObject("ContactDetails").getString("mobile")
            );

            toBeUpdated.setName(user.getJsonObject("User").getString("name"));
            toBeUpdated.setSurname(user.getJsonObject("User").getString("surname"));
            toBeUpdated.setContactDetails(c);

            userCrudService.updateEntity(toBeUpdated);

            return Json.createObjectBuilder()
                    .add(ResponseCodes.SUCCESS.toString(), true)
                    .add(ResponseCodes.SUCCESS_CODE.toString(), 200)
                    .add(ResponseCodes.SUCCESS_MESSAGE.toString(), "User successfully updated.")
                    .add("User", new Gson().toJson(toBeUpdated))
                    .add("Link", Json.createArrayBuilder()
                            .add(deleteLink)
                            .add(viewLink)
                            .build())
                    .build();
        } catch (Exception e){
            log.log(Level.WARNING, "There was an error updating the user", e);
            return Json.createObjectBuilder()
                    .add(ResponseCodes.SUCCESS.toString(), false)
                    .add(ResponseCodes.ERROR_CODE.toString(), 400)
                    .add(ResponseCodes.ERROR_MESSAGE.toString(), "User was not updated successfully.")
                    .build();
        }

    }

    @Override
    public JsonObject deleteUser(String userId) {
        try{
            if(!ObjectId.isValid(userId))
                return Json.createObjectBuilder()
                        .add(ResponseCodes.SUCCESS.toString(), false)
                        .add(ResponseCodes.ERROR_CODE.toString(), 400)
                        .add(ResponseCodes.ERROR_MESSAGE.toString(), "The user was not found, the id is not a valid id.")
                        .build();

            User toRemove = userCrudService.findEntityById(new ObjectId(userId));

            if(toRemove == null)
                return Json.createObjectBuilder()
                        .add(ResponseCodes.SUCCESS.toString(), false)
                        .add(ResponseCodes.ERROR_CODE.toString(), 400)
                        .add(ResponseCodes.ERROR_MESSAGE.toString(), "The user was not removed. The user does not exist.")
                        .build();
            //first remove reference from company
            List<Company> companies = companyCrudService.getAllEntities();

            if(companies == null || companies.size() <= 0)
                return Json.createObjectBuilder()
                        .add(ResponseCodes.SUCCESS.toString(), false)
                        .add(ResponseCodes.ERROR_CODE.toString(), 400)
                        .add(ResponseCodes.ERROR_MESSAGE.toString(), "The user was not removed properly. The company the user registered against does not exist.")
                        .build();

            for(User u:companies.get(0).getUsers()) {
                if (toRemove.getId().equals(u.getId())) {
                    companies.get(0).getUsers().remove(u);
                    companyCrudService.updateEntity(companies.get(0));
                    break;
                }
            }

            userCrudService.deleteEntity(toRemove);

            User removed = userCrudService.findEntityById(new ObjectId(userId));



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
            log.log(Level.WARNING, "There was an error removing the user", e);
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
            if(!ObjectId.isValid(userId))
                return Json.createObjectBuilder()
                        .add(ResponseCodes.SUCCESS.toString(), false)
                        .add(ResponseCodes.ERROR_CODE.toString(), 400)
                        .add(ResponseCodes.ERROR_MESSAGE.toString(), "The user was not found, the id is not a valid id.")
                        .build();

            User toView = userCrudService.findEntityById(new ObjectId(userId));

            if(toView == null)
                return Json.createObjectBuilder()
                        .add(ResponseCodes.SUCCESS.toString(), false)
                        .add(ResponseCodes.ERROR_CODE.toString(), 400)
                        .add(ResponseCodes.ERROR_MESSAGE.toString(), "The user was not found. Something went wrong while looking for the user.")
                        .build();

            JsonObject deleteLink = Json.createObjectBuilder()
                    .add(Link.REL.toString(), "REMOVE")
                    .add(Link.DATATYPE.toString(), MediaType.APPLICATION_JSON)
                    .add(Link.HREF.toString(), "/api/user/"+toView.getId().toString())
                    .add(Link.METHOD.toString(), "DELETE")
                    .build();

            JsonObject editLink = Json.createObjectBuilder()
                    .add(Link.REL.toString(), "EDIT")
                    .add(Link.DATATYPE.toString(), MediaType.APPLICATION_JSON)
                    .add(Link.HREF.toString(), "/api/user/"+toView.getId().toString())
                    .add(Link.METHOD.toString(), "PUT")
                    .build();

            return Json.createObjectBuilder()
                    .add(ResponseCodes.SUCCESS.toString(), true)
                    .add(ResponseCodes.SUCCESS_CODE.toString(), 200)
                    .add(ResponseCodes.SUCCESS_MESSAGE.toString(), "The user was successfully retrieved.")
                    .add("User",new Gson().toJson(toView))
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

    @Override
    public JsonObject viewAllUsers() {
        try{
            List<User> toView = userCrudService.getAllEntities();

            if(toView == null || toView.size() == 0)
                return Json.createObjectBuilder()
                        .add(ResponseCodes.SUCCESS.toString(), false)
                        .add(ResponseCodes.ERROR_CODE.toString(), 400)
                        .add(ResponseCodes.ERROR_MESSAGE.toString(), "The was no users found.")
                        .build();

            JsonArrayBuilder builder = Json.createArrayBuilder();

            for(User u: toView){
                JsonObject deleteLink = Json.createObjectBuilder()
                        .add(Link.REL.toString(), "REMOVE")
                        .add(Link.DATATYPE.toString(), MediaType.APPLICATION_JSON)
                        .add(Link.HREF.toString(), "/api/user/"+u.getId().toString())
                        .add(Link.METHOD.toString(), "DELETE")
                        .build();

                JsonObject editLink = Json.createObjectBuilder()
                        .add(Link.REL.toString(), "EDIT")
                        .add(Link.DATATYPE.toString(), MediaType.APPLICATION_JSON)
                        .add(Link.HREF.toString(), "/api/user/"+u.getId().toString())
                        .add(Link.METHOD.toString(), "PUT")
                        .build();

                builder.add(
                    Json.createObjectBuilder()
                        .add("User", new Gson().toJson(toView))
                        .add("Link", Json.createArrayBuilder()
                                        .add(deleteLink)
                                        .add(editLink)
                                        .build()
                        ).build()
                );
            }

            return Json.createObjectBuilder()
                    .add(ResponseCodes.SUCCESS.toString(), true)
                    .add(ResponseCodes.SUCCESS_CODE.toString(), 200)
                    .add(ResponseCodes.SUCCESS_MESSAGE.toString(), "The users was successfully retrieved.")
                    .add("Users",builder.build())
                    .build();

        }catch (Exception e){
            log.log(Level.WARNING, "There was an error retrieving the users", e);
            return Json.createObjectBuilder()
                    .add(ResponseCodes.SUCCESS.toString(), false)
                    .add(ResponseCodes.ERROR_CODE.toString(), 400)
                    .add(ResponseCodes.ERROR_MESSAGE.toString(), "The users was not successfully retrieved.")
                    .build();
        }
    }
}

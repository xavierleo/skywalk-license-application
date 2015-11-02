package main.java.com.skywalk.app.LicenseApplication.application.services.impl;

import main.java.com.skywalk.app.LicenseApplication.application.services.UserService;
import main.java.com.skywalk.app.LicenseApplication.domain.crud.CompanyCrudService;
import main.java.com.skywalk.app.LicenseApplication.domain.crud.UserCrudService;
import main.java.com.skywalk.app.LicenseApplication.domain.factory.Factory;
import main.java.com.skywalk.app.LicenseApplication.domain.models.Company;
import main.java.com.skywalk.app.LicenseApplication.domain.models.User;
import org.bson.types.ObjectId;
import org.jasypt.util.password.StrongPasswordEncryptor;

import javax.json.Json;
import javax.json.JsonObject;

/**
 * Created by xavier on 2015/10/28.
 */
public class UserServiceImpl implements UserService {

    private UserCrudService userCrudService;
    private CompanyCrudService companyCrudService;

    @Override
    public JsonObject registerUser(JsonObject user) {
        User newUser = Factory.buildUser(user);

        Company company = companyCrudService.findEntityById(new ObjectId(user.getString("companyId")));

        //Check if user exists
        User u = userCrudService.findEntityByProperty("username", newUser.getUsername());
        if(u != null)
            return Json.createObjectBuilder().add("Error","User already exists").build();

        //Encrypt password
        StrongPasswordEncryptor encryptor = new StrongPasswordEncryptor();
        encryptor.encryptPassword(newUser.getPassword());

        //Create user
        userCrudService.createEntity(newUser);

        //Add user to company
        User dbUser = userCrudService.findEntityByProperty("username", newUser.getUsername());
        company.getUsers().add(dbUser);
        companyCrudService.updateEntity(company);

        return Json.createObjectBuilder().add("Successful", "User successfully created").build();
    }

    @Override
    public JsonObject loginUser(JsonObject user) {
        return null;
    }

    @Override
    public JsonObject editUser(JsonObject user) {
        return null;
    }

    @Override
    public JsonObject deleteUser(JsonObject user) {
        return null;
    }

    @Override
    public JsonObject viewUser(String userId) {
        return null;
    }
}

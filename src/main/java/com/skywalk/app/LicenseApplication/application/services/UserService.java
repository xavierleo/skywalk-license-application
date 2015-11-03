package main.java.com.skywalk.app.LicenseApplication.application.services;

import javax.json.JsonObject;

/**
 * Created by xavier on 2015/10/28.
 */
public interface UserService {
     JsonObject registerUser(JsonObject user);

     JsonObject loginUser(JsonObject user);

     JsonObject editUser(JsonObject user);

     JsonObject deleteUser(String userId);

     JsonObject viewUser(String userId);

     JsonObject viewAllUsers();
}

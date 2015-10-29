package main.java.com.skywalk.app.LicenseApplication.application.services;

import javax.json.JsonObject;

/**
 * Created by xavier on 2015/10/28.
 */
public interface UserService {
    public JsonObject registerUser(JsonObject user);

    public JsonObject loginUser(JsonObject user);

    public JsonObject editUser(JsonObject user);

    public JsonObject deleteUser(JsonObject user);

    public JsonObject viewUser(String userId);
}

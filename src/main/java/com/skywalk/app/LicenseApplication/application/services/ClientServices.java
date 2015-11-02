package main.java.com.skywalk.app.LicenseApplication.application.services;

import org.bson.types.ObjectId;

import javax.json.JsonObject;

/**
 * ClientServices is the interface for all services related to the applications
 * being stored and  managed by the company;
 * @author      Tyrone Adams
 * @version     %I%, %G%
 * @since       1.0
 */

public interface ClientServices {

    /**
     *
     * Returns a JsonObject stating whether the Client successfully registered on the
     * Skywalk Licensing Application
     *
     * @param  client the JsonObject containing the new client to persist on the Skywalk License Application
     * @return        the JsonObject value stating if registration was successful or not
     */
    JsonObject registerClient(JsonObject client);

    /**
     *
     * Returns a JsonObject stating whether the Client successfully updated on the
     * Skywalk Licensing Application
     *
     * @param  client the JsonObject representing the client to be updated on the Skywalk License Application
     * @return        the JsonObject value stating if update was successful or not
     */
    JsonObject editClient(JsonObject client);

    /**
     *
     * Returns a JsonObject containing the Application Details that was successfully retrieved from the
     * Skywalk Licensing Application
     *
     * @param  id  the ObjectId of the application to view on the Skywalk License Application
     * @return the JsonObject representing the Application Object
     */
    JsonObject viewClient(ObjectId id);

    /**
     *
     * Returns a JsonObject containing all the Application Details that was successfully retrieved from the
     * Skywalk Licensing Application
     *
     * @param  companyId the name of the company that all the applications are registered to on the Skywalk License Application
     * @return the       JsonObject value containing each applications applications details
     */
    JsonObject viewAllClients(ObjectId companyId);

    /**
     *
     * Returns a JsonObject stating whether the Client was successfully removed on the
     * Skywalk Licensing Application
     *
     * @param  clientId the ObjectId representing the client to remove on the Skywalk License Application
     * @return          the JsonObject stating if removal was successful or not
     */
    JsonObject removeClient(ObjectId clientId);
}

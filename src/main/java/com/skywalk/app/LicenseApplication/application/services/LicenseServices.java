package main.java.com.skywalk.app.LicenseApplication.application.services;

import org.bson.types.ObjectId;

import javax.json.JsonObject;

/**
 * LicenseServices is the interface for all services related to the license generation for clients and the respective applications
 * being stored and  managed by the company;
 * @author      Tyrone Adams
 * @version     %I%, %G%
 * @since       1.0
 */
public interface LicenseServices {

    /**
     *
     * Returns a JsonObject stating whether the License Was Generated successfully on the
     * Skywalk Licensing Application
     *
     * @param  newLicenseDetails the JsonObject containing the new license details to persist on the Skywalk License Application
     * @return                   the JsonObject value stating if generation of the license was successful or not
     */
    JsonObject generateLicenseForClientAndApplication(JsonObject newLicenseDetails);

    /**
     *
     * Returns a JsonObject stating whether the License was updated successfully on the
     * Skywalk Licensing Application
     *
     * @param  licenseDetails    the JsonObject containing the license details to update on the Skywalk License Application
     * @return                   the JsonObject value stating if update of the license was successful or not
     */
    JsonObject updateLicenseForClientAndApplication(JsonObject licenseDetails);

    /**
     *
     * Returns a JsonObject with the License Details
     *
     * @param  licenseDetails    the JsonObject containing the client id and the application id to use as a search criteria
     * @return                   the JsonObject value with the License Details based on the search criteria
     */
    JsonObject viewLicenseForClientAndApplication(JsonObject licenseDetails);

    /**
     *
     * Returns a JsonObject with the License Details for a Specific Client
     *
     * @param  clientId    the ObjectId containing the client id to use as a search criteria
     * @return             the JsonObject value with the License Details based on the search criteria
     */
    JsonObject viewAllLicenseForClient(ObjectId clientId);

    /**
     *
     * Returns a JsonObject stating whether the License was successfully removed on the  Skywalk Licensing Application
     *
     * @param  licenseId the ObjectId representing the id to use as a filter to remove the License on the Skywalk License Application
     * @return the 	     JsonObject stating if removal was successful or not
     */
    JsonObject removeLicenseForClientAndApplication(ObjectId licenseId);
}

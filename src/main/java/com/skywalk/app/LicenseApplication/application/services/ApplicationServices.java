/**
 * 
 */
package main.java.com.skywalk.app.LicenseApplication.application.services;

import javax.json.JsonObject;

import org.bson.types.ObjectId;

/**
 * ApplicationServices is the interface for all services related to the applications
 * being stored and  managed by the company; 
 * @author      Tyrone Adams
 * @version     %I%, %G%
 * @since       1.0
 */ 

public interface ApplicationServices {
	
	/**
	 * 
	 * Returns a JsonObject stating whether the Application successfully registered on the 
	 * Skywalk Licensing Application
	 * 
	 * @param  application the JsonObject containing the new application to persist on the Skywalk License Application
	 * @return the JsonObject value stating if registration was successful or not
	 */
	JsonObject registerApplication(JsonObject application);
	
	/**
	 * 
	 * Returns a JsonObject stating whether the Application successfully updated on the 
	 * Skywalk Licensing Application
	 * 
	 * @param  application the name of the application to update on the Skywalk License Application
	 * @return the JsonObject value stating if update was successful or not
	 */
	JsonObject editApplication(JsonObject application);
	
	/**
	 * 
	 * Returns a JsonObject containing the Application Details that was successfully retrieved from the 
	 * Skywalk Licensing Application
	 * 
	 * @param  id  the ObjectId of the application to view on the Skywalk License Application
	 * @return the JsonObject representing the Application Object
	 */
	JsonObject viewApplication(ObjectId id);
	
	/**
	 * 
	 * Returns a JsonObject containing all the Application Details that was successfully retrieved from the 
	 * Skywalk Licensing Application
	 * 
	 * @param  companyId the name of the company that all the applications are registered to on the Skywalk License Application
	 * @return the       JsonObject value containing each applications applications details
	 */
	JsonObject viewAllApplication(ObjectId companyId);
	
	/**
	 * 
	 * Returns a JsonObject stating whether the Application was successfully removed on the 
	 * Skywalk Licensing Application
	 * 
	 * @param  applicationId the ObjectId representing the application to remove on the Skywalk License Application
	 * @return the 			 JsonObject stating if removal was successful or not
	 */
	JsonObject removeApplication(ObjectId applicationId);

    /**
     *
     * Returns a JsonObject stating whether the Client was successfully added to the Application
     * Skywalk Licensing Application
     *
     * @param  application the JsonObject representing the applicationId and the clientId to link on the Skywalk License Application
     * @return 			   the JsonObject stating if linking was successful or not
     */
    JsonObject assignClientToApplication(JsonObject application);

	/**
	 *
	 * Returns a JsonObject stating whether the Client was successfully added to the Application
	 * Skywalk Licensing Application
	 *
	 * @param  application the JsonObject representing the applicationId and the priceRangeId to link on the Skywalk License Application
	 * @return 			   the JsonObject stating if linking was successful or not
	 */
	JsonObject assignPriceRangeToApplication(JsonObject application);
}

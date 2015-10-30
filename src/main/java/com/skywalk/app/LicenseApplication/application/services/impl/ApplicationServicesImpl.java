/**
 * 
 */
package main.java.com.skywalk.app.LicenseApplication.application.services.impl;

import lombok.extern.java.Log;
import main.java.com.skywalk.app.LicenseApplication.application.services.ApplicationServices;
import main.java.com.skywalk.app.LicenseApplication.domain.crud.ApplicationCrudService;
import main.java.com.skywalk.app.LicenseApplication.domain.crud.CompanyCrudService;
import main.java.com.skywalk.app.LicenseApplication.domain.crud.impl.ApplicationCrudServiceImpl;
import main.java.com.skywalk.app.LicenseApplication.domain.crud.impl.CompanyCrudServiceImpl;
import main.java.com.skywalk.app.LicenseApplication.domain.factory.Factory;
import main.java.com.skywalk.app.LicenseApplication.domain.models.Application;
import main.java.com.skywalk.app.LicenseApplication.domain.models.Company;
import org.bson.types.ObjectId;

import javax.json.Json;
import javax.json.JsonObject;
import java.util.logging.Level;

/**
 * @author ironhulk
 *
 */
@Log
public class ApplicationServicesImpl implements ApplicationServices {

    private ApplicationCrudService applicationCrudService;
    private CompanyCrudService companyCrudService;

    public ApplicationServicesImpl(){
        applicationCrudService = new ApplicationCrudServiceImpl();
        companyCrudService = new CompanyCrudServiceImpl();
    }

    @Override
    public JsonObject registerApplication(JsonObject applicationParam) {
        try{
            Application application = Factory.buildApplication(applicationParam);

            //find the company to register the application against
            if(!ObjectId.isValid(applicationParam.getString("companyId")))
                return null;

            //find the company to register the application against
            Company company = companyCrudService.findEntityById(new ObjectId(applicationParam.getString("companyId")));

            if(company == null)
                return null;

            applicationCrudService.createEntity(application);

            Application created = applicationCrudService.findEntityById(application.getId());

            if(created == null)
                return null;
            JsonObject registeredApplication = Json.createObjectBuilder()
                    .add("","")
                    .build();
            return Json.createObjectBuilder()
                    .add("Application", registeredApplication).build();
        }catch (Exception e){
            log.log(Level.WARNING, "There was an error registering the application", e);
            return null;
        }
    }

    @Override
    public JsonObject editApplication(JsonObject application) {
        return null;
    }

    @Override
    public JsonObject viewApplication(ObjectId id) {
        return null;
    }

    @Override
    public JsonObject viewAllApplication(ObjectId companyId) {
        return null;
    }

    @Override
    public JsonObject removeApplication(ObjectId applicationId) {
        return null;
    }
}

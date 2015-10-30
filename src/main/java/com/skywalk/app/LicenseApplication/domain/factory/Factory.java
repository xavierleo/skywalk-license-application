package main.java.com.skywalk.app.LicenseApplication.domain.factory;

import main.java.com.skywalk.app.LicenseApplication.domain.models.*;
import org.bson.types.ObjectId;

import javax.json.JsonObject;

/**
 * Created by xavier on 2015/10/28.
 */
public class Factory {
    public static Company buildCompany(JsonObject company){
        Company c = new Company();
        EmailConfiguration emailConfiguration = new EmailConfiguration(company.getJsonObject("emailConfiguration").getString("smptServer"),
                company.getJsonObject("emailConfiguration").getString("serverPort"),
                company.getJsonObject("emailConfiguration").getString("authUsername"),
                company.getJsonObject("emailConfiguration").getString("authPassword"),
                company.getJsonObject("emailConfiguration").getString("primaryAccountAddress"),
                company.getJsonObject("emailConfiguration").getString("emailSignature"),
                company.getJsonObject("emailConfiguration").getString("enableSMTPAuthentication"),
                company.getJsonObject("emailConfiguration").getString("enableTTLSSupport") );

        c.setId(new ObjectId());
        c.setName(company.getString("name"));
        c.setDescription(company.getString("description"));
        c.setIndustry(company.getString("industry"));
        c.setEmailConfiguration(emailConfiguration);
        return c;
    }

    public static User buildUser(JsonObject user){
        User u = new User();
        ContactDetails c = new ContactDetails(user.getJsonObject("contactDetails").getString("email"),
                user.getJsonObject("contactDetails").getString("mobile"));
        u.setId(new ObjectId());
        u.setName(user.getString("name"));
        u.setSurname(user.getString("surname"));
        u.setUsername(user.getString("username"));
        u.setPassword(user.getString("password"));
        u.setRole(user.getString("role"));
        u.setContactDetails(c);
        return u;
    }

    public static Application buildApplication(JsonObject application){
        Application a = new Application();
        a.setId(new ObjectId());
        a.setName(application.getString("name"));
        a.setShortDescription(application.getString("shortDescription"));
        a.setLongDescription(application.getString("longDescription"));
        return a;
    }
}

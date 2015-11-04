package main.java.com.skywalk.app.LicenseApplication.domain.factory;

import main.java.com.skywalk.app.LicenseApplication.domain.models.*;
import org.bson.types.ObjectId;
import org.jasypt.util.password.StrongPasswordEncryptor;

import javax.json.JsonObject;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by xavier on 2015/10/28.
 */
public class Factory {
    public static Company buildCompany(JsonObject company){
        Company c = new Company();
        EmailConfiguration emailConfiguration = new EmailConfiguration(
                company.getJsonObject("EmailConfiguration").getString("smptServer"),
                company.getJsonObject("EmailConfiguration").getString("serverPort"),
                company.getJsonObject("EmailConfiguration").getString("authUsername"),
                company.getJsonObject("EmailConfiguration").getString("authPassword"),
                company.getJsonObject("EmailConfiguration").getString("primaryAccountAddress"),
                company.getJsonObject("EmailConfiguration").getString("emailSignature"),
                company.getJsonObject("EmailConfiguration").getString("enableSMTPAuthentication"),
                company.getJsonObject("EmailConfiguration").getString("enableTTLSSupport"));

        c.setId(new ObjectId());
        c.setName(company.getString("name"));
        c.setDescription(company.getString("description"));
        c.setIndustry(company.getString("industry"));
        c.setEmailConfiguration(emailConfiguration);
        return c;
    }

    public static User buildUser(JsonObject user){
        //Encrypt password
        StrongPasswordEncryptor encryptor = new StrongPasswordEncryptor();

        User u = new User();
        ContactDetails c = new ContactDetails(user.getJsonObject("ContactDetails").getString("email"),
                user.getJsonObject("ContactDetails").getString("mobile"));
        u.setId(new ObjectId());
        u.setName(user.getString("name"));
        u.setSurname(user.getString("surname"));
        u.setUsername(user.getString("username"));
        u.setPassword(encryptor.encryptPassword(user.getString("password")));
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

    public static Client buildClient(JsonObject client){
        Client c = new Client();
        c.setId(new ObjectId());
        c.setName(client.getString("name"));
        c.setSize(client.getString("size"));
        return c;
    }

    public static PriceRange buildPriceRange(JsonObject priceRange) {
        PriceRange p = new PriceRange();
        p.setId(new ObjectId());
        p.setDiscountPercentage(Double.valueOf(priceRange.getString("discountPercentage")));
        p.setMinAmountUsers(priceRange.getInt("minAmountUsers"));
        p.setMaxAmountUsers(priceRange.getInt("maxAmountUsers"));
        p.setPriceForUserInRange(Double.valueOf(priceRange.getString("priceForUsersInRange")));
        p.setFinalPriceWithDiscount((Double.valueOf(priceRange.getString("priceForUsersInRange"))*(Double.valueOf(priceRange.getString("discountPercentage"))/100))+Double.valueOf(priceRange.getString("priceForUsersInRange")));
        return p;
    }
    public static License buildLicense(JsonObject license) throws ParseException{
        SimpleDateFormat format = new SimpleDateFormat("yyyy/mm/dd");
        License l = new License();
        l.setId(new ObjectId());
        l.setDescription(license.getString("description"));
        l.setPaymentType(license.getString("paymentType"));
        l.setTotalRequestedUsers(license.getInt("totalRequestedUsers"));
        l.setTotalAvailableUsers(license.getInt("totalAvailableUsers"));
        l.setLicenseFee(license.getInt("licenseFee"));
        l.setInvoiceDate(format.parse(license.getString("nextInvoiceDate")));
        l.setStartDate(format.parse(license.getString("startDate")));
        l.setEndDate(format.parse(license.getString("endDate")));
        return l;
    }
}

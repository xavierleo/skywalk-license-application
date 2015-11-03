package main.java.com.skywalk.app.LicenseApplication.application.services;

import javax.json.JsonObject;

/**
 * Created by xavier on 2015/11/02.
 */
public interface PriceRangeServices {
    JsonObject createPriceRange(JsonObject priceRange);

    JsonObject linkPriceRangeToApplication(JsonObject priceRange);

    JsonObject editPriceRange(JsonObject priceRange);

    JsonObject deletePriceRange(JsonObject priceRange);

    JsonObject viewPriceRange(String priceRangeId);
}

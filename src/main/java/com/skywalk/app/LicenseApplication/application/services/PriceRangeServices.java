package main.java.com.skywalk.app.LicenseApplication.application.services;

import javax.json.JsonObject;

/**
 * Created by xavier on 2015/11/02.
 */
public interface PriceRangeServices {
    public JsonObject createPriceRange(JsonObject priceRange);

    public JsonObject editPriceRange(JsonObject priceRange);

    public JsonObject deletePriceRange(JsonObject priceRange);

    public JsonObject viewPriceRange(String priceRangeId);
}

package main.java.com.skywalk.app.LicenseApplication.domain.models;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

import lombok.Data;

@Data
@Entity(noClassnameStored=true,value="priceRanges")
public class PriceRange{

    public PriceRange() {
    }

    @Id
    @NotNull private ObjectId id;

    @NotNull @Min(1) private int minAmountUsers;

    @NotNull @Min(1) private int maxAmountUsers;

    @NotNull private double priceForUserInRange;

    @NotNull private double discountPercentage;

    @NotNull private double finalPriceWithDiscount;


}
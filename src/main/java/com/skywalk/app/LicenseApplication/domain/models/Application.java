package main.java.com.skywalk.app.LicenseApplication.domain.models;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Indexed;
import org.mongodb.morphia.annotations.Reference;

import lombok.Data;

@Data
@Entity(noClassnameStored=true,value="applications")
public class Application{

    public Application() {
    }
    
    @Id
    private ObjectId id;

    @Indexed
    @NotNull
    @Size(min = 2, max = 30)
    private String name;

    @NotNull
    @Size(min = 5)
    private String shortDescription;

    private String longDescription;

    //References
    @Reference
    private List<PriceRange> priceRanges = new ArrayList<>();

    @Reference
    private List<ClientApplication> clientApplications = new ArrayList<>();
}
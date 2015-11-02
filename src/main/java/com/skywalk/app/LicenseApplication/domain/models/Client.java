package main.java.com.skywalk.app.LicenseApplication.domain.models;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Reference;

import lombok.Data;

@Data
@Entity(noClassnameStored=true,value="clients")
public class Client{

    public Client() {
    }

    @Id
    private ObjectId id;

    @NotNull
    @Size(min = 2, max = 30)
    private String name;

    private String size;

    //Embeddables
    @Embedded
    private ContactDetails contactDetails;
    
    @Embedded
    private Liason liason;

    //References
    @Reference
    private List<License> licenses = new ArrayList<>();
    
    @Reference
    private List<Application> applications = new ArrayList<>();


}
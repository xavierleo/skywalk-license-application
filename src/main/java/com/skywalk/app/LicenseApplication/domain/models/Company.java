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
@Entity(noClassnameStored=true,value="companies")
public class Company{

    public Company() {
    }
    
    @Id    
    private ObjectId id;

    @NotNull
    @Size(min = 2, max = 30)
    private String name;

    @NotNull
    @Size(min = 5)
    private String description;

    private String industry;

    //Embeddables
    @Embedded
    private EmailConfiguration emailConfiguration;

    //References
    @Reference
    private List<User> users = new ArrayList<>();

    @Reference
    private List<Application> applications = new ArrayList<>();

}
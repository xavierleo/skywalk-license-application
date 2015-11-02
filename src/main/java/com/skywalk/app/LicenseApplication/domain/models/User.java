package main.java.com.skywalk.app.LicenseApplication.domain.models;

import javax.validation.constraints.NotNull;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

import lombok.Data;

@Data
@Entity(noClassnameStored=true,value="users")
public class User{

    public User() {
    }

    @Id
    private ObjectId id;

    @NotNull private String name;

    @NotNull private String surname;

    @NotNull private String username;

    @NotNull private String password;

    @NotNull private String role;
    
    //Embeddables
    @Embedded
    @NotNull private ContactDetails contactDetails;

}
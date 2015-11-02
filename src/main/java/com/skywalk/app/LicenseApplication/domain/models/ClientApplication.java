package main.java.com.skywalk.app.LicenseApplication.domain.models;

import lombok.Data;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Reference;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ironhulk on 02/11/2015.
 */
@Data
@Entity(noClassnameStored=true,value="client_applications")
public class ClientApplication {

    @Id
    private ObjectId id;

    //References
    @Reference
    private Client client;

    @Reference
    private Application application;

    @Reference
    private List<License> licenses = new ArrayList<>();
}

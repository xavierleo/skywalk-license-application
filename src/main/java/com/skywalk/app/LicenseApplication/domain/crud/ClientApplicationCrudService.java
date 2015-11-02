package main.java.com.skywalk.app.LicenseApplication.domain.crud;

import main.java.com.skywalk.app.LicenseApplication.domain.models.ClientApplication;
import org.bson.types.ObjectId;

import java.util.List;

/**
 * Created by ironhulk on 02/11/2015.
 */
public interface ClientApplicationCrudService extends CrudService<ClientApplication, ObjectId> {

    ClientApplication getClientApplicationByClientAndAppId(ObjectId clientId, ObjectId applicationId);

    List<ClientApplication> getClientApplicationByClientId(ObjectId clientId);
}

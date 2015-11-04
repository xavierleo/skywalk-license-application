package main.java.com.skywalk.app.LicenseApplication.domain.crud;

import main.java.com.skywalk.app.LicenseApplication.domain.models.ClientApplication;
import main.java.com.skywalk.app.LicenseApplication.domain.models.License;
import org.bson.types.ObjectId;

import java.util.List;

/**
 * Created by xavier on 2015/10/28.
 */
public interface LicenseCrudService extends CrudService<License, ObjectId> {
    License getLicenseForApplicationByClientAndAppId(ObjectId clientId, ObjectId applicationId);

    List<License> getLicenseByClientId(ObjectId clientId);
}

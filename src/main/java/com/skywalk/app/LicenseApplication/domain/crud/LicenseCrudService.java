package main.java.com.skywalk.app.LicenseApplication.domain.crud;

import main.java.com.skywalk.app.LicenseApplication.domain.models.License;
import org.bson.types.ObjectId;

/**
 * Created by xavier on 2015/10/28.
 */
public interface LicenseCrudService extends CrudService<License, ObjectId> {
}

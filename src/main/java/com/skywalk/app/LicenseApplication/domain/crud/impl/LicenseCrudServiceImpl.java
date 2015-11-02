package main.java.com.skywalk.app.LicenseApplication.domain.crud.impl;

import lombok.extern.java.Log;
import main.java.com.skywalk.app.LicenseApplication.domain.crud.LicenseCrudService;
import main.java.com.skywalk.app.LicenseApplication.domain.models.License;
import main.java.com.skywalk.app.LicenseApplication.domain.repository.CrudRepository;
import org.bson.types.ObjectId;
import org.hibernate.validator.constraints.NotBlank;
import org.mongodb.morphia.query.Query;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Created by xavier on 2015/10/28.
 */
@Log
public class LicenseCrudServiceImpl implements LicenseCrudService {
    @Override
    public void createEntity(@NotNull License entity) {
        log.info("Started Persisting: " + License.class.getName());
        CrudRepository.INSTANCE.getDatastore("license-app").save(entity);
        log.info("Completed Persisting: " + License.class.getName());
    }

    @Override
    public License findEntityById(@NotBlank ObjectId id) {
        log.info("Started findEntityById: " + License.class.getName());
        Query<License> query = CrudRepository.INSTANCE.getDatastore("license-app").createQuery(License.class);
        log.info("Compeleted findEntityById: " + License.class.getName());
        return query.field("_id").equal(id).get();
    }

    @Override
    public License findEntityByProperty(@NotBlank String property,@NotBlank String value) {
        log.info("Started findEntityById: " + License.class.getName());
        Query<License> query = CrudRepository.INSTANCE.getDatastore("license-app").createQuery(License.class);
        log.info("Compeleted findEntityById: " + License.class.getName());
        return query.field(property).equal(value).get();
    }

    @Override
    public List<License> getAllEntities() {
        log.info("Started findEntityById: " + License.class.getName());

        return CrudRepository.INSTANCE.getDatastore("license-app").createQuery(License.class).asList();
    }

    @Override
    public void updateEntity(@NotNull License entity) {
        log.info("Started findEntityById: " + License.class.getName());
        CrudRepository.INSTANCE.getDatastore("license-app").save(entity);
        log.info("Compeleted updateEntity: " + License.class.getName());
    }

    @Override
    public void deleteEntity(@NotNull License entity) {
        log.info("Started findEntityById: " + License.class.getName());
        deleteEntityById(entity.getId());
        log.info("Compeleted deleteEntity: " + License.class.getName());
    }

    @Override
    public void deleteEntityById(@NotNull ObjectId id) {
        log.info("Started findEntityById: " + License.class.getName());
        Query<License> application = CrudRepository.INSTANCE.getDatastore("license-app").createQuery(License.class).field("_id").equal(id);
        CrudRepository.INSTANCE.getDatastore("license-app").findAndDelete(application);
        log.info("Compeleted deleteEntityById: " + License.class.getName());
    }
}

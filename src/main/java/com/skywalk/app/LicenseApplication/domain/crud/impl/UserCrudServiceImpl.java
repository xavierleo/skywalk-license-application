package main.java.com.skywalk.app.LicenseApplication.domain.crud.impl;

import lombok.extern.java.Log;
import main.java.com.skywalk.app.LicenseApplication.domain.crud.UserCrudService;
import main.java.com.skywalk.app.LicenseApplication.domain.models.User;
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
public class UserCrudServiceImpl implements UserCrudService {
    @Override
    public void createEntity(@NotNull User entity) {
        log.info("Started Persisting: " + User.class.getName());
        CrudRepository.INSTANCE.getDatastore("license-app").save(entity);
        log.info("Completed Persisting: " + User.class.getName());
    }

    @Override
    public User findEntityById(@NotBlank ObjectId id) {
        log.info("Started findEntityById: " + User.class.getName());
        Query<User> query = CrudRepository.INSTANCE.getDatastore("license-app").createQuery(User.class);
        log.info("Compeleted findEntityById: " + User.class.getName());
        return query.field("_id").equal(id).get();
    }

    @Override
    public User findEntityByProperty(@NotBlank String property,@NotBlank String value) {
        log.info("Started findEntityById: " + User.class.getName());
        Query<User> query = CrudRepository.INSTANCE.getDatastore("license-app").createQuery(User.class);
        log.info("Compeleted findEntityById: " + User.class.getName());
        return query.field(property).equal(value).get();
    }

    @Override
    public List<User> getAllEntities() {
        log.info("Started findEntityById: " + User.class.getName());

        return CrudRepository.INSTANCE.getDatastore("license-app").createQuery(User.class).asList();
    }

    @Override
    public void updateEntity(@NotNull User entity) {
        log.info("Started findEntityById: " + User.class.getName());
        CrudRepository.INSTANCE.getDatastore("license-app").save(entity);
        log.info("Compeleted updateEntity: " + User.class.getName());
    }

    @Override
    public void deleteEntity(@NotNull User entity) {
        log.info("Started findEntityById: " + User.class.getName());
        deleteEntityById(entity.getId());
        log.info("Compeleted deleteEntity: " + User.class.getName());
    }

    @Override
    public void deleteEntityById(@NotNull ObjectId id) {
        log.info("Started findEntityById: " + User.class.getName());
        Query<User> application = CrudRepository.INSTANCE.getDatastore("license-app").createQuery(User.class).field("_id").equal(id);
        CrudRepository.INSTANCE.getDatastore("license-app").findAndDelete(application);
        log.info("Compeleted deleteEntityById: " + User.class.getName());
    }
}

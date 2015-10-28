package main.java.com.skywalk.app.LicenseApplication.domain.crud.impl;

import lombok.extern.java.Log;
import main.java.com.skywalk.app.LicenseApplication.domain.crud.ClientCrudService;
import main.java.com.skywalk.app.LicenseApplication.domain.models.Client;
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
public class ClientCrudServiceImpl implements ClientCrudService {
    @Override
    public void createEntity(@NotNull Client entity) {
        log.info("Started Persisting: " + Client.class.getName());
        CrudRepository.INSTANCE.getDatastore("license-app").save(entity);
        log.info("Completed Persisting: " + Client.class.getName());
    }

    @Override
    public Client findEntityById(@NotBlank ObjectId id) {
        log.info("Started findEntityById: " + Client.class.getName());
        Query<Client> query = CrudRepository.INSTANCE.getDatastore("license-app").createQuery(Client.class);
        log.info("Compeleted findEntityById: " + Client.class.getName());
        return query.field("_id").equal(id).get();
    }

    @Override
    public Client findEntityByProperty(@NotBlank String property,@NotBlank String value) {
        log.info("Started findEntityById: " + Client.class.getName());
        Query<Client> query = CrudRepository.INSTANCE.getDatastore("license-app").createQuery(Client.class);
        log.info("Compeleted findEntityById: " + Client.class.getName());
        return query.field(property).equal(value).get();
    }

    @Override
    public List<Client> getAllEntities() {
        log.info("Started findEntityById: " + Client.class.getName());

        return CrudRepository.INSTANCE.getDatastore("license-app").createQuery(Client.class).asList();
    }

    @Override
    public void updateEntity(@NotNull Client entity) {
        log.info("Started findEntityById: " + Client.class.getName());
        CrudRepository.INSTANCE.getDatastore("license-app").save(entity);
        log.info("Compeleted updateEntity: " + Client.class.getName());
    }

    @Override
    public void deleteEntity(@NotNull Client entity) {
        log.info("Started findEntityById: " + Client.class.getName());
        deleteEntityById(entity.getId());
        log.info("Compeleted deleteEntity: " + Client.class.getName());
    }

    @Override
    public void deleteEntityById(@NotNull ObjectId id) {
        log.info("Started findEntityById: " + Client.class.getName());
        Query<Client> application = CrudRepository.INSTANCE.getDatastore("license-app").createQuery(Client.class).field("_id").equal(id);
        CrudRepository.INSTANCE.getDatastore("license-app").findAndDelete(application);
        log.info("Compeleted deleteEntityById: " + Client.class.getName());
    }
}

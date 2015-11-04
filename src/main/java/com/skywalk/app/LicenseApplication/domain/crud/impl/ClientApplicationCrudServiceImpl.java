package main.java.com.skywalk.app.LicenseApplication.domain.crud.impl;

import lombok.extern.java.Log;
import main.java.com.skywalk.app.LicenseApplication.domain.crud.ClientApplicationCrudService;
import main.java.com.skywalk.app.LicenseApplication.domain.models.Client;
import main.java.com.skywalk.app.LicenseApplication.domain.models.ClientApplication;
import main.java.com.skywalk.app.LicenseApplication.domain.repository.CrudRepository;
import org.bson.types.ObjectId;
import org.hibernate.validator.constraints.NotBlank;
import org.mongodb.morphia.query.Query;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Created by ironhulk on 02/11/2015.
 */
@Log
public class ClientApplicationCrudServiceImpl implements ClientApplicationCrudService {

    @Override
    public void createEntity(@NotNull ClientApplication entity) {
        log.info("Started Persisting: " + ClientApplication.class.getName());
        CrudRepository.INSTANCE.getDatastore("license-app").save(entity);
        log.info("Completed Persisting: " + ClientApplication.class.getName());
    }

    @Override
    public ClientApplication findEntityById(@NotBlank ObjectId id) {
        log.info("Started findEntityById: " + ClientApplication.class.getName());
        Query<ClientApplication> query = CrudRepository.INSTANCE.getDatastore("license-app").createQuery(ClientApplication.class);
        log.info("Compeleted findEntityById: " + ClientApplication.class.getName());
        return query.field("_id").equal(id).get();
    }

    @Override
    public ClientApplication findEntityByProperty(@NotBlank String property,@NotBlank String value) {
        log.info("Started findEntityById: " + ClientApplication.class.getName());
        Query<ClientApplication> query = CrudRepository.INSTANCE.getDatastore("license-app").createQuery(ClientApplication.class);
        log.info("Compeleted findEntityById: " + ClientApplication.class.getName());
        return query.field(property).equal(value).get();
    }

    @Override
    public List<ClientApplication> getAllEntities() {
        log.info("Started findEntityById: " + ClientApplication.class.getName());

        return CrudRepository.INSTANCE.getDatastore("license-app").createQuery(ClientApplication.class).asList();
    }

    @Override
    public void updateEntity(@NotNull ClientApplication entity) {
        log.info("Started findEntityById: " + ClientApplication.class.getName());
        CrudRepository.INSTANCE.getDatastore("license-app").save(entity);
        log.info("Completed updateEntity: " + ClientApplication.class.getName());
    }

    @Override
    public void deleteEntity(@NotNull ClientApplication entity) {
        log.info("Started findEntityById: " + ClientApplication.class.getName());
        deleteEntityById(entity.getId());
        log.info("Completed deleteEntity: " + ClientApplication.class.getName());
    }

    @Override
    public void deleteEntityById(@NotNull ObjectId id) {
        log.info("Started findEntityById: " + ClientApplication.class.getName());
        Query<ClientApplication> application = CrudRepository.INSTANCE.getDatastore("license-app").createQuery(ClientApplication.class).field("_id").equal(id);
        CrudRepository.INSTANCE.getDatastore("license-app").findAndDelete(application);
        log.info("Completed deleteEntityById: " + ClientApplication.class.getName());
    }

    @Override
    public ClientApplication getClientApplicationByClientAndAppId(ObjectId clientId, ObjectId applicationId) {
        log.info("Started getClientApplicationByClientAndAppId: " + ClientApplication.class.getName());
        Query<ClientApplication> clientApplication = CrudRepository.INSTANCE.getDatastore("license-app").createQuery(ClientApplication.class);
        clientApplication.and(
                clientApplication.disableValidation().criteria("application.$id").equal(applicationId),
                clientApplication.disableValidation().criteria("client.$id").equal(clientId)
        );
        return clientApplication.get();
    }

    @Override
    public List<ClientApplication> getClientApplicationByClientId(ObjectId clientId) {
        log.info("Started getClientApplicationByClientAndAppId: " + ClientApplication.class.getName());
        Client c = CrudRepository.INSTANCE.getDatastore("license-app").createQuery(Client.class).field("_id").equal(clientId).get();

        Query<ClientApplication> clientApplication = CrudRepository.INSTANCE.getDatastore("license-app").createQuery(ClientApplication.class);
        clientApplication.disableValidation().criteria("client.$id").equal(clientId);
        return clientApplication.asList();
    }
}

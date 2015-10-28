package main.java.com.skywalk.app.LicenseApplication.domain.crud.impl;

import lombok.extern.java.Log;
import main.java.com.skywalk.app.LicenseApplication.domain.crud.CompanyCrudService;
import main.java.com.skywalk.app.LicenseApplication.domain.models.Company;
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
public class CompanyCrudServiceImpl implements CompanyCrudService {
    @Override
    public void createEntity(@NotNull Company entity) {
        log.info("Started Persisting: " + Company.class.getName());
        CrudRepository.INSTANCE.getDatastore("license-app").save(entity);
        log.info("Completed Persisting: " + Company.class.getName());
    }

    @Override
    public Company findEntityById(@NotBlank ObjectId id) {
        log.info("Started findEntityById: " + Company.class.getName());
        Query<Company> query = CrudRepository.INSTANCE.getDatastore("license-app").createQuery(Company.class);
        log.info("Compeleted findEntityById: " + Company.class.getName());
        return query.field("_id").equal(id).get();
    }

    @Override
    public Company findEntityByProperty(@NotBlank String property,@NotBlank String value) {
        log.info("Started findEntityById: " + Company.class.getName());
        Query<Company> query = CrudRepository.INSTANCE.getDatastore("license-app").createQuery(Company.class);
        log.info("Compeleted findEntityById: " + Company.class.getName());
        return query.field(property).equal(value).get();
    }

    @Override
    public List<Company> getAllEntities() {
        log.info("Started findEntityById: " + Company.class.getName());

        return CrudRepository.INSTANCE.getDatastore("license-app").createQuery(Company.class).asList();
    }

    @Override
    public void updateEntity(@NotNull Company entity) {
        log.info("Started findEntityById: " + Company.class.getName());
        CrudRepository.INSTANCE.getDatastore("license-app").save(entity);
        log.info("Compeleted updateEntity: " + Company.class.getName());
    }

    @Override
    public void deleteEntity(@NotNull Company entity) {
        log.info("Started findEntityById: " + Company.class.getName());
        deleteEntityById(entity.getId());
        log.info("Compeleted deleteEntity: " + Company.class.getName());
    }

    @Override
    public void deleteEntityById(@NotNull ObjectId id) {
        log.info("Started findEntityById: " + Company.class.getName());
        Query<Company> application = CrudRepository.INSTANCE.getDatastore("license-app").createQuery(Company.class).field("_id").equal(id);
        CrudRepository.INSTANCE.getDatastore("license-app").findAndDelete(application);
        log.info("Compeleted deleteEntityById: " + Company.class.getName());
    }
}

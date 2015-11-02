package main.java.com.skywalk.app.LicenseApplication.domain.crud.impl;

import lombok.extern.java.Log;
import main.java.com.skywalk.app.LicenseApplication.domain.crud.PriceRangeCrudService;
import main.java.com.skywalk.app.LicenseApplication.domain.models.PriceRange;
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
public class PriceRangeCrudServiceImpl implements PriceRangeCrudService {
    @Override
    public void createEntity(@NotNull PriceRange entity) {
        log.info("Started Persisting: " + PriceRange.class.getName());
        CrudRepository.INSTANCE.getDatastore("license-app").save(entity);
        log.info("Completed Persisting: " + PriceRange.class.getName());
    }

    @Override
    public PriceRange findEntityById(@NotBlank ObjectId id) {
        log.info("Started findEntityById: " + PriceRange.class.getName());
        Query<PriceRange> query = CrudRepository.INSTANCE.getDatastore("license-app").createQuery(PriceRange.class);
        log.info("Compeleted findEntityById: " + PriceRange.class.getName());
        return query.field("_id").equal(id).get();
    }

    @Override
    public PriceRange findEntityByProperty(@NotBlank String property,@NotBlank String value) {
        log.info("Started findEntityById: " + PriceRange.class.getName());
        Query<PriceRange> query = CrudRepository.INSTANCE.getDatastore("license-app").createQuery(PriceRange.class);
        log.info("Compeleted findEntityById: " + PriceRange.class.getName());
        return query.field(property).equal(value).get();
    }

    @Override
    public List<PriceRange> getAllEntities() {
        log.info("Started findEntityById: " + PriceRange.class.getName());

        return CrudRepository.INSTANCE.getDatastore("license-app").createQuery(PriceRange.class).asList();
    }

    @Override
    public void updateEntity(@NotNull PriceRange entity) {
        log.info("Started findEntityById: " + PriceRange.class.getName());
        CrudRepository.INSTANCE.getDatastore("license-app").save(entity);
        log.info("Compeleted updateEntity: " + PriceRange.class.getName());
    }

    @Override
    public void deleteEntity(@NotNull PriceRange entity) {
        log.info("Started findEntityById: " + PriceRange.class.getName());
        deleteEntityById(entity.getId());
        log.info("Compeleted deleteEntity: " + PriceRange.class.getName());
    }

    @Override
    public void deleteEntityById(@NotNull ObjectId id) {
        log.info("Started findEntityById: " + PriceRange.class.getName());
        Query<PriceRange> application = CrudRepository.INSTANCE.getDatastore("license-app").createQuery(PriceRange.class).field("_id").equal(id);
        CrudRepository.INSTANCE.getDatastore("license-app").findAndDelete(application);
        log.info("Compeleted deleteEntityById: " + PriceRange.class.getName());
    }
}

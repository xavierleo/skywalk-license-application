package main.java.com.skywalk.app.LicenseApplication.domain.crud.impl;

import java.util.List;

import javax.validation.constraints.NotNull;

import org.bson.types.ObjectId;
import org.hibernate.validator.constraints.NotBlank;
import org.mongodb.morphia.query.Query;

import lombok.extern.java.Log;
import main.java.com.skywalk.app.LicenseApplication.domain.crud.ApplicationCrudService;
import main.java.com.skywalk.app.LicenseApplication.domain.models.Application;
import main.java.com.skywalk.app.LicenseApplication.domain.repository.CrudRepository;

@Log
public class ApplicationCrudServiceImpl implements ApplicationCrudService {

	@Override
	public void createEntity(@NotNull Application entity) {
		log.info("Started Persisting: " + Application.class.getName());
		CrudRepository.INSTANCE.getDatastore("license-app").save(entity);
		log.info("Completed Persisting: " + Application.class.getName());
	}

	@Override
	public Application findEntityById(@NotBlank ObjectId id) {
		log.info("Started findEntityById: " + Application.class.getName());
		Query<Application> query = CrudRepository.INSTANCE.getDatastore("license-app").createQuery(Application.class);
		log.info("Compeleted findEntityById: " + Application.class.getName());
		return query.field("_id").equal(id).get();
	}

	@Override
	public Application findEntityByProperty(@NotBlank String property,@NotBlank String value) {
		log.info("Started findEntityById: " + Application.class.getName());
		Query<Application> query = CrudRepository.INSTANCE.getDatastore("license-app").createQuery(Application.class);
		log.info("Compeleted findEntityById: " + Application.class.getName());
		return query.field(property).equal(value).get();
	}

	@Override
	public List<Application> getAllEntities() {
		log.info("Started findEntityById: " + Application.class.getName());
		
		return CrudRepository.INSTANCE.getDatastore("license-app").createQuery(Application.class).asList();
	}

	@Override
	public void updateEntity(@NotNull Application entity) {
		log.info("Started findEntityById: " + Application.class.getName());
		CrudRepository.INSTANCE.getDatastore("license-app").save(entity);
		log.info("Compeleted updateEntity: " + Application.class.getName());
	}

	@Override
	public void deleteEntity(@NotNull Application entity) {
		log.info("Started findEntityById: " + Application.class.getName());
		deleteEntityById(entity.getId());
		log.info("Compeleted deleteEntity: " + Application.class.getName());
	}

	@Override
	public void deleteEntityById(@NotNull ObjectId id) {
		log.info("Started findEntityById: " + Application.class.getName());
		Query<Application> application = CrudRepository.INSTANCE.getDatastore("license-app").createQuery(Application.class).field("_id").equal(id);
		CrudRepository.INSTANCE.getDatastore("license-app").findAndDelete(application);
		log.info("Compeleted deleteEntityById: " + Application.class.getName());
	}

}

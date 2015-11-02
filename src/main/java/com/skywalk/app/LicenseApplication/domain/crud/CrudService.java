package main.java.com.skywalk.app.LicenseApplication.domain.crud;

import java.io.Serializable;
import java.util.List;

public interface CrudService<E, K extends Serializable> {
	public void createEntity(E entity);
	
	public E findEntityById(K id);
	
	public E findEntityByProperty(String property, String value);
	
	public List<E> getAllEntities();
	
	public void updateEntity(E entity);
	
	public void deleteEntity(E entity);
	
	public void deleteEntityById(K id);
}

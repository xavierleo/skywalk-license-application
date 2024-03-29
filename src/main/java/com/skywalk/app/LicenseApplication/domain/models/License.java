package main.java.com.skywalk.app.LicenseApplication.domain.models;

import java.util.Date;

import javax.validation.constraints.Future;
import javax.validation.constraints.NotNull;

import jdk.nashorn.internal.ir.annotations.Reference;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

import lombok.Data;

@Data
@Entity(noClassnameStored=true,value="licenses")
public class License{

    public License() {
    }
    
    @Id
	@NotNull private ObjectId id;

	@NotNull private String description;

	@NotNull private Date startDate;

	@NotNull @Future private Date endDate;

	@NotNull private String paymentType;

	@NotNull private Date invoiceDate;

	@NotNull private int totalRequestedUsers;

	@NotNull private int totalAvailableUsers;

	@NotNull private double licenseFee;

	@Reference
	private ClientApplication clientApplication;
}
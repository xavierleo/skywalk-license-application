package main.java.com.skywalk.app.LicenseApplication.infrastructure.configurations;

import main.java.com.skywalk.app.LicenseApplication.application.services.*;
import main.java.com.skywalk.app.LicenseApplication.application.services.impl.*;
import main.java.com.skywalk.app.LicenseApplication.domain.models.Company;
import org.glassfish.hk2.utilities.binding.AbstractBinder;

/**
 * Created by ironhulk on 03/11/2015.
 */
public class DependencyInjectionBinder extends AbstractBinder {
    @Override
    protected void configure() {
        bind(ApplicationServicesImpl.class).to(ApplicationServices.class);
        bind(ClientServicesImpl.class).to(ClientServices.class);
        bind(CompanyServiceImpl.class).to(CompanyService.class);
        bind(UserServiceImpl.class).to(UserService.class);
        bind(LicenseServicesImpl.class).to(LicenseServices.class);
        bind(PriceRangeServiceImpl.class).to(PriceRangeServices.class);
    }
}

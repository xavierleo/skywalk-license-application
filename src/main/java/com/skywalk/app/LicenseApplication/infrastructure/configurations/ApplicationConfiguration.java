package main.java.com.skywalk.app.LicenseApplication.infrastructure.configurations;

import lombok.extern.java.Log;
import org.glassfish.jersey.server.ResourceConfig;

import javax.ws.rs.ApplicationPath;

/**
 * Created by ironhulk on 03/11/2015.
 */
@Log
@ApplicationPath("/api")
public class ApplicationConfiguration extends ResourceConfig {

    public ApplicationConfiguration() {
        log.info("Setting up Resource Configurations");
        //register all restful resources
        packages("main.java.com.skywalk.app.LicenseApplication.application.rest");

        //register DI bindings
        register(new DependencyInjectionBinder());
    }
}

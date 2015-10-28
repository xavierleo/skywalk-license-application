package main.java.com.skywalk.app.LicenseApplication.domain.models;

import javax.validation.constraints.NotNull;

import lombok.Getter;

public class EmailConfiguration{

    public EmailConfiguration() {
    }

    public EmailConfiguration(String smptServer, String serverPort, String authUsername, String authPassword,
			String primaryAccountAddress, String emailSignature, String enableSMTPAuthentication,
			String enableTTLSSupport) {
		super();
		this.smptServer = smptServer;
		this.serverPort = serverPort;
		this.authUsername = authUsername;
		this.authPassword = authPassword;
		this.primaryAccountAddress = primaryAccountAddress;
		this.emailSignature = emailSignature;
		this.enableSMTPAuthentication = enableSMTPAuthentication;
		this.enableTTLSSupport = enableTTLSSupport;
	}

	@NotNull @Getter private String smptServer;

	@NotNull @Getter private String serverPort;

	@NotNull @Getter private String authUsername;

	@NotNull @Getter private String authPassword;

	@NotNull @Getter private String primaryAccountAddress;

	@NotNull @Getter private String emailSignature;

	@NotNull @Getter private String enableSMTPAuthentication;

	@NotNull @Getter private String enableTTLSSupport;


}
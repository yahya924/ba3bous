package com.igatn.extranet;

import com.igatn.extranet.app.AppProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * FRE - this project must be built as JAR, thus, it requires an entry point
 * having main() method.
 * 
 * Also, as we're using spring, we need a minimal amount of configuration to bootstrap the app,
 * this is the role of @SpringBootApplication annotation. 
 */
@SpringBootApplication
@ConfigurationPropertiesScan(basePackageClasses = AppProperties.class)
@EnableJpaRepositories("com.igatn.extranet.domainjpa.api.data")
public class ExtranetIgaTnApplication {
	
	/**
	 * FRE - the app entry point main method call a static run() which launch the app
	 * and creates the spring context.
	 * @param args
	 */
	public static void main(String[] args) {
		SpringApplication.run(ExtranetIgaTnApplication.class, args);
	}
}

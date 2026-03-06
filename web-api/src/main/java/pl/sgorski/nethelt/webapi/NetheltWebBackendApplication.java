package pl.sgorski.nethelt.webapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import pl.sgorski.nethelt.webapi.features.auth.config.properties.JwtProperties;

@SpringBootApplication
@EnableConfigurationProperties(JwtProperties.class)
public class NetheltWebBackendApplication {

	static void main(String[] args) {
		SpringApplication.run(NetheltWebBackendApplication.class, args);
	}

}

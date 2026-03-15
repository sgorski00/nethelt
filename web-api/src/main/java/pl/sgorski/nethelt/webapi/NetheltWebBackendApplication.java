package pl.sgorski.nethelt.webapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import pl.sgorski.nethelt.webapi.http.client.github.config.GithubClientProperties;
import pl.sgorski.nethelt.webapi.security.jwt.JwtProperties;

@SpringBootApplication
@EnableConfigurationProperties({
		JwtProperties.class,
		GithubClientProperties.class
})
public class NetheltWebBackendApplication {

	static void main(String[] args) {
		SpringApplication.run(NetheltWebBackendApplication.class, args);
	}

}

package pl.sgorski.nethelt.webapi.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI myOpenApiInfo() {
        var contact = new Contact();
        contact.setName("Sebastian Górski");
        contact.setEmail("sebastian.gorski@studenci.collegiumwitelona.pl");

        var info =
                new Info()
                        .title("Expense Splitter API")
                        .version("1.0.0")
                        .description("API for managing shared expenses, participants, and payments.")
                        .contact(contact);

        var securityScheme =
                new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description(
                                "USE JWT token obtained from /auth/login endpoint or via OAuth2 authorization flow.");

        return new OpenAPI()
                .info(info)
                .components(new Components().addSecuritySchemes("bearerAuth", securityScheme))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }
}

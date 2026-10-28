package br.com.tourguide.api.TourGuideApiApplication.infra.springdoc;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Configuração do Swagger (springdoc):
 * <ul>
 *   <li>O springdoc lê os controllers e gera a documentação OpenAPI em /v3/api-docs;</li>
 *   <li>A tela do Swagger fica em /swagger-ui.html;</li>
 *   <li>Registra o esquema "bearer-key" para o botão "Authorize" aceitar o token JWT do login
 *   e mandar no header Authorization das próximas chamadas.</li>
 * </ul>
 */
@Configuration
public class SpringDocConfiguration {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Tour.Guide API")
                        .description("API REST do Tour.Guide")
                        .version("v1"))
                .components(new Components()
                        .addSecuritySchemes("bearer-key", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList("bearer-key"));
    }
}

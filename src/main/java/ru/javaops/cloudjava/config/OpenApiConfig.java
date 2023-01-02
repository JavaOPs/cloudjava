package ru.javaops.cloudjava.config;

import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.javaops.cloudjava.model.User;
import ru.javaops.cloudjava.to.UserTo;

@Configuration
public class OpenApiConfig {
    //    https://github.com/springdoc/springdoc-openapi/issues/915
    //    https://springdoc.org/faq.html#how-can-i-set-a-global-header

    @Bean
    GroupedOpenApi token() {
        return GroupedOpenApi.builder()
                .group("JWT Token")
                .addOpenApiCustomizer(openApi -> {
                    openApi.addSecurityItem(new SecurityRequirement().addList("Authorization"))
                            .components(new Components()
                                    .addSecuritySchemes("Authorization", new SecurityScheme()
                                            .in(SecurityScheme.In.HEADER)
                                            .type(SecurityScheme.Type.HTTP)
                                            .scheme("basic"))
                            )
                            .info(new Info().title("JWT Token").description("""
                                    Приложение по <a href='https://javaops.ru/view/cloudjava'>курсу CloudJava</a>
                                    <p>Тестовые креденшелы:<br>
                                       - user@gmail.com / password<br>
                                       - admin@javaops.ru / admin
                                    </p>
                                    """));

                })
                .pathsToMatch("/token")
                .build();
    }

    @Bean
    GroupedOpenApi api() {
        return GroupedOpenApi.builder()
                .group("REST API")
                .addOpenApiCustomizer(openApi -> {
                    openApi.addSecurityItem(new SecurityRequirement().addList("Authorization"))
                            .components(new Components()
                                    .addSchemas("User", ModelConverters.getInstance().readAllAsResolvedSchema(User.class).schema)
                                    .addSchemas("UserTo", ModelConverters.getInstance().readAllAsResolvedSchema(UserTo.class).schema)
                                    .addSecuritySchemes("Authorization", new SecurityScheme()
                                            .in(SecurityScheme.In.HEADER)
                                            .type(SecurityScheme.Type.HTTP)
                                            .scheme("bearer")
                                            .name("JWT"))
                            )
                            .info(new Info().title("REST API").version("1.0").description("""
                                    Приложение по <a href='https://javaops.ru/view/cloudjava'>курсу CloudJava</a>
                                    <p>Авторизация через JWT Token (справа верху `Select a definition`)</p>
                                    """));
                })
                .pathsToMatch("/api/**")
                .build();
    }
}

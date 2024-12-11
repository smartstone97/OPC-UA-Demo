package com.mlx.opcuaservice.configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

/**
 * Configuration for Swagger documentation.
 *
 * @author liuyanyu
 * @version 1.0.0
 */
@Configuration
public class SwaggerConfig {
    private final String title;
    private final String version;
    private final String description;
    private final Map<?, ?> contact;
    private final Map<?, ?> licence;
    private final List<?> groups;

    /**
     * Constructor to load the configuration from the resource file.
     *
     * @param configuration the swagger configuration resource
     */
    public SwaggerConfig(SwaggerResource configuration) {
        title = configuration.value("Info", "title").toString();
        version = configuration.value("Info", "version").toString();
        description = configuration.value("Info", "description").toString();
        contact = (Map<?, ?>) configuration.value("Info", "contact");
        licence = (Map<?, ?>) configuration.value("Info", "licence");
        groups = (List<?>) configuration.value("Groups");
    }

    /**
     * @return the OpenAPI object
     */
    @Bean
    public OpenAPI getOpenAPI() {
        return new OpenAPI().info(info());
    }

    /**
     * @return the first group of OpenAPI objects
     */
    @Bean
    public GroupedOpenApi firstGroup() {
        return getGroupInfo((Map<?, ?>) groups.get(0));
    }

    private Info info() {
        return new Info()
                .title(title)
                .version(version)
                .description(description)
                .contact(contact())
                .license(license());
    }

    private Contact contact() {
        return new Contact()
                .name(contact.get("name").toString())
                .url(contact.get("url").toString())
                .email(contact.get("email").toString());
    }

    private License license() {
        return new License()
                .name(licence.get("name").toString())
                .identifier(licence.get("identifier").toString())
                .url(licence.get("url").toString());
    }

    private GroupedOpenApi getGroupInfo(Map<?, ?> whichGroup) {
        Map<?, ?> groupInfo = (Map<?, ?>) whichGroup.get("Info");
        return GroupedOpenApi.builder()
                .group(whichGroup.get("name").toString())
                .addOpenApiCustomizer(openApi -> openApi.
                        info(new Info()
                                .title(groupInfo.get("title").toString())
                                .version(groupInfo.get("version").toString())
                                .description(groupInfo.get("description").toString())
                                .contact(contact())
                                .license(license())
                        )
                )
                .pathsToMatch(whichGroup.get("path-to-match").toString().split(";"))
                .build();
    }
}

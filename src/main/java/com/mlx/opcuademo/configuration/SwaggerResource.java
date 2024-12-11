package com.mlx.opcuaservice.configuration;

import com.mlx.utilities.YamlUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

/**
 * Configuration to load Swagger resource.
 *
 * @author liuyanyu
 * @version 1.0.0
 */
@Configuration
public class SwaggerResource {
    @Value("classpath:${swagger.config-file-path}")
    private Resource resource;

    /**
     * @param keys keys to get value from resource YAML file.
     * @return value of the keys in resource YAML file.
     */
    public Object value(String... keys) {
        return YamlUtil.getValue(resource, keys);
    }
}

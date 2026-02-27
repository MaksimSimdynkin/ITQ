package ru.myproject.itqgenerator.configuration;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import ru.myproject.itqgenerator.generet.GeneratorProperties;

@Configuration
@EnableConfigurationProperties(GeneratorProperties.class)
public class GeneratorConfig {
}

package ru.myproject.itqgenerator.generet;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Setter
@Getter
@Validated
@ConfigurationProperties(prefix = "generator")
public class GeneratorProperties {

    @NotBlank
    private String baseUrl ="http://localhost:8080";

    @Min(1) @Max(1000000)
    private int n = 100;

    @NotBlank
    private String initiator = "generator";

    @NotBlank
    private String authorPrefix = "Author";

    @NotBlank
    private String titlePrefix = "Doc";

    @Min(1)@Max(1000)
    private int logEvery = 50;

    @Min(1)@Max(100)
    private int parallelism = 1;
}

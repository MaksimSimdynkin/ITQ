package ru.myproject.itq.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private int batchSize = 200;
    private Workers workers = new Workers();

    @Getter @Setter
    public static class Workers {
        private Worker submit = new Worker();
        private Worker approve = new Worker();
    }

    @Getter @Setter
    public static class Worker {
        private boolean enabled = true;
        private long fixedDelayMs = 2000;
        private String initiator = "worker";
    }
}

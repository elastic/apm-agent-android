package co.elastic.apm.android.sample.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import co.elastic.otel.agent.attach.RuntimeAttach;

@SpringBootApplication
public class BackendApplication {

    public static void main(String[] args) {
        RuntimeAttach.attachJavaagentToCurrentJvm();
        SpringApplication.run(BackendApplication.class, args);
    }
}

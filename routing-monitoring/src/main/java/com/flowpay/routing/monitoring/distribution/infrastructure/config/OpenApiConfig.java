package com.flowpay.routing.monitoring.distribution.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Metadata shown at the top of the Swagger UI / OpenAPI document. */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI routingMonitoringOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Routing Monitoring API")
                .description("Distributes customer interactions across teams and exposes the live "
                        + "state of the operation (agents, loads and queues).")
                .version("v1")
                .contact(new Contact().name("FlowPay").email("dev@flowpay.example"))
                .license(new License().name("Proprietary")));
    }
}

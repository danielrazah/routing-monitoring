package com.flowpay.routing.monitoring.distribution.infrastructure.config;

import com.flowpay.routing.monitoring.distribution.domain.routing.CardIssuesRouting;
import com.flowpay.routing.monitoring.distribution.domain.routing.LoanContractingRouting;
import com.flowpay.routing.monitoring.distribution.domain.routing.OtherSubjectsRouting;
import com.flowpay.routing.monitoring.distribution.domain.routing.RoutingStrategy;
import com.flowpay.routing.monitoring.distribution.domain.routing.SubjectRouter;
import com.flowpay.routing.monitoring.distribution.domain.service.DistributionService;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Wires the pure-domain pieces into Spring. The routing strategies stay free of any
 * annotations; we expose them here as beans and let Spring collect them into the router.
 *
 * Onboarding a new team is one line in this file (a new @Bean) and one new strategy class —
 * the existing strategies, the router and the distribution logic never change.
 */
@Configuration
@EnableConfigurationProperties(DistributionProperties.class)
public class RoutingConfig {

    @Bean
    public CardIssuesRouting cardIssuesRouting() {
        return new CardIssuesRouting();
    }

    @Bean
    public LoanContractingRouting loanContractingRouting() {
        return new LoanContractingRouting();
    }

    @Bean
    public OtherSubjectsRouting otherSubjectsRouting() {
        return new OtherSubjectsRouting();
    }

    @Bean
    public SubjectRouter subjectRouter(List<RoutingStrategy> strategies) {
        return new SubjectRouter(strategies);
    }

    @Bean
    public DistributionService distributionService() {
        return new DistributionService();
    }
}

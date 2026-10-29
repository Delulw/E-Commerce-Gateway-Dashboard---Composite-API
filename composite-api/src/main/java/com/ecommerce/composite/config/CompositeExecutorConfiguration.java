package com.ecommerce.composite.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class CompositeExecutorConfiguration {

    @Bean("compositeRequestExecutor")
    public ThreadPoolTaskExecutor compositeRequestExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(3);
        executor.setMaxPoolSize(3);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("composite-request-");
        return executor;
    }
}

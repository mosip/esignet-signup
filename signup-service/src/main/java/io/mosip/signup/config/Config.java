package io.mosip.signup.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
public class Config {

    @Value("${mosip.signup.task.core.pool.size:20}")
    private int taskCorePoolSize;

    @Value("${mosip.signup.task.max.pool.size:20}")
    private int taskMaxPoolSize;

    @Bean
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(taskCorePoolSize);
        executor.setMaxPoolSize(taskMaxPoolSize);
        executor.setThreadNamePrefix("MOSIP-SIGNUP-Async-Thread-");
        executor.initialize();
        return executor;
    }
}

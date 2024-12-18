/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package io.mosip.signup.config;

import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.redis.spring.RedisLockProvider;
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@EnableSchedulerLock(defaultLockAtLeastFor = "PT120S", defaultLockAtMostFor = "PT120S")
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
        executor.setThreadNamePrefix("ES-SIGNUP-Async-Thread-");
        executor.initialize();
        return executor;
    }

    @Bean
    public LockProvider lockProvider(RedisConnectionFactory redisConnectionFactory) {
        return new RedisLockProvider(redisConnectionFactory);
    }
}

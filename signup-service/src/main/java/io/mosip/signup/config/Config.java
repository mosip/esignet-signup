/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package io.mosip.signup.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.mosip.signup.services.CacheUtilService;
import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.redis.spring.RedisLockProvider;
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.core5.util.Timeout;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.client.RestTemplate;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;

import java.util.List;
import java.util.concurrent.Executor;

@EnableSchedulerLock(defaultLockAtLeastFor = "PT120S", defaultLockAtMostFor = "PT120S")
@Configuration
public class Config {

    @Value("${mosip.signup.task.core.pool.size:20}")
    private int taskCorePoolSize;

    @Value("${mosip.signup.task.max.pool.size:20}")
    private int taskMaxPoolSize;

    @Value("${mosip.signup.http.selftoken.restTemplate.max-connection-per-route:20}")
    private int selfTokenRestTemplateMaxConnectionPerRoute;

    @Value("${mosip.signup.http.selftoken.restTemplate.total-max-connections:100}")
    private int selfTokenRestTemplateTotalMaxConnections;

    @Value("${mosip.signup.http.selftoken.restTemplate.connect-timeout-ms:5000}")
    private int connectTimeoutMs;

    @Value("${mosip.signup.http.selftoken.restTemplate.read-timeout-ms:10000}")
    private int readTimeoutMs;

    @Bean
    public ObjectMapper objectMapper() {
        return JsonMapper.builder()
                .addModule(new JavaTimeModule())
                .build();
    }

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

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new JdkSerializationRedisSerializer());
        return template;
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public RestTemplate selfTokenRestTemplate(CacheUtilService cacheUtilService) {
        RestTemplate restTemplate = new RestTemplate();

        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setDefaultMaxPerRoute(selfTokenRestTemplateMaxConnectionPerRoute);
        connectionManager.setMaxTotal(selfTokenRestTemplateTotalMaxConnections);
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(Timeout.ofMilliseconds(connectTimeoutMs))
                .setResponseTimeout(Timeout.ofMilliseconds(readTimeoutMs))
                .build();

        CloseableHttpClient httpClient = HttpClients.custom()
                .setConnectionManager(connectionManager)
                .setDefaultRequestConfig(requestConfig)
                .disableAutomaticRetries()
                .build();

        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
        restTemplate.setRequestFactory(requestFactory);
        restTemplate.getInterceptors().add((request, body, execution) -> {
            String token = cacheUtilService.fetchAccessTokenFromIAMServer();
            request.getHeaders().set("Cookie", "Authorization="+token);
            return execution.execute(request, body);
        });
        return restTemplate;
    }

    @Bean
    public BeanPostProcessor restTemplateMessageConverterCustomizer() {
        return new BeanPostProcessor() {
            @Override
            public Object postProcessAfterInitialization(Object bean, String beanName) {
                if (bean instanceof RestTemplate) {
                    RestTemplate restTemplate = (RestTemplate) bean;
                    List<HttpMessageConverter<?>> converters = restTemplate.getMessageConverters();
                    // Making JSON converter the default by moving it to the front of the list
                    converters.stream()
                            .filter(converter -> converter instanceof MappingJackson2HttpMessageConverter)
                            .findFirst()
                            .ifPresent(converter -> {
                                converters.remove(converter);
                                converters.add(0, converter);
                            });
                    return restTemplate;
                }
                return bean;
            }
        };
    }

}

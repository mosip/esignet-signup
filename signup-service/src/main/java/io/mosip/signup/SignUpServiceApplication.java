package io.mosip.signup;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableCaching
@EnableAsync
@SpringBootApplication(scanBasePackages = "io.mosip.signup.*," +
        "io.mosip.esignet.core.config.RedisCacheConfig," +
        "io.mosip.esignet.core.config.SimpleCacheConfig,"+
        "${mosip.auth.adapter.impl.basepackage}")
public class SignUpServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SignUpServiceApplication.class, args);
    }
}

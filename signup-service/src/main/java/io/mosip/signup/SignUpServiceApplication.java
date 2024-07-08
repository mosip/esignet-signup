package io.mosip.signup;

import io.mosip.esignet.core.config.RedisCacheConfig;
import io.mosip.esignet.core.config.SimpleCacheConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableCaching
@EnableAsync
@SpringBootApplication(scanBasePackages = "io.mosip.signup.*," +
        "io.mosip.esignet.core.config.RedisCacheConfig," +
        "io.mosip.esignet.core.config.SimpleCacheConfig,"+
        "${mosip.signup.integration.impl.basepackage},"+
        "${mosip.auth.adapter.impl.basepackage}",
        scanBasePackageClasses = {SimpleCacheConfig.class, RedisCacheConfig.class})
public class SignUpServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SignUpServiceApplication.class, args);
    }
}

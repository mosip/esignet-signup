/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package io.mosip.signup;

import brave.Tracer;
import io.mosip.esignet.core.config.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.sleuth.autoconfig.TraceAutoConfiguration;
import org.springframework.context.annotation.*;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@EnableCaching
@EnableAsync
@SpringBootApplication(scanBasePackages = "io.mosip.signup.*," +
        "io.mosip.kernel.auth.defaultadapter," +
        "${mosip.signup.integration.impl.basepackage}")
@Import({SharedComponentConfig.class, RedisCacheConfig.class, SimpleCacheConfig.class,
        AccessLogSleuthConfiguration.class, TraceAutoConfiguration.class})
public class SignUpServiceApplication {

    @Bean
    public SleuthValve sleuthValve(Tracer tracer) {
        return new SleuthValve(tracer);
    }

    public static void main(String[] args) {
        SpringApplication.run(SignUpServiceApplication.class, args);
    }
}

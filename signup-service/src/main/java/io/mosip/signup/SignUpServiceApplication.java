/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package io.mosip.signup;

import io.mosip.esignet.core.config.RedisCacheConfig;
import io.mosip.esignet.core.config.SharedComponentConfig;
import io.mosip.esignet.core.config.SimpleCacheConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@EnableCaching
@EnableAsync
@SpringBootApplication(scanBasePackages = "io.mosip.signup.*," +
        "${mosip.signup.integration.impl.basepackage},"+
        "${mosip.auth.adapter.impl.basepackage}")
@Import({SharedComponentConfig.class, RedisCacheConfig.class, SimpleCacheConfig.class})
public class SignUpServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SignUpServiceApplication.class, args);
    }
}

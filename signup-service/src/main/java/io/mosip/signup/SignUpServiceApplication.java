/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package io.mosip.signup;

import io.mosip.esignet.core.config.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScans;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@EnableCaching
@EnableAsync
@SpringBootApplication
@ComponentScans({
        @ComponentScan(basePackages = "io.mosip.signup.*"),
        @ComponentScan(basePackages = "io.mosip.kernel.auth.defaultadapter"),
        @ComponentScan(basePackages = "${mosip.signup.integration.impl.basepackage}"),
        @ComponentScan(basePackages = "io.mosip.esignet.core.config", includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SleuthValve.class))
})
/*@SpringBootApplication(scanBasePackages = "io.mosip.signup.*," +
        "io.mosip.kernel.auth.defaultadapter," +
        "io.mosip.esignet.core.config.SleuthValve," +
        "${mosip.signup.integration.impl.basepackage}", )*/
@Import({SharedComponentConfig.class, RedisCacheConfig.class, SimpleCacheConfig.class, AccessLogSleuthConfiguration.class})
public class SignUpServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SignUpServiceApplication.class, args);
    }
}

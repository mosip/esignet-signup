/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package io.mosip.signup.services;

import io.mosip.signup.api.exception.IdentityVerifierException;
import io.mosip.signup.api.spi.IdentityVerifierPlugin;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

import static io.mosip.signup.api.util.ErrorConstants.PLUGIN_NOT_FOUND;

@Slf4j
@Component
public class IdentityVerifierFactory {


    @Autowired
    private List<IdentityVerifierPlugin> identityVerifiers;


   public IdentityVerifierPlugin getIdentityVerifier(String id) {
       log.info("Request to fetch identity verifier with id : {}", id);
       log.info("List of identity verifiers found : {}", identityVerifiers);
       Optional<IdentityVerifierPlugin> result = identityVerifiers.stream()
               .filter(idv -> idv.getVerifierId().equals(id) )
               .findFirst();

       log.info("Identity verifiers result : {}", result);

       if(result.isEmpty())
           throw new IdentityVerifierException(PLUGIN_NOT_FOUND);

       return result.get();
   }

}

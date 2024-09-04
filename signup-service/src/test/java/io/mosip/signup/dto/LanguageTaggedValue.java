/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package io.mosip.signup.dto;

import io.mosip.signup.util.ErrorConstants;
import io.mosip.signup.validator.Language;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

@Data
@NoArgsConstructor
public class LanguageTaggedValue implements Serializable {

    private static final long serialVersionUID = 1L;

    public LanguageTaggedValue(String language, String value){
        this.language =  language;
        this.value = value;
    }

    @Language
    private String language;

    @NotBlank(message = ErrorConstants.INVALID_VALUE)
    private String value;
}

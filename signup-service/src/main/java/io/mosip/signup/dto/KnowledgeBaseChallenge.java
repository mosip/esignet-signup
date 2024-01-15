package io.mosip.signup.dto;

import lombok.Data;

import java.util.List;

@Data
public class KnowledgeBaseChallenge {
    private List<LanguageTaggedValue> fullName;
}

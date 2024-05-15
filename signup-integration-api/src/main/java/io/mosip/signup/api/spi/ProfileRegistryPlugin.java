package io.mosip.signup.api.spi;

import io.mosip.signup.api.dto.ProfileDto;
import io.mosip.signup.api.dto.ProfileResult;
import io.mosip.signup.api.exception.InvalidProfileException;
import io.mosip.signup.api.exception.ProfileException;

public interface ProfileRegistryPlugin {

    /*
     * Validates the input data in the profileDto.
     * validation on mandatory fields, field values, allowed values should be implemented in this method.
     * Allowed action values for the captured profileDto. Allowed values are "CREATE" and "UPDATE"
     * On any error throw exception with respective errorCode
     * These errorCodes will be displayed in the UI with message from the i18n bundle
     */
    void validate(String action, ProfileDto profileDto) throws InvalidProfileException;

    /*
     * Method to create a user profile.
     * validate method is invoked on the profileDto before passing the same profileDto to createProfile method
     */
    ProfileResult createProfile(String requestId, ProfileDto profileDto) throws ProfileException;

    /*
     * Method to update a user profile.
     * validate method is invoked on the profileDto before passing the same profileDto to updateProfile method
     */
    ProfileResult updateProfile(String requestId, ProfileDto profileDto) throws ProfileException;

    /*
     * Method to get profile status for the input individualId
     * Profile status may differ from one ID registry to the other.
     * esignet-signup service accepted status are ACTIVE, INACTIVE
     */
    ProfileResult getProfileStatus(String requestId, String individualId) throws ProfileException;

    /*
     * Method to get the profile, usually used to check the existence of the profile based on the
     * input individual ID.
     */
    ProfileDto getProfile(String individualId) throws ProfileException;
}


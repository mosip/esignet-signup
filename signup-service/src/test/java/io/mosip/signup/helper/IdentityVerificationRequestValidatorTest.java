package io.mosip.signup.helper;

import io.mosip.esignet.core.exception.InvalidRequestException;
import io.mosip.signup.api.dto.FrameDetail;
import io.mosip.signup.dto.IdentityVerificationRequest;
import io.mosip.signup.util.ErrorConstants;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@RunWith(MockitoJUnitRunner.class)
public class IdentityVerificationRequestValidatorTest {

    @Mock
    IdentityVerificationRequestValidator identityVerificationRequestValidator;

    @Test
    public void testValidate_withValidRequest_thenPass() {
        IdentityVerificationRequest request = new IdentityVerificationRequest();
        request.setSlotId("validSlotId");
        request.setStepCode("validStepCode");
        request.setFrames(new ArrayList<>());
        assertDoesNotThrow(() -> identityVerificationRequestValidator.validate(request));
    }

    @Test
    public void testValidate_withNullStepCode_thenFail() {
        IdentityVerificationRequest request = new IdentityVerificationRequest();
        request.setSlotId("validSlotId");
        request.setStepCode(null);

        try{
            identityVerificationRequestValidator.validate(request);
        }catch (InvalidRequestException e){
            Assert.assertEquals(e.getErrorCode(),ErrorConstants.INVALID_STEP_CODE);
        }
    }

    @Test
    public void testValidate_withBlankStepCode_thenFail() {
        IdentityVerificationRequest request = new IdentityVerificationRequest();
        request.setSlotId("validSlotId");
        request.setStepCode("  ");
        try{
            identityVerificationRequestValidator.validate(request);
        }catch (InvalidRequestException e){
            Assert.assertEquals(e.getErrorCode(),ErrorConstants.INVALID_STEP_CODE);
        }
    }

    @Test
    public void testValidate_WithInvalidFrameContent_thenFail() {
        FrameDetail frameDetail = new FrameDetail();
        frameDetail.setFrame("  ");
        frameDetail.setOrder(1);

        List<FrameDetail> frames = new ArrayList<>();
        frames.add(frameDetail);

        IdentityVerificationRequest request = new IdentityVerificationRequest();
        request.setSlotId("validSlotId");
        request.setStepCode("validStepCode");
        request.setFrames(frames);
        try{
            identityVerificationRequestValidator.validate(request);
        }catch (InvalidRequestException e){
            Assert.assertEquals(e.getErrorCode(),ErrorConstants.INVALID_FRAME);
        }
    }

    @Test
    public void testValidate_WithInvalidFrameOrder_thenFail() {
        FrameDetail frameDetail = new FrameDetail();
        frameDetail.setFrame("validFrame");
        frameDetail.setOrder(0);

        List<FrameDetail> frames = new ArrayList<>();
        frames.add(frameDetail);

        IdentityVerificationRequest request = new IdentityVerificationRequest();
        request.setSlotId("validSlotId");
        request.setStepCode("validStepCode");
        request.setFrames(frames);
        try{
            identityVerificationRequestValidator.validate(request);
        }catch (InvalidRequestException e){
            Assert.assertEquals(e.getErrorCode(),ErrorConstants.INVALID_ORDER);
        }
    }
}

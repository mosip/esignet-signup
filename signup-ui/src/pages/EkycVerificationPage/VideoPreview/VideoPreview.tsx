import { useCallback, useEffect, useRef, useState } from "react";
import { Detector } from "react-detect-offline";
import { useTranslation } from "react-i18next";
import { useLocation } from "react-router-dom";
import Webcam from "react-webcam";

import { Button } from "~components/ui/button";
import { Icons } from "~components/ui/icons";
import {
  Step,
  StepContent,
  StepDivider,
  StepFooter,
  StepHeader,
  StepTitle,
} from "~components/ui/step";
import { DefaultEkyVerificationProp } from "~typings/types";

import {
  EkycVerificationStep,
  EkycVerificationStore,
  setCriticalErrorSelector,
  setStepSelector,
  useEkycVerificationStore,
} from "../useEkycVerificationStore";

export const VideoPreview = ({
  cancelPopup,
  settings,
}: DefaultEkyVerificationProp) => {
  const { t } = useTranslation("translation", {
    keyPrefix: "video_preview",
  });
  const webcamRef = useRef(null);

  const { setStep, setCriticalError } = useEkycVerificationStore(
    useCallback(
      (state: EkycVerificationStore) => ({
        setStep: setStepSelector(state),
        setCriticalError: setCriticalErrorSelector(state),
      }),
      []
    )
  );

  const [cancelButton, setCancelButton] = useState<boolean>(false);
  const [permissionGranted, setPermissionGranted] = useState(false);
  const [permissionErrMsg, setPermissionErrMsg] = useState({
    header: "permission_denied_header",
    description: "permission_denied_description",
  });

  // key info list for video preview page
  const keyInfoList = Object.keys(t("key_info"));

  /**
   * Handle the proceed button click, move forward to video preview page
   * @param e event
   */
  const handleContinue = (e: any) => {
    e.preventDefault();
    webcamRef.current = null;
    setStep(EkycVerificationStep.SlotCheckingScreen);
  };

  /**
   * Handle cancel button click, show the cancel alert popover
   * @param e event
   */
  const handleCancel = (e: any) => {
    e.preventDefault();
    setCancelButton(true);
  };

  /**
   * Handle the stay button click, close the cancel alert popover
   */
  const handleStay = () => {
    setCancelButton(false);
  };

  // video constraints
  const videoConstraints = {
    facingMode: "user",
  };

  const cameraPermissionCheck = () => {
    navigator.mediaDevices
      .getUserMedia({ video: true })
      .then(cameraPermissionAllowed)
      .catch(cameraPermissionDenied);
  };

  navigator.permissions.query({ name: "camera" as PermissionName}).then((permissionStatus) => {

    // Listen for changes in the permission state
    permissionStatus.onchange = () => {
      cameraPermissionCheck();
    };
  });

  useEffect(() => {
    cameraPermissionCheck();
  }, []);

  // if camera permission granted then set the state
  const cameraPermissionAllowed = useCallback((stream: MediaStream) => {
    window.videoLocalStream = stream;
    setPermissionGranted(true);
  }, []);

  // if camera permission denied then set the state
  const cameraPermissionDenied = useCallback((error: any) => {
    setPermissionGranted(false);

    // doing this type of setting the state 
    // so that it not re render anything
    // it will only render when state is  actually changed
    setPermissionErrMsg((_) =>
      error.name === "NotReadableError"
        ? _.header !== "not_accessible_header"
          ? {
              header: "not_accessible_header",
              description: "not_accessible_description",
            }
          : _
        : _.header !== "permission_denied_header"
          ? {
              header: "permission_denied_header",
              description: "permission_denied_description",
            }
          : _
    );
  }, []);

  // video preview div, it will show the video preview if camera permission granted
  // otherwise it will show a card asking for camera permission
  const videoPreviewDiv = () => {
    return (
      <>
        {permissionGranted && (
          <div className="corner-border">
            <Webcam
              height={560}
              width={560}
              audio={false}
              ref={webcamRef}
              videoConstraints={videoConstraints}
              className="rounded-lg"
            />
          </div>
        )}
        {!permissionGranted && (
          <Step className="2xl:h-full xl:h-full md:mx-0 md:rounded-2xl md:shadow-none sm:mx-0 sm:rounded-2xl">
            <StepHeader className="p-0"></StepHeader>
            <StepContent className="video-preview-disabled m-6 h-[90%] content-center text-sm md:m-0">
              <div className="flex flex-col text-center">
                <Icons.disabledCamera
                  id="camera-disabled"
                  name="camera-disabled"
                  className="mb-6 h-[52px] w-[52px] self-center"
                />
                <div className="video-preview-disabled-header pb-2">
                  {t(permissionErrMsg.header)}
                </div>
                <div className="video-preview-disabled-subheader pb-5">
                  {t(permissionErrMsg.description)}
                </div>
              </div>
            </StepContent>
            <StepFooter className="p-0"></StepFooter>
          </Step>
        )}
      </>
    );
  };

  return (
    <>
      {cancelPopup({ cancelButton, handleStay })}
      <div className="my-4 flex flex-row items-stretch justify-center gap-x-1">
        <Step className="mx-10 lg:mx-4 md:rounded-2xl md:shadow sm:mx-0 sm:rounded-2xl sm:shadow">
          <StepHeader className="px-0 py-5 sm:pb-[25px] sm:pt-[33px]">
            <StepTitle className="relative flex w-full items-center justify-center gap-x-4 text-base font-semibold">
              <div
                className="video-preview-header ml-5 w-full"
                id="video-preview-header"
              >
                {t("header")}
              </div>
            </StepTitle>
          </StepHeader>
          <StepDivider />
          <StepContent className="px-6 py-5 text-sm">
            {/* video preview for small screen */}
            <div className="hidden md:block sm:block">{videoPreviewDiv()}</div>
            <div className="scrollable-div !h-[250px] md:mt-8 sm:mt-8">
              {keyInfoList.map((keyInfo, index) => (
                <div key={index} className="mb-6">
                  <Icons.check className="mr-1 inline-block h-4 w-4 stroke-[4px] text-primary" />
                  <span className="video-preview-content">
                    {t(`key_info.${keyInfo}`)}
                  </span>
                </div>
              ))}
            </div>
          </StepContent>
          <StepDivider />
          <StepFooter className="p-5">
            <div className="flex w-full flex-row items-center justify-center gap-x-4">
              <Button
                id="cancel-preview-button"
                name="cancel-preview-button"
                variant="cancel_outline"
                className="w-full p-4 font-semibold"
                onClick={handleCancel}
              >
                {t("cancel_button")}
              </Button>
              <Detector
                render={({ online }) => (
                  <Button
                    id="proceed-preview-button"
                    name="proceed-preview-button"
                    className="w-full p-4 font-semibold"
                    onClick={handleContinue}
                    disabled={!online || !permissionGranted}
                  >
                    {t("proceed_button")}
                  </Button>
                )}
              />
            </div>
          </StepFooter>
        </Step>
        {/* video preview for large screen */}
        <div className="md:hidden sm:hidden">{videoPreviewDiv()}</div>
      </div>
    </>
  );
};

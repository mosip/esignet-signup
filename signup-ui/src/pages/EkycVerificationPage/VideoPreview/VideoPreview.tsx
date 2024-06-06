import { useCallback, useEffect, useRef, useState } from "react";
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
  const [permissionGranted, setPermissionGranted] = useState(true);
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

  useEffect(() => {
    // checking camera permission in every 1 second
    setInterval(cameraPermissionCheck, 1000);
  }, [permissionGranted]);

  // if camera permission granted then set the state
  const cameraPermissionAllowed = (stream: MediaStream) => {
    if (!permissionGranted) {
      setPermissionGranted(true);
    }
  };

  // if camera permission denied then set the state
  const cameraPermissionDenied = (error: any) => {
    if (permissionGranted) {
      setPermissionGranted(false);
      if (error.name === "NotReadableError") {
        if (permissionErrMsg.header !== "not_accessible_header") {
          setPermissionErrMsg({
            header: "not_accessible_header",
            description: "not_accessible_description",
          });
        }
      } else if (permissionErrMsg.header !== "permission_denied_header") {
        setPermissionErrMsg({
          header: "permission_denied_header",
          description: "permission_denied_description",
        });
      }
    }
  };

  // check the camera permission, if camera permission granted then set the state
  // it will work for chrome & firefox as well
  const cameraPermissionCheck = () => {
    navigator.mediaDevices
      .getUserMedia({ video: true })
      .then(cameraPermissionAllowed)
      .catch(cameraPermissionDenied);
  };

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
              className="rounded-lg"
            />
          </div>
        )}
        {!permissionGranted && (
          <Step className="2xl:h-full xl:h-full md:mx-0 md:rounded-2xl md:shadow-none sm:mx-0 sm:rounded-2xl">
            <StepHeader className="p-0"></StepHeader>
            <StepContent className="m-6 h-[90%] content-center rounded-[10px] bg-[#F8F8F8] text-sm md:m-0">
              <div className="flex flex-col text-center">
                <Icons.disabledCamera
                  id="camera-disabled"
                  name="camera-disabled"
                  className="mb-6 h-[52px] w-[52px] self-center"
                />
                <div className="color-[#313131] pb-2 text-base font-semibold leading-5">
                  {t(permissionErrMsg.header)}
                </div>
                <div className="color-[#7E7E7E] pb-5 text-sm font-normal leading-4">
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
      <div className="m-3 mt-10 flex flex-row items-stretch justify-center gap-x-1 sm:mb-20">
        <Step className="mx-10 lg:mx-4 md:rounded-2xl md:shadow sm:rounded-2xl sm:shadow">
          <StepHeader className="px-0 py-5 sm:pb-[25px] sm:pt-[33px]">
            <StepTitle className="relative flex w-full items-center justify-center gap-x-4 text-base font-semibold">
              <div
                className="ml-5 w-full text-[22px] font-semibold"
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
                  <Icons.check className="mr-1 inline-block h-4 w-4 stroke-[4px] text-orange-500" />
                  <span>{t(`key_info.${keyInfo}`)}</span>
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
              <Button
                id="proceed-preview-button"
                name="proceed-preview-button"
                className="w-full p-4 font-semibold"
                onClick={handleContinue}
                disabled={!permissionGranted}
              >
                {t("proceed_button")}
              </Button>
            </div>
          </StepFooter>
        </Step>
        {/* video preview for large screen */}
        <div className="md:hidden sm:hidden">{videoPreviewDiv()}</div>
      </div>
    </>
  );
};

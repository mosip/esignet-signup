import { useCallback, useEffect, useRef, useState } from "react";
import { useTranslation } from "react-i18next";
import Webcam from "react-webcam";

import { PUBLISH_TOPIC, SUBSCRIBE_TOPIC, WS_URL } from "~constants/routes";
import { Button } from "~components/ui/button";
import useStompClient from "~pages/shared/stompWs";
import { WS_BASE_URL } from "~services/api.service";
import langConfigService from "~services/langConfig.service";
import {
  DefaultEkyVerificationProp,
  IdentityVerificationRequestDto,
  IdentityVerificationResponseDto,
  IdentityVerificationState,
  IdvFeedbackEnum,
  IdvFrames,
} from "~typings/types";
import LoadingIndicator from "~/common/LoadingIndicator";

import {
  EkycVerificationStep,
  EkycVerificationStore,
  errorBannerMessageSelector,
  setErrorBannerMessageSelector,
  setIsNoBackgroundSelector,
  setSlotIdSelector,
  setStepSelector,
  slotIdSelector,
  useEkycVerificationStore,
} from "../useEkycVerificationStore";
import { EkycStatusAlert } from "./components/EkycStatusAlert";

export const VerificationScreen = ({
  cancelPopup,
  settings,
}: DefaultEkyVerificationProp) => {
  const { t } = useTranslation("translation", {
    keyPrefix: "verification_screen",
  });
  const webcamRef = useRef(null);
  const [timer, setTimer] = useState<number | null>(null);
  const [message, setMessage] = useState<string | any>(null);
  const [alertConfig, setAlertConfig] = useState<object | null>(null);
  const [colorVerification, setColorVerification] = useState<boolean>(false);
  const [bgColor, setBgColor] = useState<string | null>(null);
  const [imageFrames, setImageFrames] = useState<IdvFrames[]>([]);

  // let imageFrames: IdvFrames[] = [];
  let identityVerification: IdentityVerificationState | null = {
    stepCode: null,
    fps: null,
    totalDuration: null,
    startupDelay: 10,
    feedbackType: null,
    feedbackCode: null,
  };
  const [langMap, setLangMap] = useState<any>({});
  let captureFrameInterval: any = null;
  let publishMessageInterval: any = null;
  let newCaptureInterval: any = null;

  let frameArray: any[] = [];
  let frameCount = 0;

  // getting stored data from the store
  const {
    setIsNoBackground,
    setErrorBannerMessage,
    errorBannerMessage,
    slotId,
    setStep,
    setSlotId,
  } = useEkycVerificationStore(
    useCallback(
      (state: EkycVerificationStore) => ({
        setIsNoBackground: setIsNoBackgroundSelector(state),
        setErrorBannerMessage: setErrorBannerMessageSelector(state),
        errorBannerMessage: errorBannerMessageSelector(state),
        slotId: slotIdSelector(state),
        setStep: setStepSelector(state),
        setSlotId: setSlotIdSelector(state),
      }),
      []
    )
  );

  const webSocketUrl = `${WS_BASE_URL}${WS_URL}?slotId=${slotId}`;

  const { client, connected, publish, subscribe, unsubscribe } =
    useStompClient(webSocketUrl);
  // const slotId = "123456";
  // temporary button ref variable
  const buttonRef = useRef(null);

  // capturing frame from the web camera
  // const captureFrame = useCallback(() => {
  //   if (webcamRef && webcamRef.current) {
  //     let frameArray = imageFrames.map((_) => _);
  //     const imageSrc = (webcamRef.current as Webcam).getScreenshot();

  //     if (imageSrc) {
  //       frameArray.push({
  //         frame: imageSrc,
  //         order: frameArray.length,
  //       });
  //       console.log(
  //         "before setimage frame frameArray",
  //         frameArray.length,
  //         imageFrames.length
  //       );
  //       setImageFrames(frameArray);
  //       console.log(
  //         "after setimage frame frameArray",
  //         frameArray.length,
  //         imageFrames.length
  //       );
  //     }
  //   }
  // }, [webcamRef, imageFrames]);

  const isError = false;

  /**
   * Sending message to web socket
   * @param request
   */
  const sendMessage = (request: any) => {
    if (imageFrames.length) {
      request.frames = imageFrames.map((frame: IdvFrames) => {
        return { ...frame };
      });
    } else {
      request.frames = Array.from(Array(4).keys()).map((i: number) => {
        return { frame: "", order: i };
      });
    }
    publish(PUBLISH_TOPIC, JSON.stringify(request));
    setImageFrames([]);
  };

  const handleRetry = (e: any) => {
    e.preventDefault();
    setSlotId(null);
    setStep(EkycVerificationStep.SlotCheckingScreen);
  };

  // timer useEffect
  useEffect(() => {
    if (timer && timer > 0) {
      const intervalId = setTimeout(() => {
        setTimer(timer - 1);
      }, 1000);
      setMessage(["welcome_message", { count: timer }]);
      return () => clearTimeout(intervalId);
    }
  }, [timer]);

  useEffect(() => {
    setIsNoBackground(!!colorVerification);
    if (isError && !colorVerification) {
      setErrorBannerMessage(
        `Verification was unsuccessful, restarting in ${timer} seconds`
      );
    }
    if (timer === 0) {
      setErrorBannerMessage(null);
    }
  }, [isError, timer, colorVerification]);

  const videoConstraints = {
    aspectRatio:
      window.screen.availWidth <= 1280
        ? window.screen.availWidth / window.innerHeight
        : 1.6,
  };

  // stompjs connection established
  const onConnect = () => {
    const request: IdentityVerificationRequestDto = {
      slotId: slotId ?? "",
      stepCode: "START",
      frames: [],
    };

    // as soon as we establish the connection, we will send the process frame request
    sendMessage(request);
  };

  // useEffect(() => {
  //   // checking camera permission in every 1 second
  //   const cameraCheckInterval = setInterval(
  //     cameraDeviceCheck,
  //     1000
  //   );
  //   return () => clearInterval(cameraCheckInterval);
  // }, []);

  // const cameraDeviceCheck = () => {
  //   navigator.mediaDevices
  //     .getUserMedia({ video: true })
  //     .catch(stopEkycVerificationCheck);
  // };

  useEffect(() => {
    // check the camera permission, if camera permission granted then set the state
    // it will work for chrome & firefox as well
    const cameraPermissionCheck = () => {
      navigator.mediaDevices
        .getUserMedia({ video: true })
        .catch(stopEkycVerificationCheck);
    };

    // // checking camera permission in every 1 second
    // const cameraPermissionCheckInterval = setInterval(
    //   cameraPermissionCheck,
    //   1000
    // );

    cameraPermissionCheck();
    return () => {
      // clearInterval(cameraPermissionCheckInterval);
      if (window.localStream) {
        window.localStream.getTracks().forEach(track => track.stop());
      }
    };
  }, []);

  const stopEkycVerificationCheck = (err: DOMException) => {
    if (connected) {
      unsubscribe();
      client.deactivate();
    }
    setStep(EkycVerificationStep.IdentityVerificationStatus);
  }

  const convertResponseToState = (
    res: IdentityVerificationResponseDto
  ): IdentityVerificationState => {
    let temp: IdentityVerificationState = {
      stepCode: null,
      fps: null,
      totalDuration: null,
      startupDelay: 10,
      feedbackType: null,
      feedbackCode: null,
    };
    if (res) {
      temp = {
        ...temp,
        ...(res.step?.code && { stepCode: res.step.code }),
        ...(res.step?.framesPerSecond && { fps: res.step.framesPerSecond }),
        ...(res.step?.durationInSeconds && {
          totalDuration: res.step.durationInSeconds,
        }),
        ...(res.step?.startupDelayInSeconds && {
          startupDelay: res.step.startupDelayInSeconds,
        }),
        ...(res.feedback?.type && { feedbackType: res.feedback.type }),
        ...(res.feedback?.code && { feedbackCode: res.feedback.code }),
      };
    }
    return temp;
  };

  const checkPreviousState = (
    res: IdentityVerificationResponseDto
  ): IdentityVerificationState | null => {
    let temp: any = identityVerification;
    let tempRes = convertResponseToState(res);

    if (tempRes.stepCode !== null && tempRes.stepCode !== temp?.stepCode) {
      temp = {
        ...temp,
        ...tempRes,
      };
    } else {
      temp = {
        ...temp,
        feedbackType: tempRes.feedbackType,
        feedbackCode: tempRes.feedbackCode,
      };
    }
    identityVerification = temp;
    return temp;
  };

  const redirectToConsent = () => {
    unsubscribe();
    console.log("deactivate inside redirect to consent");
    client.deactivate();
    const consentUrl = settings?.configs["signin.redirect-url"].replace(
      "authorize",
      "consent"
    );
    const encodedIdToken = window.location.hash;
    window.onbeforeunload = null;
    window.location.replace(`${consentUrl}${encodedIdToken}`);
  };

  const endWithSuccess = () => {
    resetEverything();
    setStep(EkycVerificationStep.IdentityVerificationStatus);
  };

  const checkFeedback = (currentStep: IdentityVerificationState) => {
    setErrorBannerMessage(null);
    switch (currentStep.feedbackType) {
      case IdvFeedbackEnum.MESSAGE:
        setMessage([`messages.${currentStep.feedbackCode}`]);
        break;
      case IdvFeedbackEnum.COLOR:
        setColorVerification(true);
        setBgColor(currentStep.feedbackCode);
        setMessage(["focus_on_screen_message"]);
        break;
      case IdvFeedbackEnum.ERROR:
        setErrorBannerMessage(t(`errors.${currentStep.feedbackCode}`));
        setColorVerification(false);
        setMessage(null);
        break;
      default:
        break;
    }
  };

  // const endResponseCheck = (currentStep: IdentityVerificationState | null) => {
  //   if (currentStep === null) {
  //     return;
  //   }
  //   unsubscribe();
  //   client.deactivate();
  //   if (
  //     currentStep.feedbackType === IdvFeedbackEnum.MESSAGE &&
  //     currentStep.feedbackCode === "success_check"
  //   ) {
  //     endWithSuccess(t("successful_header"))
  //   } else {
  //     setAlertConfig({
  //       icon: "fail",
  //       header: t("unsuccessful_header"),
  //       subHeader: t("unsuccessful_subheader"),
  //       footer: (
  //         <Button
  //           id="retry-button"
  //           className="my-4 h-16 w-full"
  //           onClick={handleRetry}
  //         >
  //           Retry
  //         </Button>
  //       ),
  //     });
  //   }
  // };

  const resetEverything = () => {
    // when stepcode is end, then it will clear the interval
    // clearing capture frame & publish message interval
    clearInterval(captureFrameInterval);
    clearInterval(publishMessageInterval);
    clearInterval(newCaptureInterval);
    setErrorBannerMessage(null);
    setColorVerification(false);
    setMessage([""]);
  };

  /**
   * Getting response from the web socket
   * @param response
   */
  const receiveMessage = (response: any) => {
    const res = JSON.parse(response.body);
    const previousState = identityVerification;
    const currentState = checkPreviousState(res);

    if (currentState) {
      if (currentState.stepCode === "END") {
        endWithSuccess();
      } else if (previousState?.stepCode !== currentState?.stepCode) {
        resetEverything();

        const request = {
          slotId: slotId ?? "",
          stepCode: currentState.stepCode,
          frames: [],
        };
        // adding timer to show
        setTimer(currentState.startupDelay);
        // setting delay in startup
        setTimeout(() => {
          // setting the framerate to capture images
          setMessage([`stepCodes.${currentState.stepCode}`]);

          newCaptureInterval = setInterval(
            () => {
              captureNewFrame(request, currentState.fps ?? 3);
            },
            10000 / (currentState.fps ?? 3)
          );
          checkFeedback(currentState);
        }, currentState.startupDelay * 1000);
      } else {
        checkFeedback(currentState);
      }
    }
  };

  const captureNewFrame = (request: any, fps: number) => {
    console.log("capture new frame");
    if (frameArray.length >= fps) {
      request.frames = frameArray.length
        ? frameArray.map((frame: IdvFrames) => {
            return { ...frame };
          })
        : Array.from(Array(4).keys()).map((i: number) => {
            return { frame: "", order: i };
          });
      console.log(request)
      publish(PUBLISH_TOPIC, JSON.stringify(request));
      frameArray = [];
    } else {
      if (webcamRef && webcamRef.current) {
        const imageSrc = (webcamRef.current as Webcam).getScreenshot();
        console.log(frameArray)
        if (imageSrc) {
          frameArray.push({
            frame: imageSrc,
            order: frameCount++,
          });
        }
      }
    }
  };

  // useEffect to know when the connection is established
  // then subscribe to the topic and call onConnect
  useEffect(() => {
    if (connected) {
      console.log("subscribe")
      subscribe(`${SUBSCRIBE_TOPIC}${slotId}`, receiveMessage);

      onConnect();
    }
  }, [connected]);

  // activate the socket connection
  const activateClient = () => {
    client?.activate();
  };

  // temporary useEffect for establishing
  // socket connection through button click
  useEffect(() => {
    if (buttonRef.current) {
      setTimeout(() => {
        if (buttonRef.current) (buttonRef.current as HTMLElement).click();
      }, 10000);
    }
  }, [buttonRef]);

  // useEffect to deactivate the socket connection
  useEffect(() => {
    langConfigService.getLangCodeMapping().then((langMap) => {
      setLangMap(langMap);
    });
    return () => {
      console.log("deactivate inside useEffect");
      client?.deactivate();
    };
  }, []);

  return alertConfig !== null ? (
    <EkycStatusAlert config={alertConfig} />
  ) : (
    <div className="sm:pb-[4em]">
      {!connected ? (
        <div className="video-message sm:w-[90vw]">
          <LoadingIndicator
            message="please_wait"
            msgParam="Loading. Please wait....."
            iconClass="video-message-loading"
            divClass=""
          />
        </div>
      ) : (
        !errorBannerMessage &&
        message && <div className="video-message sm:w-[90vw]">{t(...message)}</div>
      )}
      <div
        className={
          colorVerification
            ? `no-video-border`
            : "video-border mx-auto my-3 w-max sm:w-[90vw]"
        }
        style={colorVerification && bgColor ? { backgroundColor: bgColor } : {}}
      >
        <Webcam
          audio={false}
          ref={webcamRef}
          className={
            colorVerification
              ? "hidden"
              : "h-[500px] w-[800px] rounded-lg lg:h-auto lg:w-[750px] md:h-auto md:w-[90vw] sm:h-auto sm:w-[90vw]"
          }
          videoConstraints={videoConstraints}
          screenshotFormat="image/jpeg"
        />
      </div>
      {/* temporary button for socket connection */}
      <button
        ref={buttonRef}
        hidden
        type="button"
        onClick={() => activateClient()}
      >
        Send Message
      </button>
    </div>
  );
};

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
  IdvFrames,
  KeyValueStringObject,
  KycProviderDetail,
  KycProviderDetailProp,
} from "~typings/types";

import {
  EkycVerificationStore,
  errorBannerMessageSelector,
  kycProviderDetailSelector,
  setErrorBannerMessageSelector,
  setIsNoBackgroundSelector,
  slotIdSelector,
  useEkycVerificationStore,
} from "../useEkycVerificationStore";
import { EkycStatusAlert } from "./components/EkycStatusAlert";

export const VerificationScreen = ({
  cancelPopup,
  settings,
}: DefaultEkyVerificationProp) => {
  const { t, i18n } = useTranslation("translation", {
    keyPrefix: "verification_screen",
  });
  const webcamRef = useRef(null);
  const [timer, setTimer] = useState<number | null>(null);
  const [message, setMessage] = useState<string | null>(null);
  const [alertConfig, setAlertConfig] = useState<object | null>(null);
  const [colorVerification, setColorVerification] = useState<boolean>(false);
  const [bgColor, setBgColor] = useState<string | null>(null);
  const [imageFrames, setImageFrames] = useState<IdvFrames[]>([]);
  const [identityVerification, setIdentityVerification] =
    useState<IdentityVerificationState | null>(null);
  const [langMap, setLangMap] = useState<any>({});
  let captureFrameInterval: any = null;
  let publishMessageInterval: any = null;

  // getting stored data from the store
  const {
    setIsNoBackground,
    setErrorBannerMessage,
    errorBannerMessage,
    slotId,
    kycProviderDetail,
  } = useEkycVerificationStore(
    useCallback(
      (state: EkycVerificationStore) => ({
        setIsNoBackground: setIsNoBackgroundSelector(state),
        setErrorBannerMessage: setErrorBannerMessageSelector(state),
        errorBannerMessage: errorBannerMessageSelector(state),
        slotId: slotIdSelector(state),
        kycProviderDetail: kycProviderDetailSelector(state),
      }),
      []
    )
  );

  const webSocketUrl = `${WS_BASE_URL}${WS_URL}?slotId=${slotId}`;

  const { client, connected, publish, subscribe } =
    useStompClient(webSocketUrl);
  // const slotId = "123456";
  // temporary button ref variable
  const buttonRef = useRef(null);

  // capturing frame from the web camera
  const captureFrame = useCallback(() => {
    if (webcamRef && webcamRef.current) {
      const imageSrc = (webcamRef.current as Webcam).getScreenshot();

      if (imageSrc) {
        let frameArray = imageFrames;
        frameArray.push({
          frame: imageSrc,
          order: frameArray.length,
        });
        setImageFrames(frameArray);
      }
    }
  }, [webcamRef]);

  const isError = false;

  /**
   * Sending message to web socket
   * @param request
   */
  const sendMessage = (request: any) => {
    console.log(
      "*****************************Sending Message*****************************"
    );
    console.log(request);
    request.frames = imageFrames.map((frame: IdvFrames) => {
      return { frame: "", order: frame.order };
    });
    publish(PUBLISH_TOPIC, JSON.stringify(request));
    setImageFrames([]);
  };

  // timer useEffect
  useEffect(() => {
    if (timer && timer > 0) {
      const intervalId = setTimeout(() => {
        setTimer(timer - 1);
      }, 1000);
      setMessage(t("welcome_message", { count: timer }));
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

  const getCurrentLangMsg = (
    key: KycProviderDetailProp,
    prop: string
  ): string => {
    const currentLang = langMap[i18n.language];
    // const msg: any = kycProviderDetail?[key]?[prop]?[currentLang];
    let msg = "default";
    let temp: KycProviderDetail | KeyValueStringObject | null =
      kycProviderDetail;
    if (temp && temp[key]) {
      temp = temp[key] as KeyValueStringObject;
      if (temp && temp[prop]) {
        temp = temp[prop] as KeyValueStringObject;
        if (temp && temp[currentLang]) {
          msg = temp[currentLang] as string;
        }
      }
    }
    return msg;
  };

  // stompjs connection established
  const onConnect = () => {
    const request: IdentityVerificationRequestDto = {
      slotId: slotId ?? "",
      stepCode: "START",
      frames: [],
    };

    // publish(PUBLISH_TOPIC, JSON.stringify(request));
    // as soon as we establish the connection, we will send the process frame request
    sendMessage(request);
  };

  const checkPreviousState = (
    res: IdentityVerificationResponseDto
  ): IdentityVerificationState | null => {
    let temp = identityVerification;
    if (res.step?.code !== temp?.stepCode) {
      temp = {
        ...temp,
        stepCode: res.step?.code ?? null,
        fps: res.step?.framesPerSecond ?? null,
        totalDuration: res.step?.durationInSeconds ?? null,
        startupDelay: res.step?.startupDelayInSeconds ?? 10,
        feedbackType: res.feedback?.type ?? null,
        feedbackCode: res.feedback?.code ?? null,
      };
      setIdentityVerification(temp);
    }
    return temp;
  };

  const checkFeedback = (currentStep: IdentityVerificationState) => {
    switch (currentStep.feedbackType) {
      case "MESSAGE":
        setMessage(
          getCurrentLangMsg("messages", currentStep.feedbackCode ?? "default")
        );
        break;
      case "COLOR":
        setColorVerification(true);
        setBgColor(currentStep.feedbackCode);
        break;
      case "ERROR":
        setErrorBannerMessage(
          getCurrentLangMsg("errors", currentStep.feedbackCode ?? "default")
        );
        break;
      default:
        break;
    }
  };

  const endResponseCheck = (currentStep: IdentityVerificationState) => {
    if (currentStep.feedbackType === "MESSAGE") {
      setAlertConfig({
        icon: "success",
        header: getCurrentLangMsg(
          "messages",
          currentStep?.feedbackCode ?? "default"
        ),
        subHeader: "Please wait while we finalize the process",
        footer: null,
      });
    } else if (currentStep.feedbackType === "ERROR") {
      setAlertConfig({
        icon: "fail",
        header: getCurrentLangMsg(
          "errors",
          currentStep?.feedbackCode ?? "default"
        ),
        subHeader: "Oops! We were unable to complete the eKYC verification.",
        footer: (
          <Button id="retry-button" className="my-4 h-16 w-full">
            Retry
          </Button>
        ),
      });
    }
  };

  /**
   * Getting response from the web socket
   * @param response
   */
  const receiveMessage = (response: any) => {
    const res = JSON.parse(response.body);
    const previousState = identityVerification;
    const currentState = checkPreviousState(res);

    console.log(
      "******************************Getting Response from Socket******************************"
    );
    console.log(res);
    if (currentState) {
      if (currentState.stepCode === "END") {
        // when stepcode is end, then it will clear the interval
        // clearing capture frame & publish message interval
        clearInterval(captureFrameInterval);
        clearInterval(publishMessageInterval);

        endResponseCheck(currentState);
      } else if (previousState?.stepCode !== currentState?.stepCode) {
        // if stepcode is different then it will executed
        // clearing capture frame & publish message interval
        clearInterval(captureFrameInterval);
        clearInterval(publishMessageInterval);
        setColorVerification(false);
        setMessage("")
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
          captureFrameInterval = setInterval(
            captureFrame,
            Math.floor(10000 / (currentState?.fps ?? 3))
          );
          setMessage(
            getCurrentLangMsg("stepCodes", currentState?.stepCode ?? "default")
          );

          // sending image frame after every 10 seconds,
          // currently static, later will change to dynamic
          publishMessageInterval = setInterval(
            () => sendMessage(request),
            10000
          );
          checkFeedback(currentState);
        }, currentState.startupDelay * 1000);
      } else {
        checkFeedback(currentState);
      }
    }
  };

  // useEffect to know when the connection is established
  // then subscribe to the topic and call onConnect
  useEffect(() => {
    if (connected) {
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
      client?.deactivate();
    };
  }, []);

  return alertConfig !== null ? (
    <EkycStatusAlert config={alertConfig} />
  ) : (
    <div className="sm:pb-[4em]">
      {!errorBannerMessage && message && (
        <div className="video-message sm:w-[90vw]">{message}</div>
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

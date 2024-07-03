import { useCallback, useEffect, useRef, useState } from "react";
import { useTranslation } from "react-i18next";
import Webcam from "react-webcam";

import { PUBLISH_TOPIC, SUBSCRIBE_TOPIC, WS_URL } from "~constants/routes";
import { Button } from "~components/ui/button";
import useStompClient from "~pages/shared/stompWs";
import { WS_BASE_URL } from "~services/api.service";
import { DefaultEkyVerificationProp } from "~typings/types";

import {
  EkycVerificationStore,
  errorBannerMessageSelector,
  setErrorBannerMessageSelector,
  setIsNoBackgroundSelector,
  useEkycVerificationStore,
} from "../useEkycVerificationStore";
import { EkycStatusAlert } from "./components/EkycStatusAlert";

type frameObj = {
  frame: string | null;
  order: number;
};

export const VerificationScreen = ({
  cancelPopup,
  settings,
}: DefaultEkyVerificationProp) => {
  const { t } = useTranslation("translation", {
    keyPrefix: "verification_screen",
  });
  const webcamRef = useRef(null);
  const [timer, setTimer] = useState<number | null>(null);
  const [message, setMessage] = useState<string | null>(null);
  const [alertConfig, setAlertConfig] = useState<object | null>(null);
  const [colorVerification, setColorVerification] = useState<boolean>(false);
  const [bgColor, setBgColor] = useState<string | null>(null);
  const [imageFrames, setImageFrames] = useState<frameObj[]>([]);
  let captureFrameInterval: any = null;
  const slotId = "123456";
  // temporary button ref variable
  const buttonRef = useRef(null);

  const webSocketUrl = `${WS_BASE_URL}${WS_URL}?slotId=${slotId}`;

  const { client, connected, publish, subscribe } =
    useStompClient(webSocketUrl);

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

  // getting stored data from the store
  const { setIsNoBackground, setErrorBannerMessage, errorBannerMessage } =
    useEkycVerificationStore(
      useCallback(
        (state: EkycVerificationStore) => ({
          setIsNoBackground: setIsNoBackgroundSelector(state),
          setErrorBannerMessage: setErrorBannerMessageSelector(state),
          errorBannerMessage: errorBannerMessageSelector(state),
        }),
        []
      )
    );

  /**
   * Sending message to web socket
   * @param request
   */
  const sendMessage = (request: any) => {
    console.log(
      "*****************************Sending Message*****************************"
    );
    console.log(request);
    // request.frames = imageFrames;
    publish(PUBLISH_TOPIC, JSON.stringify(request));
    setImageFrames([]);

    if (request.stepCode < 5) {
      setTimeout(() => {
        request.stepCode = request.stepCode
          ? (parseInt(request.stepCode) + 1).toString()
          : "0";
        sendMessage(request);
      }, 10000);
    } else {
      client?.deactivate();
    }
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

  // stompjs connection established
  const onConnect = () => {
    const request = {
      slotId: "123456",
      stepCode: "0",
      frames: [],
    };
    // as soon as we establish the connection, we will send the process frame request
    sendMessage(request);
  };

  /**
   * Getting response from the web socket
   * @param response
   */
  const gettingResponseFromSocket = (response: any) => {
    const res = JSON.parse(response.body);
    console.log(
      "******************************Getting Response from Socket******************************"
    );
    console.log(res);
    clearInterval(captureFrameInterval);
    captureFrameInterval = setInterval(
      captureFrame,
      // Math.floor(10000 / res.step.framesPerSecond)
      Math.floor(10000)
    );

    switch (res.feedback.code) {
      case "0":
        // setTimer(res.step.startupDelayInSeconds);
        setTimer(10);
        break;
      case "1":
        setColorVerification(true);
        setBgColor("blue");
        setMessage(t("focus_on_screen_message"));
        break;
      case "2":
        setColorVerification(true);
        setBgColor("#E68500");
        break;
      case "3":
        setColorVerification(true);
        setBgColor("rgb(0, 255, 0)");
        break;
      case "4":
        setColorVerification(false);
        setMessage("Initiating ID verification");
        break;
      case "5":
        setAlertConfig({
          icon: "success",
          header: "Verification Successful!",
          subHeader: "Please wait while we finalize the process",
          footer: null,
        });
        break;
      case 7:
        break;
      case 9:
        setAlertConfig({
          icon: "fail",
          header: "Verification Unsuccessful!",
          subHeader: "Oops! We were unable to complete the eKYC verification.",
          footer: (
            <Button id="retry-button" className="my-4 h-16 w-full">
              Retry
            </Button>
          ),
        });
        break;
    }
  };

  // useEffect to know when the connection is established
  // then subscribe to the topic and call onConnect
  useEffect(() => {
    if (connected) {
      subscribe(`${SUBSCRIBE_TOPIC}${slotId}`, gettingResponseFromSocket);

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

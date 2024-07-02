export const VerificationScreen = () => {
  return <h1>Verification Screen</h1>;
};



// import { useCallback, useEffect, useRef, useState } from "react";
// import { useTranslation } from "react-i18next";
// import Webcam from "react-webcam";
// import { io } from "socket.io-client";

// import { Button } from "~components/ui/button";

// import {
//   EkycVerificationStore,
//   errorBannerMessageSelector,
//   setErrorBannerMessageSelector,
//   setIsNoBackgroundSelector,
//   useEkycVerificationStore,
// } from "../useEkycVerificationStore";
// import { EkycStatusAlert } from "./components/EkycStatusAlert";

// type frameObj = {
//   frame: string | null;
//   order: number;
// };

// export const VerificationScreen = () => {
//   const webcamRef = useRef(null);
//   const [timer, setTimer] = useState<number | null>(null);
//   const [message, setMessage] = useState<string | null>(null);
//   const [alertConfig, setAlertConfig] = useState<object | null>(null);
//   const [colorVerification, setColorVerification] = useState<boolean>(false);
//   const [bgColor, setBgColor] = useState<string | null>(null);
//   const [imageFrames, setImageFrames] = useState<frameObj[]>([]);
//   let captureFrameInterval: any = null;
//   let socket: any = null;

//   const captureFrame = useCallback(() => {
//     if (webcamRef && webcamRef.current) {
//       const imageSrc = (webcamRef.current as Webcam).getScreenshot();

//       if (imageSrc) {
//         let frameArray = imageFrames;
//         frameArray.push({
//           frame: imageSrc,
//           order: frameArray.length,
//         });
//         setImageFrames(frameArray);
//       }
//     }
//   }, [webcamRef]);

//   const { t } = useTranslation("translation", {
//     keyPrefix: "verification_screen",
//   });
//   const isError = false;

//   const { setIsNoBackground, setErrorBannerMessage, errorBannerMessage } =
//     useEkycVerificationStore(
//       useCallback(
//         (state: EkycVerificationStore) => ({
//           setIsNoBackground: setIsNoBackgroundSelector(state),
//           setErrorBannerMessage: setErrorBannerMessageSelector(state),
//           errorBannerMessage: errorBannerMessageSelector(state),
//         }),
//         []
//       )
//     );

//   const sendMessage = (data: any) => {
//     console.log("message", data);
//     data.request.frames = imageFrames;
//     socket.emit("/process-frame", data);
//     setImageFrames([]);

//     if (data.request.stepCode < 5) {
//       setTimeout(() => {
//         data.request.stepCode = (data.request.stepCode ?? 0) + 1;
//         sendMessage(data);
//       }, 10000);
//     }
//   };

//   useEffect(() => {
//     if (timer && timer > 0) {
//       const intervalId = setTimeout(() => {
//         setTimer(timer - 1);
//       }, 1000);
//       setMessage(t("welcome_message", { count: timer }));
//       return () => clearTimeout(intervalId);
//     }
//   }, [timer]);

//   useEffect(() => {
//     setIsNoBackground(!!colorVerification);
//     if (isError && !colorVerification) {
//       setErrorBannerMessage(
//         `Verification was unsuccessful, restarting in ${timer} seconds`
//       );
//     }
//     if (timer === 0) {
//       setErrorBannerMessage(null);
//     }
//   }, [isError, timer, colorVerification]);

//   const videoConstraints = {
//     aspectRatio:
//       window.screen.availWidth <= 1280
//         ? window.screen.availWidth / window.innerHeight
//         : 1.6,
//   };

//   // socket connection established
//   const onConnect = () => {
//     console.log("connected");
//     const data = {
//       requestTime: "2019-08-24T14:15:22Z",
//       request: {
//         slotId: "123456",
//         stepCode: null,
//         frames: [],
//       },
//     };
//     // as soon as we establish the connection, we will send the process frame request
//     sendMessage(data);
//   };

//   // on disconnect event
//   const onDisconnect = () => {
//     console.log("disconnected");
//   };

//   // on process frame response event
//   const processFrame = (x: any) => {
//     console.log("getting process frame response through foo event");
//     if (x.step.code === null) {
//       setTimer(x.step.startupDelayInSeconds);
//     }
//     console.log("foo event", x);
//   };

//   const gettingResponseFromSocket = (response: any) => {
//     console.log("getting response from socket", response);
//     clearInterval(captureFrameInterval);
//     captureFrameInterval = setInterval(
//       captureFrame,
//       Math.floor(10000 / response.step.framesPerSecond)
//     );

//     switch (response.step.code) {
//       case null:
//         setTimer(response.step.startupDelayInSeconds);
//         break;
//       case 1:
//         setColorVerification(true);
//         setBgColor("blue");
//         setMessage(t("focus_on_screen_message"));
//         break;
//       case 2:
//         setColorVerification(true);
//         setBgColor("#E68500");
//         break;
//       case 3:
//         setColorVerification(true);
//         setBgColor("rgb(0, 255, 0)");
//         break;
//       case 4:
//         setColorVerification(false);
//         setMessage("Initiating ID verification");
//         break;
//       case 5:
//         setAlertConfig({
//           icon: "success",
//           header: "Verification Successful!",
//           subHeader: "Please wait while we finalize the process",
//           footer: null,
//         });
//         break;
//       case 7:
//         break;
//       case 9:
//         setAlertConfig({
//           icon: "fail",
//           header: "Verification Unsuccessful!",
//           subHeader: "Oops! We were unable to complete the eKYC verification.",
//           footer: (
//             <Button id="retry-button" className="my-4 h-16 w-full">
//               Retry
//             </Button>
//           ),
//         });
//         break;
//     }
//   };

//   useEffect(() => {
//     // const data = {
//     //   requestTime: "2019-08-24T14:15:22Z",
//     //   request: {
//     //     slotId: "123456",
//     //     stepCode: 0,
//     //     frames: imageFrames,
//     //   },
//     // };
//     // sendMessage(data);
//     socket = io("ws://localhost:8088?code=abc&state=def", {
//       path: "/v1/signup/identity-verification/ws",
//     });

//     // attaching callback for establishing the socket connection
//     socket.on("connect", onConnect);
//     // attaching callback for disconnect event
//     socket.on("disconnect", onDisconnect);
//     // attaching callback for process frame response event
//     socket.on("/process-frame", processFrame);

//     socket.on("/get-frame", gettingResponseFromSocket);

//     return () => {
//       // removing connect event listeners
//       socket.off("connect", onConnect);
//       // removing disconnect event listeners
//       socket.off("disconnect", onDisconnect);
//       // removing process frame response event listeners
//       socket.off("/process-frame", processFrame);
//     };
//   }, []);

//   return alertConfig !== null ? (
//     <EkycStatusAlert config={alertConfig} />
//   ) : (
//     <div className="sm:pb-[4em]">
//       {!errorBannerMessage && message && (
//         <div className="video-message sm:w-[90vw]">{message}</div>
//       )}
//       <div
//         className={
//           colorVerification
//             ? `no-video-border`
//             : "video-border mx-auto my-3 w-max sm:w-[90vw]"
//         }
//         style={colorVerification && bgColor ? { backgroundColor: bgColor } : {}}
//       >
//         <Webcam
//           audio={false}
//           ref={webcamRef}
//           className={
//             colorVerification
//               ? "hidden"
//               : "h-[500px] w-[800px] rounded-lg lg:h-auto lg:w-[750px] md:h-auto md:w-[90vw] sm:h-auto sm:w-[90vw]"
//           }
//           videoConstraints={videoConstraints}
//           screenshotFormat="image/jpeg"
//         />
//       </div>
//     </div>
//   );
// };

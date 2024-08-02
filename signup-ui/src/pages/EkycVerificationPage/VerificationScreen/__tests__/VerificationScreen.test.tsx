import { QueryCache, QueryClient } from "@tanstack/react-query";
import { screen } from "@testing-library/react";
import { WS } from "jest-websocket-mock";

import { renderWithClient } from "~utils/test";
import { SettingsDto } from "~typings/types";

import { VerificationScreen } from "../VerificationScreen";

describe("Web socket connection between the front end and back end", () => {
  const queryCache = new QueryCache();
  const queryClient = new QueryClient({ queryCache });

  const settings = {
    response: {
      configs: {
        "slot.request.limit": 2,
        "slot.request.delay": 1,
      },
    },
  } as SettingsDto;
  const cancelPopup = jest.fn();

  let ws: WS;

  beforeEach(() => {
    ws = new WS("ws://localhost:8088");
  });

  afterEach(() => WS.clean());

  it("should exchange message", () => {
    // Arrange
    // Act
    // Assert
    // 1. status to be connected (using hidden element) when connected
    // NOTE: Due to the randomness nature of the socket, I will just check the stepCode=START
    // 2. send to /topic/<SLOT_ID> and receive 
    //    `"frameNumber":0,"stepCode":"START","step":{"code":"liveness_check","framesPerSecond":3,"durationInSeconds":100,"startupDelayInSeconds":15,"retryOnTimeout":false,"retryableErrorCodes":[]},"feedback":null`
    // 3. status to be disconnected (using hidden element) when disconnected
  });
});

describe("VerificationScreen (vs)", () => {
  const queryCache = new QueryCache();
  const queryClient = new QueryClient({ queryCache });

  const settings = {
    response: {
      configs: {},
    },
  } as SettingsDto;
  const cancelPopup = jest.fn();

  it("should render correctly", () => {
    // Arrange
    renderWithClient(
      queryClient,
      <VerificationScreen
        settings={settings.response}
        cancelPopup={cancelPopup}
      />
    );

    // Act

    // Assert
    // 1. `vs` is in the document
    // 2. `vs` contains a `webcam`
    const vs = screen.getByTestId("vs");
    expect(vs).toBeInTheDocument();
  });

  it("should show onscreen instructions above the video frame", () => {
    // Arrange

    renderWithClient(
      queryClient,
      <VerificationScreen
        settings={settings.response}
        cancelPopup={cancelPopup}
      />
    );

    // Act

    // Assert
    // 1. `vs-onscreen-instruction` is in the document
    // 2. `vs-onscreen-instruction` should say "Welcome! Initiating Identity verification process in..."
    const vsOnScreenInstruction = screen.getByTestId("vs-onscreen-instruction");
    expect(vsOnScreenInstruction).toBeInTheDocument();
  });

  // Currently not sure since liveness depends on the web socket's response
  it("should show liveliness verification screen", () => {
    // Arrange
    renderWithClient(
      queryClient,
      <VerificationScreen
        settings={settings.response}
        cancelPopup={cancelPopup}
      />
    );

    // Act

    // Assert
    const vsLiveliness = screen.getByTestId("vs-liveliness");
    expect(vsLiveliness).toBeInTheDocument();
  });

  // Currently not sure since liveness depends on the web socket's response
  it("should show solid colors across the full screen for color based frame verification", async () => {
    // Arrange
    renderWithClient(
      queryClient,
      <VerificationScreen
        settings={settings.response}
        cancelPopup={cancelPopup}
      />
    );

    // Act
    // TODO: add wait for x seconds

    // Assert
    const vsSolidColorScreen = screen.getByTestId("vs-solid-color-screen");
    expect(vsSolidColorScreen).toBeInTheDocument();
  });

  // Currently not sure since liveness depends on the web socket's response
  it("should show NID verification screen", () => {
    // Arrange
    renderWithClient(
      queryClient,
      <VerificationScreen
        settings={settings.response}
        cancelPopup={cancelPopup}
      />
    );

    // Act

    // Assert
    const vsNID = screen.getByTestId("vs-nid");
    expect(vsNID).toBeInTheDocument();
  });

  // This one should be moved to IdentityVerificationScreen instead
  // https://xd.adobe.com/view/d1ca3fd3-a54c-4055-b7a2-ee5ad0389788-8499/screen/ba9b246e-7658-4c2b-adb5-b908ecdc3825/specs/
  // https://xd.adobe.com/view/d1ca3fd3-a54c-4055-b7a2-ee5ad0389788-8499/screen/8f43b20a-1a6a-4a49-b751-e1e2ccb2346b/specs/
  it("should show feedback message when verification fails", () => {
    // Arrange
    // TODO: mock failed verification
    renderWithClient(
      queryClient,
      <VerificationScreen
        settings={settings.response}
        cancelPopup={cancelPopup}
      />
    );

    // Act

    // Assert
    const vsFailedVerification = screen.getByTestId("vs-failed-verification");
    expect(vsFailedVerification).toBeInTheDocument();
  });

  // This one should be moved to IdentityVerificationScreen instead
  // https://xd.adobe.com/view/d1ca3fd3-a54c-4055-b7a2-ee5ad0389788-8499/screen/ba9b246e-7658-4c2b-adb5-b908ecdc3825/specs/
  // https://xd.adobe.com/view/d1ca3fd3-a54c-4055-b7a2-ee5ad0389788-8499/screen/8f43b20a-1a6a-4a49-b751-e1e2ccb2346b/specs/
  it("should show warning message if there is any technical issue", () => {
    // Arrange
    // TODO: mock technical issue: internet connection lost, ...
    renderWithClient(
      queryClient,
      <VerificationScreen
        settings={settings.response}
        cancelPopup={cancelPopup}
      />
    );

    // Act

    // Assert
    const vsTechnicalIssueWarningMsg = screen.getByTestId(
      "vs-technical-issue-warning-msg"
    );
    expect(vsTechnicalIssueWarningMsg).toBeInTheDocument();
  });

  // This one should be moved to IdentityVerificationScreen instead
  // https://xd.adobe.com/view/d1ca3fd3-a54c-4055-b7a2-ee5ad0389788-8499/screen/8ee5c56c-1cd5-4b52-adc4-4350f88e8973/specs/
  it("should be redirected to the leading screen when the verification is successful", () => {
    // Arrange
    // TODO: mock successful verification
    renderWithClient(
      queryClient,
      <VerificationScreen
        settings={settings.response}
        cancelPopup={cancelPopup}
      />
    );

    // Act

    // Assert
    // to be redirected to and land on the leading screen
  });
});

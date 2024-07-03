import { QueryCache, QueryClient } from "@tanstack/react-query";
import { screen } from "@testing-library/react";

import { renderWithClient } from "~utils/test";

import { VerificationScreen } from "../VerificationScreen";

describe("Web socket connection between the front end and back end", () => {
  const queryCache = new QueryCache();
  const queryClient = new QueryClient({ queryCache });

  it("should be on", () => {
    // TODO: will add the test implementation once some web socket structure is given

    // Arrange

    // Act
    renderWithClient(queryClient, <VerificationScreen />);

    // Assert
    // the connection should be on
  });
});

describe("VerificationScreen (vs)", () => {
  const queryCache = new QueryCache();
  const queryClient = new QueryClient({ queryCache });

  it("should render correctly", () => {
    // Arrange
    renderWithClient(queryClient, <VerificationScreen />);

    // Act

    // Assert
    const vs = screen.getByTestId("vs");
    expect(vs).toBeInTheDocument();
  });

  it("should show onscreen instructions above the video frame sent from eKYC provider", () => {
    // Arrange
    // TODO: mock instruction of an eKYC provider

    renderWithClient(queryClient, <VerificationScreen />);

    // Act

    // Assert
    const vsOnScreenInstruction = screen.getByTestId("vs-onscreen-instruction");
    expect(vsOnScreenInstruction).toBeInTheDocument();
  });

  it("should show liveliness verification screen", () => {
    // Arrange
    renderWithClient(queryClient, <VerificationScreen />);

    // Act

    // Assert
    const vsLiveliness = screen.getByTestId("vs-liveliness");
    expect(vsLiveliness).toBeInTheDocument();
  });

  it("should show solid colors across the full screen for color based frame verification", async () => {
    // Arrange
    renderWithClient(queryClient, <VerificationScreen />);

    // Act
    // TODO: add wait for x seconds

    // Assert
    const vsSolidColorScreen = screen.getByTestId("vs-solid-color-screen");
    expect(vsSolidColorScreen).toBeInTheDocument();
  });

  it("should show NID verification screen", () => {
    // Arrange
    renderWithClient(queryClient, <VerificationScreen />);

    // Act

    // Assert
    const vsNID = screen.getByTestId("vs-nid");
    expect(vsNID).toBeInTheDocument();
  });

  it("should show feedback message when verification fails", () => {
    // Arrange
    // TODO: mock failed verification
    renderWithClient(queryClient, <VerificationScreen />);

    // Act

    // Assert
    const vsFailedVerification = screen.getByTestId("vs-failed-verification");
    expect(vsFailedVerification).toBeInTheDocument();
  });

  it("should show warning message if there is any technical issue", () => {
    // Arrange
    // TODO: mock technical issue: internet connection lost, ...
    renderWithClient(queryClient, <VerificationScreen />);

    // Act

    // Assert
    const vsTechnicalIssueWarningMsg = screen.getByTestId(
      "vs-technical-issue-warning-msg"
    );
    expect(vsTechnicalIssueWarningMsg).toBeInTheDocument();
  });

  it("should be redirected to the leading screen when the verification is successful", () => {
    // Arrange
    // TODO: mock successful verification
    renderWithClient(queryClient, <VerificationScreen />);

    // Act

    // Assert
    // to be redirected to and land on the leading screen
  });
});

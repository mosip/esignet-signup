import { QueryCache, QueryClient } from "@tanstack/react-query";
import { screen } from "@testing-library/react";

import { renderWithClient } from "~utils/test";

import { VerificationScreen } from "../VerificationScreen";

describe("Web socket connection between the front end and back end", () => {
  const queryCache = new QueryCache();
  const queryClient = new QueryClient({ queryCache });

  it("should be on", () => {
    // will add the test implementation once some web socket structure is given

    // Arrange

    // Act
    renderWithClient(queryClient, <VerificationScreen />);

    // Assert
  });
});

describe("VerificationScreen (vs)", () => {
  const queryCache = new QueryCache();
  const queryClient = new QueryClient({ queryCache });

  it("should render correctly", () => {
    // Arrange

    // Act
    renderWithClient(queryClient, <VerificationScreen />);

    const vs = screen.getByTestId("vs");

    // Assert
    expect(vs).toBeInTheDocument();
  });

  it("should show onscreen instructions above the video frame sent from eKYC provider", () => {
    // Arrange

    // Act
    renderWithClient(queryClient, <VerificationScreen />);

    const vsOnScreenInstruction = screen.getByTestId("vs-onscreen-instruction");

    // Assert
    expect(vsOnScreenInstruction).not.toBeNull();
  });

  it("should show liveliness verification screen", () => {
    // Arrange

    // Act
    renderWithClient(queryClient, <VerificationScreen />);

    const vsLiveliness = screen.getByTestId("vs-liveliness");

    // Assert
    expect(vsLiveliness).not.toBeNull();
  });

  it("should show solid colors across the full screen for color based frame verification", async () => {
    // Arrange

    // Act
    renderWithClient(queryClient, <VerificationScreen />);

    const vsSolidColorScreen = screen.getByTestId("vs-solid-color-screen");

    // Assert
    expect(vsSolidColorScreen).not.toBeNull();
  });

  it("should show NID verification screen", () => {
    // Arrange

    // Act
    renderWithClient(queryClient, <VerificationScreen />);

    const vsNID = screen.getByTestId("vs-nid");

    // Assert
    expect(vsNID).toBeInTheDocument();
  });

  it("should show feedback message when verification fails", () => {
    // Arrange

    // Act
    renderWithClient(queryClient, <VerificationScreen />);

    // Assert
  });

  it("should show warning message if there is any technical issue", () => {
    // Arrange

    // Act
    renderWithClient(queryClient, <VerificationScreen />);

    // Assert
  });

  it("should be redirected to the leading screen when the verification is successful", () => {
    // Arrange

    // Act
    renderWithClient(queryClient, <VerificationScreen />);

    // Assert
  });
});

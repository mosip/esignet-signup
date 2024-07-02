import { QueryCache, QueryClient } from "@tanstack/react-query";

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

describe("VerificationScreen", () => {
  const queryCache = new QueryCache();
  const queryClient = new QueryClient({ queryCache });

  it("should show onscreen instructions above the video frame sent from eKYC provider", () => {
    // Arrange

    // Act
    renderWithClient(queryClient, <VerificationScreen />);

    // Assert
  });

  it("should show solid colors across the full screen for color based frame verification", () => {
    // Arrange

    // Act
    renderWithClient(queryClient, <VerificationScreen />);

    // Assert
  });

  it("should show liveliness verification screen", () => {
    // Arrange

    // Act
    renderWithClient(queryClient, <VerificationScreen />);

    // Assert
  });

  it("should show NID verification screen", () => {
    // Arrange

    // Act
    renderWithClient(queryClient, <VerificationScreen />);

    // Assert
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

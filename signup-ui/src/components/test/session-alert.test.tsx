import { screen } from "@testing-library/react";

import "@testing-library/jest-dom";

import { QueryCache, QueryClient } from "@tanstack/react-query";

import { SessionAlertDialog } from "~components/session-alert";

import { renderWithClient } from "./utils";

describe("SessionAlert", () => {
  const queryCache = new QueryCache();
  const queryClient = new QueryClient({ queryCache });

  test("should not render before promptTimeout", () => {
    renderWithClient(
      queryClient,
      <SessionAlertDialog
        showSessionAlert={false}
        isInSessionTimeoutScope={true}
        isIdle={() => false}
        sessionRemainingTimeout={1_000}
        handleContinueSession={jest.fn()}
        handleReturnToLogin={jest.fn()}
      />
    );
    expect(screen.queryByTestId("session-alert-dialog")).toBeNull();
  });

  test("should render alert message correctly after promptTimeout", async () => {
    renderWithClient(
      queryClient,
      <SessionAlertDialog
        showSessionAlert={true}
        isInSessionTimeoutScope={true}
        isIdle={() => false}
        sessionRemainingTimeout={1_000}
        handleContinueSession={jest.fn()}
        handleReturnToLogin={jest.fn()}
      />
    );

    expect(screen.getByTestId("session-alert-dialog")).toBeInTheDocument();

    expect(screen.getByText("Alert!")).toBeInTheDocument();
    expect(
      screen.getByText("Your session is about to expire due to inactivity")
    ).toBeInTheDocument();
    expect(screen.getByText("Continue Session")).toBeInTheDocument();
  });

  test("should render session timeout message correctly", () => {
    renderWithClient(
      queryClient,
      <SessionAlertDialog
        showSessionAlert={true}
        isInSessionTimeoutScope={true}
        isIdle={() => true}
        sessionRemainingTimeout={1_000}
        handleContinueSession={jest.fn()}
        handleReturnToLogin={jest.fn()}
      />
    );

    expect(screen.getByTestId("session-alert-dialog")).toBeInTheDocument();

    expect(screen.getByText("Session Expired")).toBeInTheDocument();
    expect(
      screen.getByText(
        "Your session has expired due to inactivity. Please click on the below button to return to the login screen."
      )
    ).toBeInTheDocument();
    expect(screen.getByText("Return to Login")).toBeInTheDocument();
  });
});

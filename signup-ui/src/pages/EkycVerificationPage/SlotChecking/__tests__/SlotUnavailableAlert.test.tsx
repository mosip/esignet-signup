import { QueryCache, QueryClient } from "@tanstack/react-query";
import { screen } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";

import { renderWithClient } from "~utils/test";

import { SlotUnavailableAlert } from "../components/SlotUnavailableAlert";

describe("SlotUnavailableAlert", () => {
  const queryCache = new QueryCache();
  const queryClient = new QueryClient({ queryCache });

  test("should render alert when the slot is unavailable correctly", () => {
    renderWithClient(
      queryClient,
      <MemoryRouter>
        <SlotUnavailableAlert />
      </MemoryRouter>
    );
    expect(screen.queryByTestId("slot-unavailable")).not.toBeNull();
    expect(
      screen.getByTestId("slot-unavailable-failed-icon")
    ).toBeInTheDocument();
    expect(screen.getByText("Alert!")).toBeInTheDocument();
    expect(
      screen.getByText(
        "We are experiencing a higher than usual demand, hence we are unable to proceed with the eKYC verification process at this moment. We regret the inconvenience and request you to please try again later. Thank you for your understanding."
      )
    ).toBeInTheDocument();
  });
});

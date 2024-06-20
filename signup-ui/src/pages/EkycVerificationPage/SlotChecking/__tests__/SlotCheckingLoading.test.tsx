import { render, screen } from "@testing-library/react";

import { SlotCheckingLoading } from "../components/SlotCheckingLoading";

describe("SlotCheckingLoading", () => {
  test("should render loading screen correctly", () => {
    render(<SlotCheckingLoading />);
    expect(screen.queryByTestId("slot-checking-content")).not.toBeNull();
    expect(screen.getByTestId("slot-checking-loader")).toBeInTheDocument();
    expect(screen.getByText("Loading!!!")).toBeInTheDocument();
    expect(
      screen.getByText("Kindly wait as we initiate your eKYC")
    ).toBeInTheDocument();
  });
});

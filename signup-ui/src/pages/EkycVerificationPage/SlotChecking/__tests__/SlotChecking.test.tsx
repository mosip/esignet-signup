import { QueryCache, QueryClient } from "@tanstack/react-query";
import { act, screen, waitFor } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";

import { renderWithClient, sleep } from "~utils/test";
import { useEkycVerificationStore } from "~pages/EkycVerificationPage/useEkycVerificationStore";
import { KycProvider } from "~typings/types";
import { checkSlotHandlerUnavailable } from "~/mocks/handlers/slot-checking";
import { mswServer } from "~/mocks/msw-server";

// import * as mutationHooks from "../../../shared/mutations";
import { SlotUnavailableAlert } from "../components/SlotUnavailableAlert";
import { SlotChecking } from "../SlotChecking";

afterEach(() => {
  jest.clearAllMocks();
});

describe("SlotChecking", () => {
  const queryCache = new QueryCache();
  const queryClient = new QueryClient({ queryCache });

  const initialEkycStore = useEkycVerificationStore.getState();
  beforeEach(() => {
    useEkycVerificationStore.setState({
      ...initialEkycStore,
      kycProvider: {
        id: "Kyc_Provider_1",
        logoUrl: "https://avatars.githubusercontent.com/u/39733477?s=200&v=4",
        displayName: {
          eng: "Veridonia",
          khm: "មនុស្សឆ្លាតវៃ",
          "@none": "Default Kyc Provider 1",
        },
        active: true,
        processType: "VIDEO",
        retryOnFailure: true,
        resumeOnSuccess: true,
        description: "Kyc provider 1",
      } as KycProvider,
    });
  });

  test("should show loading when the slot availability is being checked", async () => {
    // Arrange
    // Act
    await act(async () =>
      renderWithClient(
        queryClient,
        <MemoryRouter>
          <SlotChecking />
        </MemoryRouter>
      )
    );

    await sleep(3000);

    // Assert
    await expect(
      await waitFor(() => screen.queryByTestId("slot-checking-content"))
    ).not.toBeNull();
  });

  test("should show alert when the slot is unavailable", async () => {
    // Arrange
    // use slot unavailable response
    mswServer.use(checkSlotHandlerUnavailable);

    // Act
    await act(async () =>
      renderWithClient(
        queryClient,
        <MemoryRouter>
          <SlotUnavailableAlert />
        </MemoryRouter>
      )
    );

    await sleep(3000);

    // Assert
    await expect(
      await waitFor(() => screen.queryByTestId("slot-unavailable"))
    ).not.toBeNull();
  });
});

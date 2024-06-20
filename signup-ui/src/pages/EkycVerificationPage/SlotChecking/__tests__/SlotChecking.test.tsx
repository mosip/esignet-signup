import { QueryCache, QueryClient, useMutation } from "@tanstack/react-query";
import { screen } from "@testing-library/react";

import { renderWithClient } from "~utils/test";
import { useEkycVerificationStore } from "~pages/EkycVerificationPage/useEkycVerificationStore";
import { KycProvider } from "~typings/types";

import * as mutationHooks from "../../../shared/mutations";
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

  test("should show loading when the slot availability is being checked", () => {
    jest.spyOn(mutationHooks, "useSlotAvailability").mockReturnValue({
      slotAvailabilityMutation: {
        isPending: true,
        mutate: jest.fn(),
      },
    } as any);

    renderWithClient(queryClient, <SlotChecking />);
    expect(screen.queryByTestId("slot-checking-content")).not.toBeNull();
  });

  test("should show alert when the slot is unavailable", () => {
    jest.spyOn(mutationHooks, "useSlotAvailability").mockReturnValue({
      slotAvailabilityMutation: {
        isPending: false,
        mutate: jest.fn(),
      },
    } as any);

    renderWithClient(queryClient, <SlotUnavailableAlert />);
    expect(screen.queryByTestId("slot-unavailable")).not.toBeNull();
  });
});

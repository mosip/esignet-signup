import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { useTranslation } from 'react-i18next';
import { EkycVerificationStep, useEkycVerificationStore } from '~pages/EkycVerificationPage/useEkycVerificationStore';
import { VerificationScreen } from '../VerificationScreen';
import { SettingsDto } from "~typings/types";

// Mocking dependencies
jest.mock('react-i18next', () => ({
  useTranslation: jest.fn(() => ({ t: (key: string) => key })),
}));

jest.mock('~services/langConfig.service', () => ({
  getLangCodeMapping: jest.fn().mockResolvedValue({}),
}));

jest.mock('~pages/shared/stompWs', () => ({
  useStompClient: () => ({
    client: { activate: jest.fn(), deactivate: jest.fn() },
    connected: true,
    publish: jest.fn(),
    subscribe: jest.fn(),
    unsubscribe: jest.fn(),
  }),
}));

jest.mock('../useEkycVerificationStore', () => ({
  useEkycVerificationStore: jest.fn(() => ({
    setIsNoBackground: jest.fn(),
    setErrorBannerMessage: jest.fn(),
    errorBannerMessage: null,
    slotId: 'test-slot-id',
    setStep: jest.fn(),
    setSlotId: jest.fn(),
  })),
}));

describe('VerificationScreen', () => {
  const defaultProps = {
    cancelPopup: jest.fn(),
  };

  const settings = {
    response: {
      configs: {
        "signin.redirect-url":
          "https://esignet.camdgc-dev1.mosip.net/authorize",
      },
    },
  } as SettingsDto;

  it('renders loading indicator when not connected', () => {
    // Override the mock to simulate not connected state
    jest.mock('~pages/shared/stompWs', () => ({
      useStompClient: () => ({
        client: { activate: jest.fn(), deactivate: jest.fn() },
        connected: false,
        publish: jest.fn(),
        subscribe: jest.fn(),
        unsubscribe: jest.fn(),
      }),
    }));
    
    render(<VerificationScreen settings={settings.response} cancelPopup={defaultProps.cancelPopup}/>);

    expect(screen.getByText('please_wait')).toBeInTheDocument();
  });

  it('displays webcam and handles errors', async () => {
    // Render the component
    render(<VerificationScreen settings={settings.response} cancelPopup={defaultProps.cancelPopup}/>);

    // Check if webcam is rendered
    expect(screen.getByRole('button', { hidden: true })).toBeInTheDocument();

    // Simulate error scenario
    await waitFor(() => {
      // Set the error in the mock store
      const mockStore = useEkycVerificationStore();
      mockStore.setErrorBannerMessage('Test Error Message');
      
      // Check if error message is displayed
      expect(screen.getByText('Test Error Message')).toBeInTheDocument();
    });
  });

  // Add more test cases to cover different scenarios
});

import { useState } from "react";

import { SignUpContext } from "./SignUpContext";

interface SignUpProviderProps {
  children: React.ReactNode;
}

export const SignUpProvider = ({ children }: SignUpProviderProps) => {
  const [activeStep, setActiveStep] = useState(0);

  return (
    <SignUpContext.Provider
      value={{
        activeStep,
        setActiveStep,
      }}
    >
      {children}
    </SignUpContext.Provider>
  );
};

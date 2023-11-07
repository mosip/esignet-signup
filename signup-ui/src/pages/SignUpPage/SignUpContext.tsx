import { createContext, Dispatch, SetStateAction, useContext } from "react";

export interface SignUpContextProps {
  activeStep: number;
  setActiveStep: Dispatch<SetStateAction<number>>;
}

export const SignUpContext = createContext<SignUpContextProps | undefined>(
  undefined
);

export const useSignUpContext = (): SignUpContextProps => {
  const context = useContext(SignUpContext);
  if (!context) {
    throw new Error(
      `useSignUpContext must be used within a SignUpProvider component`
    );
  }
  return context;
};

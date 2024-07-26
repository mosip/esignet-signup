import { useCallback } from "react";

import { DefaultEkyVerificationProp } from "~typings/types";

import {
  isLivenessCheckSuccessSelector,
  useEkycVerificationStore,
} from "../useEkycVerificationStore";
import { IdentityVerificationStatusFailed } from "./IdentityVerificationStatusFailed";
import { IdentityVerificationStatusSuccess } from "./IdentityVerificationStatusSuccess";

export const IdentityVerificationStatus = ({
  settings,
  cancelPopup,
}: DefaultEkyVerificationProp) => {
  const { isLivenessCheckSuccess } = useEkycVerificationStore(
    useCallback(
      (state) => ({
        isLivenessCheckSuccess: isLivenessCheckSuccessSelector(state),
      }),
      []
    )
  );

  if (!isLivenessCheckSuccess) {
    return (
      <IdentityVerificationStatusFailed
        settings={settings}
        cancelPopup={cancelPopup}
      />
    );
  }

  return (
    <IdentityVerificationStatusSuccess
      settings={settings}
      cancelPopup={cancelPopup}
    />
  );
};

import { useEffect, useState } from "react";

export const useTimer = (
  initialTimer: number,
  onTimeOut: (enableResendOtp: boolean) => void
) => {
  // initialize timeLeft with the seconds prop
  const [timeLeft, setTimeLeft] = useState(initialTimer);

  useEffect(() => {
    // exit early when we reach 0
    if (!timeLeft) {
      onTimeOut(true);
      return;
    }

    // save intervalId to clear the interval when the
    // component re-renders
    const intervalId = setInterval(() => {
      setTimeLeft(timeLeft - 1);
    }, 1000);

    // clear interval on re-render to avoid memory leaks
    return () => clearInterval(intervalId);
    // add timeLeft as a dependency to re-rerun the effect
    // when we update it
  }, [timeLeft, onTimeOut]);

  return [timeLeft, setTimeLeft] as const;
};

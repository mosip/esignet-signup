import { useTranslation } from "react-i18next";

interface ResendAttemptProps {
  currentAttempts: number;
  totalAttempts: number;
  attemptRetryAfter?: number;
  showRetry?: boolean;
}

export const ResendAttempt = ({
  currentAttempts,
  totalAttempts,
  attemptRetryAfter = 300,
  showRetry = false,
}: ResendAttemptProps) => {
  const { t } = useTranslation();
  return (
    <>
      {currentAttempts < totalAttempts && (
        <div className="w-max rounded-md bg-[#FFF7E5] p-2 px-8 text-center text-sm font-semibold text-[#8B6105]">
          {t(showRetry ? "attempts_left_and_retry" : "attempts_left", {
            attemptLeft: currentAttempts,
            totalAttempt: totalAttempts,
            attemptRetryAfter: attemptRetryAfter / 60,
          })}
        </div>
      )}
    </>
  );
};

import { useTranslation } from "react-i18next";

interface ResendAttemptProps {
  currentAttempts: number;
  totalAttempts: number;
  attemptRetryAfter?: number;
}

export const ResendAttempt = ({
  currentAttempts,
  totalAttempts,
  attemptRetryAfter = 5,
}: ResendAttemptProps) => {
  const { t } = useTranslation();
  return (
    <>
      {currentAttempts < totalAttempts && (
        <div className="w-max rounded-md bg-[#FFF7E5] p-2 px-8 text-center text-sm font-semibold text-[#8B6105]">
          {t("attempts_left", {
            attemptLeft: currentAttempts,
            totalAttempt: totalAttempts,
            attemptRetryAfter: attemptRetryAfter,
          })}
        </div>
      )}
    </>
  );
};

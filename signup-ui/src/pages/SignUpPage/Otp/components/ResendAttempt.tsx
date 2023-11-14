import { useTranslation } from "react-i18next";

interface ResendAttemptProps {
  currentAttempts: number;
  totalAttempts: number;
}

export const ResendAttempt = ({
  currentAttempts,
  totalAttempts,
}: ResendAttemptProps) => {
  const { t } = useTranslation();
  return (
    <>
      {currentAttempts < totalAttempts && (
        <div className="w-full bg-[#FFF7E5] rounded-md p-2 text-[#8B6105] text-center text-sm font-semibold">
          {t("attempts_left", {
            attemptLeft: currentAttempts,
            totalAttempt: totalAttempts,
          })}
        </div>
      )}
    </>
  );
};

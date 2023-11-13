interface ResendAttemptProps {
  currentAttempts: number;
  totalAttempts: number;
}

export const ResendAttempt = ({
  currentAttempts,
  totalAttempts,
}: ResendAttemptProps) => {
  return (
    <>
      {currentAttempts < totalAttempts && (
        <div className="w-full bg-[#FFF7E5] rounded-md p-2 text-[#8B6105] text-center text-sm font-semibold">
          {currentAttempts} of {totalAttempts} attempts left
        </div>
      )}
    </>
  );
};

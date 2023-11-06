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
        <div className="w-full bg-amber-100/60 rounded-md p-2 text-yellow-800 text-center">
          {currentAttempts} of {totalAttempts} attempts left
        </div>
      )}
    </>
  );
};

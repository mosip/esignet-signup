/**
 *
 * @param secondsLeft the remaining seconds
 * @returns string of minute and seconds remaining in the format of 00:00
 */
export const convertTime = (secondsLeft: number): string => {
  let hours = Math.floor(secondsLeft / 3600);
  let minutes: string | number = Math.floor(secondsLeft / 60) - hours * 60;
  let seconds: string | number = Math.floor(secondsLeft % 60);

  if (minutes < 10) minutes = `0${minutes}`;
  if (seconds < 10) seconds = `0${seconds}`;

  return `${minutes}:${seconds}`;
};

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

/**
 *
 * @param timeoutInSeconds the number of seconds that will be timeout
 * @returns Date object of the timeout
 */
export const getTimeoutTime = (timeoutInSeconds: number): Date => {
  const time = new Date();
  time.setSeconds(time.getSeconds() + timeoutInSeconds); // timeout seconds later

  return time;
};

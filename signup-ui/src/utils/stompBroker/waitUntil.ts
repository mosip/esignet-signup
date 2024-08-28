const MAX_MILLIS = 2000;

const waitUntil = (
  predicate: () => boolean,
  errorMessage: string = `Predicate did not become true in ${MAX_MILLIS}ms`
): Promise<void> => {
  let timedOut = false;
  const timeout = setTimeout(() => (timedOut = true), MAX_MILLIS);
  const recursivelyResolve = (
    resolve: () => void,
    reject: (message: string) => void
  ) => {
    if (timedOut) {
      reject(errorMessage);
    }
    if (predicate()) {
      clearTimeout(timeout);
      resolve();
    } else {
      setTimeout(() => recursivelyResolve(resolve, reject), 10);
    }
  };

  return new Promise((resolve, reject) => {
    recursivelyResolve(resolve, reject);
  });
};

export default waitUntil;

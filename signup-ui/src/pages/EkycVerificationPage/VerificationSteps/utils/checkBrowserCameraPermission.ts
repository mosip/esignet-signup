/**
 * Checks if the browser has permission to access the camera.
 *
 * This function attempts to access the user's camera using the MediaDevices.getUserMedia() method.
 * If successful, it then enumerates all media devices and filters out the video input devices.
 * If there is at least one video input device with a device ID, the function returns true.
 * If the function fails to access the camera or there are no video input devices with a device ID, it returns false.
 *
 * @async
 * @function
 * @returns {Promise<boolean>} - A promise that resolves to true if the browser has camera access and there is at least one video input device with a device ID, and false otherwise.
 * @throws Will throw an error if the browser does not support the MediaDevices API.
 */
export const checkBrowserCameraPermission = async () => {
  try {
    await navigator.mediaDevices.getUserMedia({ video: true });
    let devices = await navigator.mediaDevices.enumerateDevices();
    const videoDevices = devices.filter(
      (device) => device.kind === "videoinput"
    );

    if (
      videoDevices.length > 0 &&
      videoDevices.some((device) => device.deviceId)
    ) {
      return true;
    } else {
      return false;
    }
  } catch (error) {
    return false;
  }
};

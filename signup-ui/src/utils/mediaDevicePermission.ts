export const requestCameraPermission = async () => {
  try {
    await navigator.mediaDevices.getUserMedia({ video: true });
    console.log("Camera permission granted");
  } catch (error) {
    console.error("Error requesting camera permission:", error);
  }
};

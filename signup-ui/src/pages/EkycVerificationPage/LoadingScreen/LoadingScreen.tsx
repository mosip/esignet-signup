import { UnsupportedBrowserPerm } from "./components/UnsupportedBrowserPerm";

export const LoadingScreen = ({
  handleDismiss,
}: {
  handleDismiss: (args: { key: string; error: string }) => void;
}) => {
  return <UnsupportedBrowserPerm handleDismiss={handleDismiss} />;
};

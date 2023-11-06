import { Button } from "~components/ui/button";
import { Icons } from "~components/ui/icons";

export const Status = () => {
  return (
    <div className="container max-w-lg border-[1px] rounded-2xl bg-white p-0">
      {/* Status */}
      <div className="p-6 flex flex-col gap-y-6">
        <div className="flex flex-col gap-y-2 items-center">
          <Icons.check className="w-20 h-20 text-green-500" />
          <h3 className="font-bold text-xl">Successful!</h3>
          <div className="text-center text-gray-500">
            <div>Your mobile number has been verified successfully.</div>
            <div>
              Please continue to setup your account and complete the
              registration process.
            </div>
          </div>
        </div>
        <Button className="w-full p-4 font-semibold py-6" variant="secondary">
          Continue
        </Button>
      </div>
    </div>
  );
};

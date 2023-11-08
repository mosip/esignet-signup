import { useCallback } from "react";
import { PopoverTrigger } from "@radix-ui/react-popover";
import { UseFormReturn } from "react-hook-form";

import { Button } from "~components/ui/button";
import {
  Card,
  CardContent,
  CardDescription,
  CardDivider,
  CardHeader,
  CardTitle,
} from "~components/ui/card";
import { Checkbox } from "~components/ui/checkbox";
import {
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from "~components/ui/form";
import { Icons } from "~components/ui/icons";
import { Input } from "~components/ui/input";
import { Popover, PopoverContent } from "~components/ui/popover";

import { useSignUpContext } from "../SignUpContext";
import { SignUpForm } from "../SignUpPage";

interface AccountSetupProps {
  methods: UseFormReturn<SignUpForm, any, undefined>;
}

export const AccountSetup = ({ methods }: AccountSetupProps) => {
  const { setActiveStep } = useSignUpContext();
  const { control, trigger } = methods;

  const handleContinue = useCallback(
    async (e: any) => {
      e.preventDefault();
      const isStepValid = await trigger();

      if (isStepValid) {
        setActiveStep((prevActiveStep) => prevActiveStep + 1);
      }
    },
    [trigger, setActiveStep]
  );

  return (
    <div className="h-screen flex items-center justify-center">
      <Card className="px-0 container max-w-md rounded-[20px] shadow-[0_3px_10px_rgb(0,0,0,0.2)]">
        <CardHeader className="flex items-center">
          <CardTitle className="font-medium text-3xl">Setup Account</CardTitle>
          <CardDescription className="text-center text-gray-500">
            Please enter the requested details to complete your registration.
          </CardDescription>
        </CardHeader>
        <CardDivider className="w-full border-[1px]" />
        <CardContent className="m-6">
          <div className="flex flex-col gap-y-4">
            <FormField
              control={control}
              name="username"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Username</FormLabel>
                  <FormControl>
                    <Input placeholder="Enter username" {...field} />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
            <FormField
              control={control}
              name="fullNameInKhmer"
              render={({ field }) => (
                <FormItem>
                  <div className="flex items-center gap-1">
                    <FormLabel>Full Name in Khmer</FormLabel>
                    <Popover>
                      <PopoverTrigger asChild>
                        <Icons.info className="w-4 h-4 cursor-pointer" />
                      </PopoverTrigger>
                      <PopoverContent side="right">
                        Maximum 30 characters allowed and it should not contain
                        any digit or special characters except "" space.
                      </PopoverContent>
                    </Popover>
                  </div>
                  <FormControl>
                    <Input placeholder="Enter Full Name in Khmer" {...field} />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
            <FormField
              control={control}
              name="password"
              render={({ field }) => (
                <FormItem>
                  <div className="flex items-center gap-1">
                    <FormLabel>Password</FormLabel>
                    <Popover>
                      <PopoverTrigger asChild>
                        <Icons.info className="w-4 h-4 cursor-pointer" />
                      </PopoverTrigger>
                      <PopoverContent side="right" className="w-80">
                        <div className="flex items-center justify-center">
                          <ul className="list-disc">
                            <li>Require at least 8 characters long</li>
                            <li>Require at least 1 digit</li>
                            <li>Require at least 1 special character</li>
                            <li>Require at least 1 capital letter</li>
                          </ul>
                        </div>
                      </PopoverContent>
                    </Popover>
                  </div>
                  <FormControl>
                    <Input
                      type="password"
                      placeholder="Enter Password"
                      {...field}
                    />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
            <FormField
              control={control}
              name="confirmPassword"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Confirm Password</FormLabel>
                  <FormControl>
                    <Input
                      type="password"
                      placeholder="Enter Password"
                      {...field}
                    />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
            <FormField
              control={control}
              name="termAndPolicy"
              render={({ field }) => (
                <FormItem className="flex space-y-0 items-start gap-x-4">
                  <FormControl className="">
                    <Checkbox
                      checked={field.value}
                      onCheckedChange={field.onChange}
                      className="h-5 w-5 rounded-[2px] text-white data-[state=checked]:bg-orange-500 data-[state=checked]:border-orange-500"
                    />
                  </FormControl>
                  <FormLabel>
                    I agree to Cambodia's{" "}
                    <a className="text-orange-500 underline" target="_blank">
                      Terms & Conditions
                    </a>{" "}
                    and{" "}
                    <a className="text-orange-400 underline" target="_blank">
                      Privacy Policy
                    </a>
                    , to store & process my information as required.
                  </FormLabel>
                  <FormMessage />
                </FormItem>
              )}
            />
            <Button
              type="submit"
              variant="secondary"
              className="w-full"
              onClick={handleContinue}
            >
              Continue
            </Button>
          </div>
        </CardContent>
      </Card>
    </div>
  );
};

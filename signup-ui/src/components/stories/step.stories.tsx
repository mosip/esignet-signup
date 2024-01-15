import { Meta, StoryObj } from "@storybook/react";

import { ReactComponent as SuccessIconSvg } from "~assets/svg/success-icon.svg";
import { Button } from "~components/ui/button";
import { Icons } from "~components/ui/icons";
import {
  Step,
  StepContent,
  StepDescription,
  StepDivider,
  StepFooter,
  StepHeader,
  StepTitle,
} from "~components/ui/step";

const meta: Meta<typeof Step> = {
  title: "ui/Step",
  component: Step,
  tags: ["autodocs"],
  argTypes: {},
};
export default meta;

type Story = StoryObj<typeof Step>;

const notifications = [
  {
    title: "Your call has been confirmed.",
    description: "1 hour ago",
  },
  {
    title: "You have a new message!",
    description: "1 hour ago",
  },
  {
    title: "Your subscription is expiring soon!",
    description: "2 hours ago",
  },
];

export const Base: Story = {
  render: (args) => (
    <Step>
      <StepHeader>
        <StepTitle>
          <SuccessIconSvg />
        </StepTitle>
        <StepDescription>Description of the step...</StepDescription>
      </StepHeader>
      <StepDivider />
      <StepContent>
        <div>
          {notifications.map((notification, index) => (
            <div
              key={index}
              className="mb-4 grid grid-cols-[25px_1fr] items-start pb-4 last:mb-0 last:pb-0"
            >
              <span className="flex h-2 w-2 translate-y-1 rounded-full bg-sky-500" />
              <div className="space-y-1">
                <p className="text-sm font-medium leading-none">
                  {notification.title}
                </p>
                <p className="text-sm text-muted-foreground">
                  {notification.description}
                </p>
              </div>
            </div>
          ))}
        </div>
      </StepContent>
      <StepFooter className="flex justify-between">
        <Button variant="link" size="sm">
          Cancel
        </Button>
        <Button size="sm">Deploy</Button>
      </StepFooter>
    </Step>
  ),
  args: {},
};

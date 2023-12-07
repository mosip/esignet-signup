import { Meta, StoryObj } from "@storybook/react";
import { Mail } from "lucide-react";

import { Button } from "~components/ui/button";

const meta: Meta<typeof Button> = {
  title: "ui/Button",
  component: Button,
  tags: ["autodocs"],
  argTypes: { onClick: { action: "clicked" } },
};
export default meta;

type Story = StoryObj<typeof Button>;

export const Base: Story = {
  render: (args) => <Button {...args}>Button</Button>,
  args: {
    variant: "default",
    size: "default",
    disabled: false,
  },
};
export const Secondary: Story = {
  render: (args) => <Button {...args}>Button</Button>,
  args: {
    variant: "secondary",
  },
};
export const Destructive: Story = {
  render: (args) => <Button {...args}>Button</Button>,
  args: {
    variant: "destructive",
  },
};
export const Link: Story = {
  render: (args) => <Button {...args}>Button</Button>,
  args: {
    variant: "link",
  },
};
export const WithIcon: Story = {
  render: (args) => (
    <Button {...args}>
      <Mail className="mr-2 h-4 w-4" /> Login with Email Button
    </Button>
  ),
  args: {
    variant: "secondary",
  },
};

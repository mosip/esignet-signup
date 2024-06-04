import { cleanup, render, renderHook, screen } from "@testing-library/react";
import { IIdleTimerProps, useIdleTimer } from "react-idle-timer";

import "@testing-library/jest-dom";

import { SessionAlert } from "~components/session-alert";

let props: IIdleTimerProps;

beforeEach(() => {
  props = {
    timeout: undefined,
    promptTimeout: undefined,
    element: undefined,
    events: undefined,
    timers: undefined,
    immediateEvents: undefined,
    onPresenceChange: undefined,
    onPrompt: undefined,
    onIdle: undefined,
    onActive: undefined,
    onAction: undefined,
    onMessage: undefined,
    debounce: undefined,
    throttle: undefined,
    eventsThrottle: undefined,
    startOnMount: undefined,
    startManually: undefined,
    stopOnIdle: undefined,
    crossTab: undefined,
    name: undefined,
    syncTimers: undefined,
    leaderElection: undefined,
    disabled: undefined,
  };
});

const idleTimer = () => {
  return renderHook(() => useIdleTimer(props));
};

describe("SessionAlert", () => {
  test("should not render before promptTimeout", () => {
    // render(<SessionAlert />);
    // const sessionAlert = screen.getByTestId("session-alert-dialog");
    // expect(sessionAlert).not.toBeInTheDocument();

    // This is your dummy data
    const dummyData = "Hello, World!";

    // Render the dummy data in a paragraph element
    const { getByText } = render(<p>{dummyData}</p>);

    // Use the getByText function to get the element
    const element = getByText(dummyData);

    // Use toBeInTheDocument to check if the element is in the document
    expect(element).toBeInTheDocument();
  });

  test("should render alert message correctly after promptTimeout", () => {
    // should have header h2 "សេចក្តីជូនដំណឹង!"
    // should have description "Session របស់អ្នកហៀបនឹងផុតកំណត់ដោយសារអសកម្ម"
    // should have the text "Session ផុតកំណត់ក្នុងរយៈពេល 00:07 នាទី"
    // should have button of "បន្ត session"
  });

  test("should render session timeout message correctly", () => {
    // should have header h2 "Session ​បាន​ផុតកំណត់"
    // should have description "Session របស់អ្នកបានផុតកំណត់ដោយសារអសកម្ម។ សូមចុចលើប៊ូតុងខាងក្រោមដើម្បីត្រឡប់ទៅអេក្រង់ចូល"
    // should have button of "ត្រឡប់ទៅ Login"
  });

  test("popup message should not popup before time set prompt", () => {});
});

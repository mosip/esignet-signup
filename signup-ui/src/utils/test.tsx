import React from "react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { render } from "@testing-library/react";

/**
 * Sleeps for x amount of milliseconds.
 *
 * @param time   Amount of time in milliseconds.
 * @returns Promise resolving timeout id.
 * @private
 */
export function sleep(time: number = 0): Promise<number> {
  return new Promise((resolve) => setTimeout(resolve, time));
}

/**
 * Renders a React element with a provided QueryClient.
 *
 * This function wraps the provided React element with a QueryClientProvider and renders it.
 * It also provides a rerender function that can be used to rerender the UI with the same QueryClient.
 *
 * @param {QueryClient} client - The QueryClient to provide to the React element.
 * @param {React.ReactElement} ui - The React element to render.
 * @returns {object} An object containing various properties from the render result, along with a rerender function.
 * The rerender function takes a new React element and rerenders it with the same QueryClient.
 */
export const renderWithClient = (
  client: QueryClient,
  ui: React.ReactElement
) => {
  const { rerender, ...result } = render(
    <QueryClientProvider client={client}>{ui}</QueryClientProvider>
  );

  return {
    ...result,
    rerender: (rerenderUi: React.ReactElement) =>
      rerender(
        <QueryClientProvider client={client}>{rerenderUi}</QueryClientProvider>
      ),
  };
};

import { forwardRef, ReactNode } from "react";

const Footer = (() => {
  return (
    <footer className="bg-white dark:bg-gray-800 shadow sticky bottom-0">
      <div className="p-4">
        <span className="text-gray-500 dark:text-gray-400 flex items-center justify-center">Powered by eSignet.</span>
      </div>
    </footer>
  )
});

export default forwardRef(Footer);
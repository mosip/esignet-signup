import { forwardRef } from "react";

import { Language } from "~components/language";

const NavBar = () => {
  return (
    <nav
      className="w-full bg-white border-gray-500 shadow px-2 sm:px-4 py-2 sticky top-0"
      id="navbar"
    >
      <div className="container h-full flex justify-between items-center">
        <div className="ltr:sm:ml-8 rtl:sm:mr-8 ltr:ml-1 rtl:mr-1">
          <span className="font-bold text-secondary tracking-normal text-2xl">
            LOGO
          </span>
        </div>
        <div className="flex rtl:sm:ml-8 ltr:sm:mr-8 rtl:ml-1 ltr:mr-1">
          <div className="mx-2 rtl:scale-x-[-1]">
            <Language />
          </div>
        </div>
      </div>
    </nav>
  );
};

export default forwardRef(NavBar);

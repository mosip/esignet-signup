import { forwardRef } from "react";

import { Language } from "~components/language";

const NavBar = () => {
  return (
    <nav className="sticky top-0 z-40 h-[70px] w-full border-gray-500 bg-white px-2 py-2 shadow-md sm:px-0">
      <div className="container flex h-full items-center justify-between">
        <div className="ltr:ml-1 ltr:sm:ml-8 rtl:mr-1 rtl:sm:mr-8">
          <img className="brand-logo" alt="brand_logo" />
        </div>
        <div className="flex ltr:mr-1 ltr:sm:mr-8 rtl:ml-1 rtl:sm:ml-8">
          <div className="mx-2 rtl:scale-x-[-1]">
            <Language />
          </div>
        </div>
      </div>
    </nav>
  );
};

export default forwardRef(NavBar);

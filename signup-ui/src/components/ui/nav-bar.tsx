import { forwardRef, ReactNode } from "react";

const NavBar = (() => {
  return (
    <nav className="bg-white border-gray-500 shadow px-2 sm:px-4 py-2 sticky top-0" id="navbar">
      <div className="flex justify-between">
        <div className="ltr:sm:ml-8 rtl:sm:mr-8 ltr:ml-1 rtl:mr-1">
          
        </div>
        <div className="flex rtl:sm:ml-8 ltr:sm:mr-8 rtl:ml-1 ltr:mr-1">
          <div className="mx-2 rtl:scale-x-[-1]">
            
          </div>

        </div>
      </div>
    </nav>
  )
});

export default forwardRef(NavBar);
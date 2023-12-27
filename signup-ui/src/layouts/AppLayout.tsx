import { Outlet } from "react-router-dom";

import Footer from "~components/ui/footer";
import NavBar from "~components/ui/nav-bar";

export const AppLayout = () => {
  return (
    <div className="flex min-h-screen flex-col">
      <NavBar />
      <div className="relative flex flex-grow flex-col">
        <Outlet />
        <Footer />
      </div>
    </div>
  );
};

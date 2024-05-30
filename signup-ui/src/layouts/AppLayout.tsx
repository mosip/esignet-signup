import { useState } from "react";
import { Outlet } from "react-router-dom";

// import { useSessionTimerContext } from "~contexts/SessionTimerContext";
import Footer from "~components/ui/footer";
import NavBar from "~components/ui/nav-bar";
import { SessionAlert } from "~components/session-alert";

export const AppLayout = () => {
  // const {
  //   showWarning,
  //   showSessionTimeout,
  //   sessionDurationLeft,
  //   isWarningTimerRunning,
  // } = useSessionTimerContext();


  return (
    <div className="flex min-h-screen flex-col">
      <NavBar />
      <div className="relative flex flex-grow flex-col sm:bg-white">
        {/* {(showWarning || showSessionTimeout) && <SessionAlert /> } */}
        {<SessionAlert />}
        <Outlet />
        <Footer />
      </div>
    </div>
  );
};

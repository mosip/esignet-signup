import { useEffect, useState } from "react";
import { Client } from "@stomp/stompjs";

const useStompClient = (url: string, options = {}) => {
  const [client, setClient] = useState<any>(null);
  const [connected, setConnected] = useState<boolean>(false);

  useEffect(() => {
    const stompClient = new Client({
      brokerURL: url,
    });

    stompClient.onConnect = () => {
      setConnected(true);
    };
    stompClient.onDisconnect = () => setConnected(false);

    setClient(stompClient);

    return () => {
      stompClient.deactivate();
    };
  }, [url]);

  const subscribe = (destination: string, callback: any) => {
    if (connected) {
      client?.subscribe(destination, callback);
    } else {
      console.warn("Client not connected yet!");
    }
  };

  const publish = (destination: string, body: any) => {
    if (connected) {
      client?.publish({ destination, body });
    } else {
      console.warn("Client not connected yet!");
    }
  };

  return { client, connected, subscribe, publish };
};

export default useStompClient;

import { useEffect, useState } from "react";
import { Client } from "@stomp/stompjs";

const useStompClient = (url: string, options = {}) => {
  const [client, setClient] = useState<any>(null);
  const [connected, setConnected] = useState<boolean>(false);
  const [subscription, setSubscription] = useState<any>(null);

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

  const subscribe = (destination: string, callback: any): any => {
    if (connected) {
      setSubscription(client?.subscribe(destination, callback));
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

  const unsubscribe = () => {
    if (subscription) {
      subscription.unsubscribe();
    } else {
      console.warn("No subscription to unsubscribe from!");
    }
  }

  return { client, connected, subscribe, publish, unsubscribe };
};

export default useStompClient;

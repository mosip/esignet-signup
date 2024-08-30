import { Message, Client } from "@stomp/stompjs";

interface ClientArgs {
  port: number;
  topic?: string;
  onMessage?: (message: Message) => void;
  endpoint?: string;
}

const getStompClient = ({
  port,
  topic,
  onMessage = jest.fn(),
  endpoint = "/websocket"
}: ClientArgs): Client => {
  const client = new Client({
    brokerURL: `ws://localhost:${port}${endpoint}`
  });

  if (topic) {
    client.onConnect = () => {
      client.subscribe(topic, onMessage);
    };
  }

  client.activate();

  return client;
};

export default getStompClient;

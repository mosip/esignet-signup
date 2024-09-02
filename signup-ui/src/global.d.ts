/// <reference types="jest-extended" />

declare module "mock-stomp-broker" {
  interface Config {
    port?: number;
    portRange?: [number, number];
    endpoint?: string;
  }

  class MockStompBroker {
    private static PORTS_IN_USE;
    private static BASE_SESSION;
    private static getRandomInt;
    private static getPort;
    private readonly port;
    private readonly httpServer;
    private readonly stompServer;
    private readonly sentMessageIds;
    private queriedSessionIds;
    private sessions;
    private thereAreNewSessions;
    private setMiddleware;
    private registerMiddlewares;

    constructor({ port, portRange, endpoint }?: Config);

    public newSessionsConnected(): Promise<string[]>;
    public subscribed(sessionId: string): Promise<void>;
    public scheduleMessage(topic: string, payload: any, headers?: {}): string;
    public messageSent(messageId: string): Promise<void>;
    public disconnected(sessionId: string): Promise<void>;
    public kill(): void;
    public getPort(): number;
  }

  export default MockStompBroker;
}

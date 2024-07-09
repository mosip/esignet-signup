import {v4 as uuid} from "uuid";
import http, { Server } from "http";
import StompServer from "stomp-broker-js";
import waitUntil from "./waitUntil";

// interface Global extends NodeJS.Global {
//   TextEncoder: TextEncoder;
//   TextDecoder: TextDecoder;
// }

// declare var global: Global;

// global.TextEncoder = global.TextEncoder || TextEncoder;
// global.TextDecoder = global.TextDecoder || TextDecoder;

type CallNextMiddleWare = () => boolean;
type MiddlewareStrategy = [
  string,
  (args: { sessionId: string; frame: Frame }) => void
];

interface Socket {
  sessionId: string;
}

interface MiddlewareArgs {
  frame: Frame;
}

interface Frame {
  headers: {
    mockMessageId: string;
  };
}

interface Session {
  sessionId: string;
  hasConnected: boolean;
  hasReceivedSubscription: boolean;
  hasSentMessage: boolean;
  hasDisconnected: boolean;
}

interface Sessions {
  [sessionId: string]: Session;
}

interface Config {
  port?: number;
  portRange?: [number, number];
  endpoint?: string;
}

class MockStompBroker {
  private static PORTS_IN_USE: number[] = [];
  private static BASE_SESSION = {
    hasConnected: false,
    hasReceivedSubscription: false,
    hasSentMessage: false,
    hasDisconnected: false
  };

  private static getRandomInt(min: number, max: number): number {
    return Math.floor(Math.random() * (max - min)) + min;
  }

  private static getPort(portRange: [number, number] = [8000, 9001]): number {
    const minInclusive = portRange[0];
    const maxExclusive = portRange[1];
    const port = this.getRandomInt(minInclusive, maxExclusive);

    return this.PORTS_IN_USE.includes(port) ? this.getPort() : port;
  }

  private readonly port: number;
  private readonly httpServer: Server;
  private readonly stompServer: any;
  private readonly sentMessageIds: string[] = [];
  private queriedSessionIds: string[] = [];
  private sessions: Sessions = {};

  constructor({ port, portRange, endpoint = "/websocket" }: Config = {}) {
    this.thereAreNewSessions = this.thereAreNewSessions.bind(this);
    this.registerMiddlewares = this.registerMiddlewares.bind(this);
    this.setMiddleware = this.setMiddleware.bind(this);

    this.port = port || MockStompBroker.getPort(portRange);
    this.httpServer = http.createServer();

    this.stompServer = new StompServer({
      server: this.httpServer,
      path: endpoint
    });

    this.registerMiddlewares();
    this.httpServer.listen(this.port);
  }

  public async newSessionsConnected(): Promise<string[]> {
    await waitUntil(this.thereAreNewSessions, "No new sessions established");

    const newSessionsIds = Object.values(this.sessions)
      .filter(({ sessionId }) => !this.queriedSessionIds.includes(sessionId))
      .filter(({ hasConnected }) => hasConnected)
      .map(({ sessionId }) => sessionId);

    this.queriedSessionIds = this.queriedSessionIds.concat(newSessionsIds);

    return newSessionsIds;
  }

  public subscribed(sessionId: string) {
    return waitUntil(() => {
      const session = this.sessions[sessionId];
      return Boolean(session && session.hasReceivedSubscription);
    }, `Session ${sessionId} never subscribed to a topic`);
  }

  public scheduleMessage(
    topic: string,
    payload: any,
    headers: {} = {
      "content-type": "application/json;charset=UTF-8"
    }
  ): string {
    const body = JSON.stringify(payload);
    const mockMessageId = uuid();
    this.stompServer.send(`/${topic}`, { ...headers, mockMessageId }, body);

    return mockMessageId;
  }

  public messageSent(messageId: string) {
    return waitUntil(
      () => this.sentMessageIds.includes(messageId),
      `Message ${messageId} was never sent`
    );
  }

  public disconnected(sessionId: string) {
    return waitUntil(() => {
      const session = this.sessions[sessionId];

      return Boolean(session && session.hasDisconnected);
    }, `Session ${sessionId} never disconnected`);
  }

  public kill() {
    this.httpServer.close();
  }

  public getPort() {
    return this.port;
  }

  private thereAreNewSessions(): boolean {
    const numberOfSessions = Object.entries(this.sessions).length;
    const numberOfSessionsQueried = this.queriedSessionIds.length;

    return numberOfSessions - numberOfSessionsQueried > 0;
  }

  private setMiddleware([event, middlewareHook]: MiddlewareStrategy) {
    this.stompServer.setMiddleware(
      event,
      (socket: Socket, args: MiddlewareArgs, next: CallNextMiddleWare) => {
        process.nextTick(() =>
          middlewareHook({ sessionId: socket.sessionId, frame: args.frame })
        );

        return next();
      }
    );
  }

  private registerMiddlewares() {
    const strategies: MiddlewareStrategy[] = [
      [
        "connect",
        ({ sessionId }) =>
          (this.sessions[sessionId] = {
            ...MockStompBroker.BASE_SESSION,
            sessionId,
            hasConnected: true
          })
      ],
      [
        "subscribe",
        ({ sessionId }) =>
          (this.sessions[sessionId].hasReceivedSubscription = true)
      ],
      [
        "send",
        ({ frame }) => this.sentMessageIds.push(frame.headers.mockMessageId)
      ],
      [
        "disconnect",
        ({ sessionId }) => (this.sessions[sessionId].hasDisconnected = true)
      ]
    ];

    strategies.forEach(this.setMiddleware);
  }
}

export default MockStompBroker;

import {Client, createClient, MessageType} from './client.js';
import {authenticate} from './support.js';


/**
 * Demonstrates GraphQL subscription using the Apollo graphql-transport-ws sub-protocol. The client code is taken from
 * the reference implementation at https://github.com/enisdenjo/graphql-ws
 *
 * There is a retry loop that detects whether the OIDC access token has expired. If so, a new client and subscription
 * are created with a new access token.
 */


process.on('uncaughtException', (err) => {
  console.error('Uncaught Exception:', err);
});

class SubscriptionClient {
  private client?: Client;

  public async startSubscription(query: string): Promise<boolean> {

    let retry = false;
    try {
      this.client = await this.createClient();
      const subscription = this.client.iterate({query: query});

      for await (const result of subscription) {
        console.log("Subscription response: ", JSON.stringify(result, null, 2));
      }

    } catch (e) {
      retry = this.needsTokenRefresh(e);
      console.error(`Caught exception (retry = ${retry})`, e);
    }

    return retry;
  }

  public terminate() {
    if (this.client) {
      this.client.dispose();
      this.client.terminate();
    }
  }

  private async createClient(): Promise<Client> {
    const response = await authenticate();

    const accessToken = response.access_token;
    console.log("Access token: ", accessToken);


    const client =  createClient({
      // TODO: Should we use a different mechanism for passing the access token?
      //       Including in connection params which gets sent with ConnectionInit message for now.
      url: `ws://localhost:8081/graphql?realm=master&access_token=${accessToken}`,
      connectionParams: async () => {
        const result = await authenticate();
        return {
          accessToken: result?.access_token,
          realm: "master"
        };
      },
      keepAlive: 10000,
      lazy: false,
      onNonLazyError: (error: unknown) => {
        console.error('nonLazyError: ', error);
      },
      on: {
        connected: () => {
          console.log("Connected!!!");
        },
        closed: (event: any) => {
          console.log(`Closed: reason = ${event.reason}, code = ${event.code}`);
        },
        error: (error: any) => {
          console.error(`Error: ${error.message}`);
        },
        ping: async (received: boolean, payload?: Record<string, unknown>) => {
          console.log(`Ping: received(${received})`);

          // Check if this is a token expiry ping. If so, get a new access token and send it back to the server
          // in a Pong message
          if (received && payload) {
            const expires = payload['accessTokenExpires'];

            if (expires) {
              console.info(`Received a token expiry Ping message. Token expires at ${expires}`);
              const authResponse = await authenticate();
              const accessToken = authResponse.access_token;
              const message: any = {
                type: MessageType.Pong,
                payload: {
                  accessToken: accessToken
                }
              }

              await client.sendMessage(message);
            }
          }
        },
        pong: (received: boolean) => {
          console.log(`Pong: received(${received})`);
        },
        message: (message: any) => {
          // console.log('Message: ', JSON.stringify(message, null, 2));
        }
      }
    });

    return client;
  }

  private needsTokenRefresh(errors: any): boolean {
    const refresh =  errors && Array.isArray(errors)
      && errors.map(e => e?.extensions?.code).includes('NotAuthorizedException');

    return refresh;
  }
}

const query = `
      subscription {
        events {
          id
          details
          type
          resourceType
          resourcePath
          realm {
            name
          }
        }
      }
      `;

let retry = true;
while (retry) {
  const subscriptionClient = new SubscriptionClient();
  retry = await subscriptionClient.startSubscription(query);
  console.log(`Retry = ${retry}`);

  if (retry) {
    subscriptionClient.terminate();
  }

}


import {createClient} from './client.js';
import { authenticate } from './support.js';

/**
 * Demonstrates GraphQL subscription using the Apollo graphql-transport-ws sub-protocol. The client code is taken from
 * the reference implementation at https://github.com/enisdenjo/graphql-ws
 */
(async () => {

  const response = await authenticate();

  console.log("Access token: ", response.access_token);
  const client = createClient({
    // TODO: Should we use a different mechanism for passing the access token?
    //       Including in connection params for now.
    url: `ws://localhost:8081/graphql?access_token=${response.access_token}`,
    connectionParams: {
      access_token: response.access_token
    },
    keepAlive: 10000,
    lazy: false,
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
      ping: (received: boolean) => {
        console.log(`Ping: received(${received})`);
      },
      pong: (received: boolean) => {
        console.log(`Pong: received(${received}`);
      },
      message: (message: any) => {
        // console.log('Message: ', JSON.stringify(message, null, 2));
      }
    }

  });

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


  client.subscribe({query: query}, {
    next: (data) => {
      console.log("Subscription response: ", JSON.stringify(data, null, 2))
    },
    error: (error) => {
      console.error('Subscription error: ', JSON.stringify(error, null, 2));
    },
    complete: () => {
      console.log("Subscription complete!");
    }
  });

})();
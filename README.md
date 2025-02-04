![CI Build](https://github.com/blevine/keycloak-graphql/actions/workflows/maven.yml/badge.svg?cache-control=no-cache)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
![](https://img.shields.io/badge/Keycloak-26.0.2-blue)
# Keycloak GraphQL API
A Keycloak extension that supports the functionality of the [Keycloak Admin REST API](https://www.keycloak.org/docs-api/25.0.2/rest-api/index.html) in GraphQL.

## WARNING: This project is in its formative stages, please do not use in production!

After a couple of years working on a project that was heavily dependent on the Keycloak REST API, I decided
to create something "better". I found theREST API difficult to work with due to its adherence to
RESTful principles with strict separation between resources. This made it difficult to, for example, 
retrieve all information about the users in a realm (e.g. the standard user information, attributes,
groups, roles, etc.) without multiple REST invocations and in-memory "joins". In addition, the REST API
provided only very coarse-grained control over what information was returned for some resources by
specifying whether a "brief" or "full" representation was to be returned.

I had already become interested GraphQL and was building a GraphQL API as part of this project. It seemed
clear that I could address the problems mentioned above by providing a GraphQL API for Keycloak.

## Generalized roadmap (no timeline yet)
- Replicate the functionality of the [Keycloak Admin REST API](https://www.keycloak.org/docs-api/25.0.2/rest-api/index.html) in GraphQL.
- Make it easier for consumers of the API to specify the shape of the data the want to retrieve.
- Support ordering and filtering on queries.
- Support performant "joining" of certain entities, e.g. users + roles + groups.
- Support write (GraphQL mutation) operations.
- Support real-time updates/notifications using GraphQL subscriptions.

## Contributing
I am not accepting pull requests at this time. Please feel free to post questions/comments in [Discussions](https://github.com/blevine/keycloak-graphql/discussions).

## Current state of the code

- Currently built against Keycloak 26.0.2 although I will probably increment this as I go. Eventually, there will 
be tagged releases to support some number of recent Keycloak versions.
- Initially I'm concentrating on the following types: Realm, Client, User, Group, Role and getting the queries
on those types right without regard to performance improvements.
- Concentrating first on read operations (GraphQL queries). I'm slowly adding support for Mutations with
the first example Mutation being `createRealm`.
- A sample Subscription service is included that allows the caller to register for Keycloak Events
and Admin Events. See below for implementation details.
- The GraphQL types rely heavily on some of the existing REST code, most notably the *Resource 
classes in the org.keycloak.admin.client.resource package and the *Representation classes in the 
org.keycloak.representations.idm package. The *Resource classes include filtering
based on role-based access control so this seems the safest route for now. GraphQL *Type classes mostly just delegate 
to those classes and sometimes to the related *Model classes in the
org.keycloak.models package. For certain optimizations, I'll probably break away from using these classes and write 
my own SQL queries. This will be done on a case-by-case basis.
- Built and tested against Keycloak 26.0.1. Once we do actual releases, a compatibility table will be provided.

## Access control, existence, and errors
Keycloak imposes role-based access controls on its resources. When a GraphQL query returns a collection, any items in that collection
to which the caller does not have access are removed from the collection. If the caller does not have access to the
collection itself, a Page with an empty items array is returned. For queries returning single results, a null is
returned when the caller does not have access.

There is no way to determine whether a null result is due to access control or the item not being found. In other words,
errors are not raised in either case. This is intentional for security reasons. For example, if we raised different errors for
access control and existence on user entities, a caller could determine whether a user existed by name (AccessDenied vs
NoSuchUser error).

## Map fields
Certain fields (especially in Realm), return Maps of varying complexity. These will most likely be wrapped in specific
GraphQL types at some point.

## Support for subscriptions
A not-so-robust subscriptions implementation is included for Keycloak events and adminEvents. The back-end 
sits on top of a Keycloak EventListenerProvider that hooks Keycloak events and admin events. The server is based
on [Java API for WebSockets](https://projects.eclipse.org/projects/ee4j.websocket) using the 
[Eclipse Tyrus](https://github.com/eclipse-ee4j/tyrus) reference implementation. A protocol handler is included
that implements the [Apollo graphql-transport-ws sub-protocol](https://github.com/enisdenjo/graphql-ws/blob/master/PROTOCOL.md) 
(although this particular implementation is not 100% compliant)

This is obviously suboptimal. I would have preferred to support Websockets on the same
Keycloak HTTP(S) port on which we support standard GraphQL traffic. However, I couldn't figure out how to integrate
a Websocket server. _Any advice in that area would be appreciated_. I suspect that one or more Quarkus extensions would
be helpful here, but I was not successful in integrating Quarkus extensions into my Keycloak extension. In fact, it
looks like enhancing Keycloak with Quarkus extensions is not currently supported.

### Subscriptions and authentication/authorization
A sample client is included in the graphql-ws-client module which is based on the 
[graphql-ws client](https://github.com/enisdenjo/graphql-ws). I've taken the approach of making the client responsible
for authenticating and obtaining an access token. The graphql-transport-ws Ping and Pong messages are used as follows:
1. The client obtains an access token from some IdP and passes the access token as part of the payload in the 
ConnectionInit message.
2. The server validates the access token and stores it in the session. If the access token cannot be validated, the
socket is closed.
3. The access token's expiration time is retrieved and a timer is started that times out just before the access
token expires.
4. When the timer expires, a Ping message is sent to the client with a payload indicating that the access token has expired.
5. The client is then responsible for contacting the IdP to obtain a new access token (i.e., by refresh or re-authentication).
6. The client sends the new access token back in a Pong message.
7. The server validates the access token and stores it in the session. If the access token cannot be validated, the
socket is closed.

When the client creates a new subscription, the access token is retrieved from the session, validated, and added to the GraphQL context.
The access token is also retrieved from the session, validated, and added to the GraphQL context whenever an established 
subscription event "fires". If the access token cannot be validated, the socket is closed.

Here is some additional [discussion of credential refresh](https://github.com/enisdenjo/graphql-ws/discussions/292) using 
the graphql-transport-ws protocol.

### Enabling the event subscriptions
To activate the event listener to support the event and adminEvent subscriptions, some Keycloak configuration is required:
1. Log in to the Keycloak Admin console
2. Navigate to Realm Settings
3. Click on the Events tab
4. In the Event Listeners field, add `multicast-event-listener`. 

## Building the extension JAR file
Run `mvn clean package`. The resulting JAR file will be located in ./graphql/target/net.brianlevine.keycloak-graphql.jar.

## Running the tests
Run `mvn test`

## Debugging the tests
Use one of the methods described [here](https://maven.apache.org/surefire/maven-surefire-plugin/examples/debugging.html).

## Debugging the Keycloak container while running the tests
The tests programmatically instantiate a [Keycloak test container](https://github.com/dasniko/testcontainers-keycloak). To
debug into the Keycloak test container (as opposed to debugging the tests themselves):
1. Run `DEBUG_PORT=nnnn mvn test`. During startup, you'll see messages indicating that the container is starting in DEBUG
mode. The container will wait until you've attached your debugger to proceed.
2. Attach your debugger to the Keycloak server on the port you chose. _You'll need to do this twice. The first time,
the attach succeeds, but then exits after a few seconds. Attach your debugger again to actually attach to the Keycloak
process and begin debugging_. (See https://github.com/dasniko/testcontainers-keycloak/issues/117 and 
https://github.com/keycloak/keycloak/discussions/12679#discussioncomment-3307999)

## Running the development server
Run `docker compose up`

in the root directory.

The docker-compose.yml file is configured to run the correct version of Keycloak and mount the JAR file on the
/opt/keycloak/providers directory.

## Using the extension in your server
Copy the JAR file to the <KEYCLOAK_DIR>/providers directory. Re-start Keycloak.

## Tools
A couple of tools are provided as development aids. You'll need to create a 'graphql-tools' realm role and assign this
role to users to whom you want to grant access. You'll also need to create a 'keycloak-graphql' client. 
The easiest way to do this is to import the client using the keycloak-graphql-client.json file in the root directory of this
project.

### GraphiQL
The [GraphiQL](https://github.com/graphql/graphiql) tool is included in this extension. To access GraphiQL, point your
browser at http://localhost:8080/realms/your-realm-name/graphql/graphiql.


### Viewing/downloading the GraphQL schema
To view the Keycloak GraphQL schema, point your browser at http://localhost:8080/realms/your-realm-name/graphql/schema.
Click the Download... button to download the schema.

### Testing queries from the command-line
GraphiQL is probably the best way to test out your queries. If you want to test your queries from the command-line,
a shell script is provide at ./scripts/graphql_test.sh.

Run `graphql_test.sh username password realm_name full_path_to_file_containing_graphql_query.json`.

You'll need to have the 'curl' and 'jq' commands installed.

## Some interesting queries.
Note: All queries except 'currentUser' must be enclosed in a realm field. You can indicate a specific realm by adding
the `id` or `name`. Providing no arguments indicates the current realm (i.e., the realm against which you authenticated).

The ordering of items
in paged queries is determined by the underlying Keycloak REST code. Specifying explicit ordering and filtering will 
eventually be supported for all queries that return collections.


### Get the users in the current realm and their attributes, roles, and groups
```graphql
query {
  realm {
		users {
      items {
        id
        username
        firstName
        lastName
        attributes
        roles {
          items {
            name
            clientRole
          }
        }
        groups {
          items {
            name
            path
          }
        }
      }
    }
  }
}
```

### Get the current authenticated user (the user making the call to the GraphQL endpoint)

```graphql
query {
  currentUser {
    id
    username
    email
    firstName
    lastName
    roles {
      items {
        name
        clientRole
      }
    }
  }
}
```

### Get the first 10 realms and include the total number of realms and total number of pages
This demonstrates how paging works. You can define the start point (default is 0) and pagesize (default is 100)
using the 'start' and 'limit' arguments. The collection is returned in the 'items' field which is where you define 
which fields you want returned. This will return all realms to which the caller has view access.

```graphql
query {
  realms(start:0, limit:10) {
    totalItems
    totalPages
    items {
      id
      name
    }
  }
}
```

### Get the first 5 users in the current realm

```graphql
query {
  realm {
    users(start:0, limit:5) {
      items {
        username
        firstName
        lastName
      }
    }
  }
}
```

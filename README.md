![CI Build](https://github.com/blevine/keycloak-graphql/actions/workflows/maven.yml/badge.svg?cache-control=no-cache)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
![](https://img.shields.io/badge/Keycloak-25.0.2-blue)
# Keycloak GraphQL API
A Keycloak extension that implements the [Keycloak Admin REST API](https://www.keycloak.org/docs-api/25.0.2/rest-api/index.html) in GraphQL.

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

## Goals
- Replicate the functionality of the [Keycloak Admin REST API](https://www.keycloak.org/docs-api/25.0.2/rest-api/index.html) in GraphQL.
- Make it easier for consumers of the API to specify the shape of the data the want to retrieve.
- Support performant "joining" of certain entities, e.g. users + roles + groups.
- Support real-time updates/notifications using GraphQL subscriptions.

## Contributing
I am not accepting pull requests at this time. Please feel free to post questions/comments in [Discussions](https://github.com/blevine/keycloak-graphql/discussions).

## Current state of the code

- Currently built against Keycloak 25.0.2 although I will probably increment this as I go. Eventually, there will 
be tagged releases to support some number of recent Keycloak versions.
- Initially I'm concentrating on the following types: Realm, Client, User, Group, Role and getting the queries
on those types right without regard to performance improvements.
- Only read operations (GraphQL queries) are supported. Mutations and subscriptions are not supported right now.
- The GraphQL types rely heavily on some of the existing REST code, most notably the *Resource 
classes in the org.keycloak.admin.client.resource package and the *Representation classes in the 
org.keycloak.representations.idm package. The *Resource classes include filtering
based on role-based access control so this seems the safest route for now.
- GraphQL *Type classes mostly just delegate to those classes and sometimes to the related *Model classes in the
org.keycloak.models package. For certain optimizations, I'll probably break away from using these classes and write 
my own SQL queries. This will be done on a case-by-case basis.
- GraphQL variables are not yet supported.
- Built and tested against Keycloak 25.0.2. Once we do actual releases, a compatibility table will be provided.

## Access control, existence, and errors
Keycloak imposes role-based access controls on its resources. When a GraphQL query returns a collection, any items in that collection
to which the caller does not have access are removed from the collection. If the caller does not have access to the
collection itself, a page with an empty items array is returned. For queries returning single results, a null is
returned when the caller does not have access.

There is no way to determine whether a null result is due to access control or the item not being found. In other words,
errors are not raised in either case. This is intentional for security reasons. For example, if we raised different errors for
access control and existence on user entities, a caller could determine whether a user existed by name (AccessDenied vs
NoSuchUser error).

## Map fields
Certain fields (especially in Realm), return Maps of varying complexity. These will most likely be wrapped in specific
GraphQL types at some point.

## Building the extension JAR file
Run `mvn clean package`. The resulting JAR file will be located in ./graphql/target/net.brianlevine.keycloak-graphql.jar.

## Running the tests
Run `mvn test`

## Debugging the tests
Use one of the methods described [here](https://maven.apache.org/surefire/maven-surefire-plugin/examples/debugging.html).

## Debugging the Keycloak container while running the tests
The tests programmatically instantiate a [Keycloak test container](https://github.com/dasniko/testcontainers-keycloak). To
debug into the Keycloak test container (as opposed to debugging the tests themselves):
1. Run `DEBUG_PORT=nnnn mvn text`. During startup, you'll see messages indicating that the container is starting in DEBUG
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
Tools are provided as development aids. You'll need to create a 'graphql-tools' realm role and assign this
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
the id or name arguments or the current (authenticated) realm by providing no arguments.

The ordering of items
in paged queries is determined by the underlying Keycloak REST code. Specifying explicit ordering and filtering will 
eventually be supported for all queries that return collections.

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
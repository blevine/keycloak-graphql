![CI Build](https://github.com/blevine/keycloak-graphql/actions/workflows/maven.yml/badge.svg?cache-control=no-cache)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
# Keycloak GraphQL API

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


## Current state of the code

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


## Building the JAR file
Run `mvn clean package`.

The resulting JAR file will be located in ./graphql/target/net.brianlevine.keycloak-graphql.jar.

## Running the development server
Run `docker compose up`

in the root directory.

The docker-compose.yml file is configured to run the correct version of Keycloak and mount the JAR file on the
/opt/keycloak/providers directory.

## Using the extension in your server
Copy the JAR file to the <KEYCLOAK_DIR>/providers directory. Re-start Keycloak.

## Tools
Tools are provided as development aids. You'll need to create a 'graphql-tools' realm role and assign this
role to users who you want to grant access to the tools. You'll also need to create a 'keycloak-graphql' client. 
The easiest way to do this is to import the client using the keycloak-graphql-client.json file in the root directory of this
project.

### GraphiQL
The [GraphiQL](https://github.com/graphql/graphiql) tool is included in this extension. To access GraphiQL, point your
browser at http://localhost:8080/realms/your-realm-name/graphql/graphiql.


### Viewing/downloading the GraphQL schema
To view the Keycloak GraphQL schema, point your browser at http://localhost:8080/realms/your-realm-name/graphql/graphiql.
Click the Download... button to download the schema.

### Testing queries from the command-line
GraphiQL is probably the best way to test out your queries. If you want to test your queries from the command-line,
a shell script is provide at ./scripts/graphql_test.sh.

Run `graphql_test.sh username password realm_name full_path_to_query.json`.

You'll need to have 'curl' and 'jq' installed.

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
which fields you want returned.

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

### Get the first 5 users in the default realm

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
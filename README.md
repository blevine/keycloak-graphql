![Keycloak GraphQL](https://github.com/github/docs/actions/workflows/maven.yml/badge.svg)
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
Initially, I want to concentrate on getting the GraphQL schema (especially the GraphQL types) right and
so I haven't concentrated at all on performance. I'm also supporting only read operations
(GraphQL queries) and no write operations (GraphQL mutations) at this time. To accomplish this, I've relied heavily
on some of the existing REST code, most notably the *Resource classes in the org.keycloak.admin.client.resource
package and the *Representation classes in org.keycloak.representations.idm package. So the GraphQL *Type classes
mostly just delegate to those classes and sometimes the related *Model classes in the org.keycloak.models package. For
certain optimizations, I'll probably break away from using these classes and write my own SQL queries. This will be on
a case-by-case basis.

At this time, I'm concentrating on the following types: Realm, Client, User, Group, Role.

## Building the JAR file
Run `mvn clean package`. The resulting JAR file will be located in ./graphql/target/net.brianlevine.keycloak-graphql.jar.

## Running the development server
The docker-compose.yml file is configured to run the correct version of Keycloak and mount the JAR file on the
/opt/keycloak/providers directory .  Just run `docker compose up` in the root directory.

## Using the extension in your server
Copy the JAR file to the <KEYCLOAK_DIR>/providers directory. Re-start Keycloak.

## GraphiQL
The [GraphiQL](https://github.com/graphql/graphiql) tool is included in this extension. To access GraphiQL, point your
browser at http://localhost:8080/realms/your-realm-name/graphql/graphiql.

Note: You'll need to create a realm role called 'graphql-tools'. Users must have this role to access GraphiQL.

## Viewing/downloading the GraphQL schema
To view the Keycloak GraphQL schema, point your browser at http://localhost:8080/realms/your-realm-name/graphql/graphiql.
Click the Download... button to download the schema.

Note: Users must have the 'graphql-tools' realm role mentioned above.



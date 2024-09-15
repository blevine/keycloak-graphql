![Keycloak GraphQL](https://github.com/blevine/keycloak-graphql/actions/workflows/maven.yml/badge.svg?cache-control=no-cache)
# Keycloak GraphQL API

## WARNING: This project is in its formative stages, please do not use in production!

After a couple of years working on a project that was heavily dependent on the Keycloak REST API, I decided
to create something "better". I found theREST API difficult to work with due to its adherence to
RESTful principles with strict separations between resources. This made it difficult to, for example, 
retrieve all information about the users in a realm (e.g. the standard user information, attributes,
groups, roles, etc.) without multiple REST invocations and in-memory "joins". In addition, the REST API
provided only very coarse-grained control over what information was returned for some resources by
specifying whether a "brief" or "full" representation was to be returned.

I had already become interested GraphQL and was building a GraphQL API as part of this project. It seemed
clear that I could address the problems mentioned above by providing a GraphQL API for Keycloak. I fully
realize that there have been many discussions about why REST is preferable to GraphQL and _vice versa_. My
short response to this is: if you're not a fan of GraphQL, please free to not use this API. However,
if you think this project has merit, I encourage you to contribute with comments, bug reports, or PRs.

## General design philosophy for GraphQL types
I eventually want to optimize this queries and so I wanted the GraphQL code to be independent of the
Keycloak REST resources (classes in org.keycloak.services.resource). However, since my initial goal was to get the
schema correct, I've delegated certain resolvers to those *Resource classes with an eye towards evaluating the performance
of the *Resource code and potentially rewriting it in the GraphQL resolver. However the representations of those resources
(classes in org.keycloak.representations.idm) seemed like they'd be useful. This is
especially true since many of the the resource classes contain code that filters representations
based on the role(s) of the caller and configured policies. So the *Type classes in 
(net.brianlevine.keycloak.graphql.types) wrap the *Representation and *Model classes and in most cases,
delegate to the wrapped class. In some cases, the *Resource classes are used to convert models to representations.

## Current state of the code
I wanted to initially concentrate on getting the GraphQL schema (especially the types) right and
so I haven't concentrated at all on performance. I'm also supporting only read operations
(GraphQL queries) and no write operations (GraphQL mutations) at this time.

## GraphiQL
The [GraphiQL](https://github.com/graphql/graphiql) tool is included in this extension. To access GraphiQL, navigate to
http://localhost:8080/realms/your-realm-name/graphql/graphiql.

Note: You'll need to create a realm role called 'graphiql-access'. Users must have this role to access GraphiQL.

## Modules

- graphql: The Keycloak plugin that implements the GraphQL endpoint,  /graphql.


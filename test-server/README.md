# Test Server
<p></p>

This is a NodeJS-based test server that incorporates the [GraphiQL](https://github.com/graphql/graphiql) tool.
It authenticates against your local Keycloak (running on localhost:8080) and proxies
GraphQL requests to Keycloak's GraphQL endpoint at localhost:4000/graphql. The plan is
to incorporate GraphiQL into the Keycloak plugin so it can be served by the Keycloak
server once I figure out how to do that.
<p></p>

## Install and run
- Prerequisite: Run the Keycloak dev server: (from the parent directory) `docker compose up`
- To install: `yarn or npm install`
- To run: `node ./server.mjs`


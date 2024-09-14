<html xmlns="http://www.w3.org/1999/html">
<head>
    <title>GraphiQL</title>
    <style>
        body {
            height: 100%;
            margin: 0;
            width: 100%;
            overflow: hidden;
        }

        #graphiql {
            height: 100vh;
        }
    </style>
    <script src="${url.resourcesPath}/../keycloak-graphql-theme/foo.js"></script>
    <script src="${url.resourcesPath}/../keycloak-graphql-theme/keycloak.min.js"></script>
    <!--
      This GraphiQL example depends on Promise and fetch, which are available in
      modern browsers, but can be "polyfilled" for older browsers.
      GraphiQL itself depends on React DOM.
      If you do not want to rely on a CDN, you can host these files locally or
      include them directly in your favored resource bundler.
    -->
    <script
            crossorigin
            src="https://unpkg.com/react@18/umd/react.production.min.js"
    ></script>
    <script
            crossorigin
            src="https://unpkg.com/react-dom@18/umd/react-dom.production.min.js"
    ></script>
    <!--
      These two files can be found in the npm module, however you may wish to
      copy them directly into your environment, or perhaps include them in your
      favored resource bundler.
     -->
    <script
            src="https://unpkg.com/graphiql/graphiql.min.js"
            type="application/javascript"
    ></script>
    <link rel="stylesheet" href="https://unpkg.com/graphiql/graphiql.min.css" />
    <!--
      These are imports for the GraphIQL Explorer plugin.
     -->
    <script
            src="https://unpkg.com/@graphiql/plugin-explorer/dist/index.umd.js"
            crossorigin
    ></script>

    <link
            rel="stylesheet"
            href="https://unpkg.com/@graphiql/plugin-explorer/dist/style.css"
    />
</head>

<body>
<div id="graphiql">Loading...</div>

<script>
  const keycloak = new Keycloak({
    url: 'http://localhost:8080',
    realm: 'master',
    clientId: 'keycloak-graphql',
  });

  console.log("Authenticating...");

  const rootElement = document.getElementById('graphiql');

  keycloak.init({onLoad: 'login-required'}).then((authenticated) => {
    if (authenticated) {
      if (keycloak.hasRealmRole('graphql-tools')) {

        console.log("User is authenticated!");
        const root = ReactDOM.createRoot(rootElement);

        const fetcher = GraphiQL.createFetcher({
          credentials: 'include',
          url: window.location.origin + "/realms/" + "${realm.name}" + "/graphql",
          headers: {
            Accept: 'application/json',
            'Content-Type': 'application/json',
            'Authorization': 'Bearer ' + keycloak.token
          }
        });

        const explorerPlugin = GraphiQLPluginExplorer.explorerPlugin();
        root.render(
          React.createElement(GraphiQL, {
            fetcher,
            defaultEditorToolsVisibility: true,
            plugins: [explorerPlugin],
          }),
        );

      }
      else {
        rootElement.innerHTML = "Not authorized. User does not have the required Keycloak role to access GraphiQL."
      }

    } else {
      rootElement.innerHTML = "User authentication failed!";
    }
  }).catch((e) => {
    rootElement.innerHTML = "Could not authenticate user. See browser console log for more info.";
    console.log("Could not authenticate the user!: ",e);
  });

</script>

</body>
</html>
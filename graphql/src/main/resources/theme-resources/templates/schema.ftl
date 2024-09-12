<html>
    <head>
        <title>Keycloak GraphQL Schema</title>
        <script src="${url.resourcesPath}/../keycloak-graphql-theme/keycloak.min.js"></script>

        <link rel="stylesheet" href="${url.resourcesPath}/../keycloak-graphql-theme/default.min.css">
        <script src="${url.resourcesPath}/../keycloak-graphql-theme/highlight.min.js"></script>
        <script src="${url.resourcesPath}/../keycloak-graphql-theme/graphql.min.js"></script>

        <script>
          function save(filename, data) {
            const blob = new Blob([data], {type: 'text/plain'});
            if(window.navigator.msSaveOrOpenBlob) {
              window.navigator.msSaveBlob(blob, filename);
            }
            else{
              const elem = window.document.createElement('a');
              elem.href = window.URL.createObjectURL(blob);
              elem.download = filename;
              document.body.appendChild(elem);
              elem.click();
              document.body.removeChild(elem);
            }
          }
        </script>
    </head>
    <body>
        <div id="download">
            <button onclick = "save('keycloak.graphql', `${schema}`)">Download keycloak.graphql...</button>
        </div>

        <pre>
            <code id="schema" class="language-graphql"></code>
        </pre>


        <script>
          const keycloak = new Keycloak({
            url: 'http://localhost:8080',
            realm: 'master',
            clientId: 'keycloak-graphql',
          });

          console.log("Authenticating...");

          const rootElement = document.getElementById('schema');

          keycloak.init({onLoad: 'login-required'}).then((authenticated) => {
            if (authenticated) {
              if (keycloak.hasRealmRole('graphiql-access')) {

                console.log("User is authenticated!");
                rootElement.innerHTML = `${schema}`;
                hljs.highlightAll();
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
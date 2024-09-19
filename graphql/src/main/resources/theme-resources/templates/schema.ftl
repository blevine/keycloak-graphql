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
            <button onclick = "save('keycloak.graphql', document.getElementById('schema').textContent)">Download keycloak.graphql...</button>
        </div>

        <pre>
            <code id="schema" class="language-graphql"></code>
        </pre>


        <script>
          const keycloak = new Keycloak({
            url: window.location.origin,
            realm: '${realm.name}',
            clientId: 'keycloak-graphql',
          });

          const rootElement = document.getElementById('schema');

          keycloak.init({onLoad: 'login-required'}).then((authenticated) => {
            if (authenticated) {
              if (keycloak.hasRealmRole('graphql-tools')) {

                console.log("User is authenticated!");

                // Uses the fact that the endpoint for this form is /schema and the endpoint to retrieve the
                // schema is /schemaAuth.
                const url = window.location.href + 'Auth';

                fetch(url, {
                  method: 'GET',
                  credentials: 'include',
                  headers: {
                    Authorization: 'Bearer ' + keycloak.token
                  }
                })
                  .then(res => res.text())
                  .then(s => {
                    rootElement.innerHTML = s;
                    hljs.highlightAll();

                  })
                  .catch(e => { console.log(e); });
              }
              else {
                rootElement.innerHTML = "<h2>Not authorized. User does not have the required role to access GraphiQL.</h2>"
              }

            } else {
              rootElement.innerHTML = "<h2>User authentication failed!</h2>";
            }
          }).catch((e) => {
            rootElement.innerHTML = "<h2>Could not authenticate user. See browser console log for more info.</h2>";
            console.log("Could not authenticate the user!: ",e);
          });
        </script>

    </body>
</html>
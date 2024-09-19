#!/bin/bash

# Usage: graphql_test username password realm full_path_to_query_file.js

USER=$1
PASSWORD=$2
REALM=$3
FILE=$4


RESULT=`curl --data "username=${USER}&password=${PASSWORD}&grant_type=password&client_id=admin-cli" http://localhost:8080/realms/${REALM}/protocol/openid-connect/token`
TOKEN=`echo $RESULT | sed 's/.*access_token":"//g' | sed 's/".*//g'`

echo $TOKEN


curl  http://localhost:8080/realms/${REALM}/graphql -X POST -d "@${FILE}"  -H "Content-Type: application/json" -H "Authorization: bearer $TOKEN" | jq

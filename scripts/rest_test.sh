#!/bin/bash

USER=$1
PASSWORD=$2


RESULT=`curl --data "username=${USER}&password=${PASSWORD}&grant_type=password&client_id=admin-cli" http://localhost:8080/realms/master/protocol/openid-connect/token`

echo $RESULT
TOKEN=`echo $RESULT | sed 's/.*access_token":"//g' | sed 's/".*//g'`

echo $TOKEN


curl  http://localhost:8080/realms/master/graphql -X POST -d @introspection.js  -H "Content-Type: application/json" -H "Authorization: bearer $TOKEN" | jq
#curl  "http://localhost:8081/auth/admin/realms/CustomerExternal/users?first=0&max=5&briefRepresentation=true" -H "Content-Type: application/json" -H "Authorization: bearer $TOKEN" | jq
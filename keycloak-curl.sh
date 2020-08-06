#!/bin/bash

if [ $# -ne 4 ]; then
  echo "Usage: . $0 hostname realm username clientid"
  echo "  options:"
  echo "    hostname: localhost:8080"
  echo "    realm:pbrealm"
  echo "    clientid:pbclient"
  echo "    For verify ssl: use 'y' (otherwise it will send curl post with --insecure)"
  exit 1  
fi

HOSTNAME=$1
REALM_NAME=$2
USERNAME=$3
CLIENT_ID=$4
SECURE=$5

KEYCLOAK_URL=http://$HOSTNAME/auth/realms/$REALM_NAME/protocol/openid-connect/token

echo "Using Keycloak: $KEYCLOAK_URL"
echo "realm: $REALM_NAME"
echo "client-id: $CLIENT_ID"
echo "username: $USERNAME"
echo "secure: $SECURE"

if [[ $SECURE = 'y' ]]; then
	INSECURE=
else 
	INSECURE=--insecure
fi

echo -n Password: 
read -s PASSWORD

export TOKEN=$(curl -s -X POST "$KEYCLOAK_URL" "$INSECURE" \
 -H "Content-Type: application/x-www-form-urlencoded" \
 -d "username=$USERNAME" \
 -d "password=$PASSWORD" \
 -d 'grant_type=password' \
 -d "client_id=$CLIENT_ID" | jq -r '.access_token')

echo $TOKEN

if [[ $(echo $TOKEN) != 'null' ]]; then
	export KEYCLOAK_TOKEN=$TOKEN
fi

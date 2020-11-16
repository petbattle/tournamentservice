# tournament project

This project uses Quarkus, the Supersonic Subatomic Java Framework.

If you want to learn more about Quarkus, please visit its website: https://quarkus.io/ .

## Running the application in dev mode

You can run your application in dev mode that enables live coding using:
```
./mvnw quarkus:dev
```

## Integration Testing

[1] Startup a mongodb server on localhost:27017 
```
e.g. mongod -vvv --dbpath=<some location>
```
[2] Run the integration test via maven
```
mvn verify -Pintegration
```

## Leaderboard

Browse to tournament
```bash
http://localhost:8080/tournament/leaderboard/af5f24cc-20ec-4086-9755-111c8da8b526
```

## Using helm and OpenShift

This will deploy the application and dependant infrastructure apps (datagrid,keycloak,mongodb)
```bash
oc new-project pet-battle-tournament
helm template my chart/ | oc apply -f- -n pet-battle-tournament
```

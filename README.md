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
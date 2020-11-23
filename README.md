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

Prerequisites
- oc login to OCP4 cluster with cluster-admin user

Deploy the cert-util operator (deploy this only once for the whole cluster)
```bash
CERTUTILS_NAMESPACE=cert-utils-operator
oc new-project "${CERTUTILS_NAMESPACE}"
helm repo add cert-utils-operator https://redhat-cop.github.io/cert-utils-operator
helm repo update
cert_utils_chart_version=$(helm search repo cert-utils-operator/cert-utils-operator | grep cert-utils-operator/cert-utils-operator | awk '{print $2}')
helm fetch cert-utils-operator/cert-utils-operator --version ${cert_utils_chart_version}
helm template cert-utils-operator-${cert_utils_chart_version}.tgz --namespace cert-utils-operator | oc apply -f- -n cert-utils-operator
rm -f cert-utils-operator-${cert_utils_chart_version}.tgz
oc -n ${CERTUTILS_NAMESPACE} wait --for condition=available --timeout=120s deployment/cert-utils-operator
```

Deploy the tournament service application and dependant infrastructure apps (datagrid,keycloak,mongodb) from this codebase
```bash
oc new-project pet-battle-tournament
helm template my chart/ | oc apply -f- -n pet-battle-tournament
```

OR deploy applications straight from the chart repository
```bash
oc new-project pet-battle-tournament
helm repo add petbattle https://petbattle.github.io/helm-charts
helm repo update
chart_version=$(helm search repo petbattle/pet-battle-tournament | grep petbattle/pet-battle-tournament | awk '{print $2}')
helm fetch petbattle/pet-battle-tournament --version ${chart_version}
helm template my pet-battle-tournament-${chart_version}.tgz | oc apply -f- -n pet-battle-tournament
rm -f pet-battle-tournament-${chart_version}.tgz
oc -n pet-battle-tournament  wait --for condition=available --timeout=120s deploymentconfig/my-pet-battle-tournament
```

And to delete
```bash
helm template my chart/ | oc delete -f- -n pet-battle-tournament
oc delete csv datagrid-operator.v8.1.1 keycloak-operator.v11.0.0
```

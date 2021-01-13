# tournament project

This project uses Quarkus, the Supersonic Subatomic Java Framework.

If you want to learn more about Quarkus, please visit its website: https://quarkus.io/ .

## Running the application in dev mode

You can run your application in dev mode that enables live coding using:
```
mvn quarkus:dev
```

## Integration Testing

Run the integration tests via maven and _Test Containers_
```
mvn verify -Pintegration
```

## Leaderboard

Browse to tournament
```bash
http://localhost:8080/tournament/leaderboard/af5f24cc-20ec-4086-9755-111c8da8b526
```

## Using helm and OpenShift

### Prerequisites
- oc login to OCP4 cluster with cluster-admin user

### Infrastructure
To deploy pet battle dependant infrastructure we need cluster admin privilege for things like operators and CRD's. The `TournamentService` application by itself only requires normal project-namespace admin privilege.

The infrastructure that requires privilege to install has been separated into a sub chart and is deployed via the `Chart.yaml` dependency
```bash
dependencies:
  - name: pet-battle-infra
    version: "1.0.0"
    repository: "https://petbattle.github.io/helm-charts"
```

You can change whether the infrastructure is deployed or not by using the tag (default: `true`)
```bash
helm template dabook chart/ --set tags.infra=false
```

To deploy a pre-packaged version of the chart including infra
```bash
helm repo add petbattle https://petbattle.github.io/helm-charts
helm repo update
helm install petbattle/pet-battle-tournament --set pet-battle-infra.operatorgroup.targetNamespaces={petbattle} --namespace petbattle --create-namespace --generate-name
```

### Deploy the cert-util operator (deploy this only once for the whole cluster)
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

### Deploy the tournament service application and dependant infrastructure apps (datagrid,keycloak,mongodb) from this codebase
```bash
oc new-project pet-battle-tournament
helm template dabook chart/ | oc apply -f- -n pet-battle-tournament
```

### Deploy grafana dashboard
```bash
oc adm policy add-cluster-role-to-user cluster-monitoring-view -z grafana-serviceaccount

helm template dabook chart/ --set grafana.BEARER_TOKEN=`oc serviceaccounts get-token grafana-serviceaccount`| oc apply -f- -n pet-battle-tournament
```
#### Enable User Workload Monitoring if not enabled

```bash
helm template dabook chart/ --set serviceMonitor.enableUserWorkloadMonitoring=true --set grafana.BEARER_TOKEN=`oc serviceaccounts get-token grafana-serviceaccount`| oc apply -f- -n pet-battle-tournament
```

### Change a default helm value
```bash
helm template dabook chart/ --set image_version=gha-noc-git-info | oc apply -f- -n pet-battle-tournament 
```

## OR 
deploy applications straight from the chart repository
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

## And to delete
```bash
helm template my chart/ | oc delete -f- -n pet-battle-tournament
oc delete csv datagrid-operator.v8.1.1 keycloak-operator.v11.0.0
```

## Useful Links
```bash
http://hostname/metrics
http://hostname/swagger-ui/
http://hostname/health
http://hostname/openapi
http://hostname/health-ui/
```

# Deployment
Wach is deployed to a kubernetes cluster (version>=1.20) using `helm`. 
For testing, a local cluster can be created using `kind`. 
Instructions are also provided on using Google Cloud Platform(GCP) autopilot cluster.
Any other kubernetes cluster should work as well. Ingress is required. Instructions for 
Ingress with kind are provided. In GCP Ingress works out of the box.

All commands are run in the `deployment` directory

## Requirements
[Install kubectl v 1.21](https://kubernetes.io/docs/tasks/tools/) \
[Install helm v 3](https://helm.sh/docs/intro/install/)

## Creating a local kind cluster
### Additional requirements
[Install kind v 0.11.0](https://kind.sigs.k8s.io/docs/user/quick-start/#installation)

### Creating a cluster
A kind cluster is useful for local testing.
1. Create a cluster by running:\
`kind create cluster --name wach --config kind-cluster-config.yaml`
1. Install ingress. At least Ingress NGINX works. Here's how to install as explained in kind tutorials:
```shell
kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/main/deploy/static/provider/kind/deploy.yaml
kubectl wait --namespace ingress-nginx \
  --for=condition=ready pod \
  --selector=app.kubernetes.io/component=controller \
  --timeout=90s
```
   
### Installing wach
1. Build an image:\
`../gradlew build -p ..`
1. Load the image into cluster:\
`kind load docker-image wach:dev --name wach`
1. Install wach charts to cluster by running:\
`helm install wach wach -f dev-values.yaml`
1. Access at the server [http://localhost](http://localhost)
 
### Useful commands
After using another cluster, re-configure kubectl to use the kind cluster:\
`kind export kubeconfig --name wach`

### Cleanup
Remove cluster by running:\
`kind delete clusters wach`

## Using Google Cloud Platform(GCP) kubernetes cluster
### Additional requirements
1. Create a Google Cloud Account at [https://console.cloud.google.com/](https://console.cloud.google.com/).
Enable billing for the account.
1. [Install Google Cloud SDK](https://cloud.google.com/sdk/docs/install)
1. Login. Complete the login in the browser window that opens:\
`gloud auth login`
1. Set the desired region:\
`gcloud config set compute/region europe-north1`
1. Create a project:\
`gcloud projects create my-wach`
1. Configure the SDK to use the new project:\
`gcloud config set project my-wach`
1. Get your billing account id. Copy the account id you want to use from output:\
`gcloud beta billing accounts list`
1. Link the new project to your billing account:\
`gcloud beta billing projects link my-wach --billing-account=BILLING-ACCOUNT-ID`
1. Enable required APIs. Compute is required for a static IP and container for a cluster:\
`gcloud services enable compute.googleapis.com container.googleapis.com`

### Creating a cluster
1. Reserve a static IP by running:\
`gcloud compute addresses create wach-static-ip --global`
1. Create a cluster using rapid release channel to get 1.20. Run:\
`gcloud container clusters create-auto wach --cluster-version=1.21 --release-channel=rapid`
1. Install wach:\
`helm install wach wach -f gcp-values.yaml`
1. Get IP from output. It'll take a while for the Ingress magic to happen:\
`kubectl get ingress wach`
1. If you get errors, try waiting up to ten minutes. The errors may change during this time.

### Useful commands
Re-configure kubectl to use the cluster:\
`gcloud container clusters get-credentials wach`

## Using your own image in a non-local cluster
1. Create a docker hub registry at https://hub.docker.com/
1. Create an access token at https://hub.docker.com/settings/security
1. Create a registry at https://hub.docker.com/repository/create
1. Build and publish an image. Instructions in [README.md](../README.md#building)
1. Install wach\
a). If using public repository: \
   Copy public-repository.yaml under `local` and populate username and repository.\
   Run:\
   `helm install wach wach --set image.tag=TAG -f local/public-repository.yaml -f gcp-values.yaml` \
b) If using a private repository:\
    Copy private-repository.yaml under `local` and populate username and repository.\
   Run:
   ```shell
   kubectl create secret docker-registry regcred --docker-server=https://index.docker.io/v2/ --docker-username=DOCKERHUB_USERNAME --docker-password=DOCKERHUB_ACCESS_TOKEN --docker-email=DOCKERHUB_EMAIL
   helm install wach wach --set image.tag=TAG -f gcp-values.yaml -f local/private-repository.yaml
   ```

### Cleaning up
Deleting the project will remove everything created in this manual:\
`gcloud projects delete wach`

One free cluster is free in GCP. By removing the static IP and the deployment the cluster should be free, but stay available. Run:\
```
helm uninstall wach
gcloud compute addresses delete wach-static-ip --global --quiet
```
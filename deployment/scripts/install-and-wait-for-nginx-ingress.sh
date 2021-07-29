#!/usr/bin/env bash

set -euo pipefail

kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/main/deploy/static/provider/kind/deploy.yaml
ITEMS="[]"

while [ "$ITEMS" == "[]" ]
do
  ITEMS=$(kubectl get pods --namespace=ingress-nginx --selector=app.kubernetes.io/component=controller -o jsonpath="{.items}")
done

kubectl wait --namespace ingress-nginx --for=condition=ready pod --selector=app.kubernetes.io/component=controller --timeout=90s

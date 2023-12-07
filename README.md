# idpay-merchant

## How test helm template

```sh
 helm dep build && helm template . -f values-dev.yaml  --debug
```

## How deploy helm

```sh
helm dep build && helm upgrade --namespace idpay --install --values values-dev.yaml --wait --timeout 5m0s idpay-merchant .
```
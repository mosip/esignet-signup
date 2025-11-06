# Esignet-Signup-With-Plugins Helm Installation

This repository provides installation scripts to deploy the **Signup-With-Plugins** Helm chart for MOSIP environments.  
Depending on your setup, you can install either of the two available plugin configurations:

- **esignet-mock-plugin.jar** (for mock or testing environments)
- **mosip-identity-plugin.jar** (for production or real identity integrations)


## Option-1 Install Signup with mock plugin 

```cd deploy/signup-with-plugins/signup-with-mock-plugin
```
-- Modify the values.yaml file if necessary to reflect your configuration requirements.

```./install.sh
```

## Option-2 Install Signup with mosipid plugin

```cd deploy/signup-with-plugins/signup-with-mosipid-plugin
```
-- Modify the values.yaml file if necessary to reflect your configuration requirements.

```./install.sh
```

## When necessary, run the delete.sh and restart.sh scripts to safely delete and restart the service.



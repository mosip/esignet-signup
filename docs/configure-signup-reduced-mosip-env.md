# esignet-signup

signup-service is part of the esignet modules, This module is used to register endusers into any integrated registry.

Below diagram depicts the high level deployment architecture for signup service with MOSIP ID-repo(registry).

![](docs/signup-with-mosip-id-repo.png)


**Note:**

For version < 1.1.0, supports only MOSIP ID-repo with default [ID schema](docs/id-schema.json) only.

For version > 1.1.0, supports integrating with any ID registry.


### Configurations

With respect to the default ID schema, below MOSIP configurations are required to be updated.

#### admin-default.properties
``
mosip.admin.masterdata.lang-code=eng,khm
``

#### application-default.properties
```
mosip.mandatory-languages=eng,khm

mosip.optional-languages=

mosip.default.template-languages=eng,khm
```

#### id-authentication-default.properties
```
request.idtypes.allowed=UIN,HANDLE

request.idtypes.allowed.internalauth=UIN

ida.mosip.external.auth.filter.classes.in.execution.order=io.mosip.authentication.hotlistfilter.impl.PartnerIdHotlistFilterImpl,io.mosip.authentication.hotlistfilter.impl.IndividualIdHotlistFilterImpl,io.mosip.authentication.hotlistfilter.impl.DeviceProviderHotlistFilterImpl,io.mosip.authentication.hotlistfilter.impl.DeviceHotlistFilterImpl,io.mosip.authentication.authtypelockfilter.impl.AuthTypeLockFilterImpl

mosip.ida.handle-types.regex={ '@phone' : '^\\+91[1-9][0-9]{7,9}@phone$' }
```

####  id-repository-default.properties
```
mosip.idrepo.credential.request.enable-convention-based-id=true

mosip.idrepo.identity.disable-uin-based-credential-request=true

mosip.idrepo.vid.disable-support=true

mosip.identity.fieldid.handle-postfix.mapping={'phone':'@phone'}
```

#### kernel-default.properties
``
mosip.kernel.sms.country.code=+91
``

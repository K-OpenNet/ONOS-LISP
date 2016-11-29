# ONOS-LISP-Management-Plane

This tool is developed to configure OOR (Open Overay Router, a dataplane implementation of LISP) through NetConf/Yang. 
Ths tool is working as an application of ONOS SDN controller. 

For more information for ONOS, please see the ONOS wiki. 
> https://wiki.onosproject.org/display/ONOS/Wiki+Home

The implementation of LISP mapping system is already contained on ONOS source tree. 
You can find out LISP mapping system implementation at org.onosproject.onos-lisp package.
> https://github.com/opennetworkinglab/onos/tree/master/protocols/lisp

#Functions
- [x] Connect to OOR device using NetConf/Yang
- [x] Add/remove map resolver on ITR
- [x] Add/remove local database(EID) on ETR
- [x] REST API support
- [ ] REST API JSON support to configure multiple locators 
- [ ] IPv6 support

#Usage through ONOS CLI
All commands are excuted on ONOS CLI. 

* **Connect to OOR device through NetConf**

  ```onos> lisp-connect {username} {password} {address} {port}```

  Example) ```lisp-connect foo bar 192.168.10.1 830```
  
* **Get map configured map resolvers of a device**

  ```onos> lisp-get-map-resolver {deviceId}```
  
  Exmaple) ```lisp-get-map-resolver netconf:192.168.10.1:830```
  
* **Add Map resover to the device**

  ```onos> lisp-add-map-resolver {deviceId} {resolver-address}```

  Note) deviceId can be referred using ONOS `devices` command. 

  Example) ```lisp-add-map-resolver netconf:192.168.10.1:830 10.10.10.10```

* **Remove Map resover to the device**

  ```onos> lisp-remove-map-resolver {deviceId} {resolver-address}```

  Example) ```lisp-remove-map-resolver netconf:192.168.10.1:830 10.10.10.10```

* **Add local Eid database**

  ```onos> lisp-add-local-eid {deviceId} {EID} {netmask} {RLOC} {Priority} {Weight}```
  
  Example) ```lisp-add-local-eid netconf:192.168.10.1:830 1.1.1.1 32 192.168.10.1 1 94```
  
* **Remove local Eid database**

  ```onos> lisp-remove-local-eid {deviceId} {EID} {netmask} {RLOC} {Priority} {Weight}```
  
  Example) ```lisp-remove-local-eid netconf:192.168.10.1:830 1.1.1.1 32 192.168.10.1 1 94```
  
* **Get configured local db of a device**

  ```onos> lisp-get-local-db {deviceId}```
  
  Exmaple) ```lisp-get-local-db" netconf:192.168.10.1:830```
  
#Usage through ONOS REST APIs
  The default web path for rest apis is "/onos/lispconfig".
  If you are running ONOS in localhost, then, the APIs can called throguh "127.0.0.1:8181/onos/lispconfig/".
  
  To test the app is sucessfully installed, you can try with "127.0.0.1:8181/onos/lispconfig/hello".
  You will see the following result. 
  
    {     
        "AppName": "Lispconfig",    
         "Version": "1.0.0",    
         "Description": "This tool is developed to configure OOR (Open Overay Router, a dataplane implementation of LISP) through NetConf/Yang"    
    }    

* **Get list of connected OOR devices**

    GET ``` onos/lispconfig/devices ```
    
    exmaple) ``` 127.0.0.1:8181/onos/lispconfig/devices ```
   
* **Connect to OOR device through NetConf**

    POST ``` onos/lispconfig/devices?username={id}&password={password}&address={address}&port={port} ```
    
    exmaple) ```127.0.0.1:8181/onos/lispconfig/devices?username=foo&password=bar&address=192.168.10.1&port=830```

* **Get added map resover from the device**

    GET ``` onos/lispconfig/{deviceId}/map-resolver ```
    
    exmaple) ```127.0.0.1:8181/onos/lispconfig/netconf:192.168.56.10:830/map-resolver

* **Add Map resover to the device**

    POST ``` onos/lispconfig/{deviceId}/map-resolver?address={address} ```
    
    exmaple) ```127.0.0.1:8181/onos/lispconfig/netconf:192.168.56.10:830/map-resolver?address=10.10.10.10
    
* **Remove Map resover to the device**

    DELETE ``` onos/lispconfig/{deviceId}/map-resolver?address={address} ```
    
    exmaple) ```127.0.0.1:8181/onos/lispconfig/netconf:192.168.56.10:830/map-resolver?address=10.10.10.10

* **Get configured local db of a device**

    GET ``` onos/lispconfig/{deviceId}/local-db ```
    
    exmaple) ```127.0.0.1:8181/onos/lispconfig/netconf:192.168.56.10:830/local-db ```
    
* **Add local db of a device**

    POST ``` onos/lispconfig/{deviceId}/local-db?eid={eid}&eid_mask={eid_mask}&rloc={rloc}&priority={prioriry}&weight={weight}```
    
    exmpale) ```127.0.0.1:8181/onos/lispconfig/netconf:192.168.56.10:830/local-db?eid=1.1.1.1&eid_maks=32&rloc=192.168.56.10&priority=1&weight=100 ```
    
* **Remove local db of a device**

    DELETE ``` onos/lispconfig/{deviceId}/local-db?eid={eid}&eid_mask={eid_mask}&rloc={rloc}&priority={prioriry}&weight={weight}```
    
    exmpale) ```127.0.0.1:8181/onos/lispconfig/netconf:192.168.56.10:830/local-db?eid=1.1.1.1&eid_maks=32&rloc=192.168.56.10&priority=1&weight=100 ```

# ONOS-LISP

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
- [ ] IPv6 support

#Usage through ONOS CLI
All commands are excuted on ONOS CLI. 

* **Connect to OOR device through NetConf**

  ```onos> lisp-connect {username} {password} {address} {port}```

  Example) ```lisp-connect foo bar 192.168.10.1 830```
  
* **Add Map resover to the device**

  ```onos> lisp-add-map-resolver {deviceId} {resolver-address}```

  Note) deviceId can be referred using ONOS `devices` command. 

  Example) ```lisp-add-map-resolver netconf:192.168.10.1:830 1.1.1.1```

* **Remove Map resover to the device**

  ```onos> lisp-remove-map-resolver {deviceId} {resolver-address}```

  Example) ```lisp-remove-map-resolver netconf:192.168.10.1:830 1.1.1.1```

* **Get map configured map resolvers of a device**

  ```onos> lisp-get-map-resolver {deviceId}```
  
  Exmaple) ```lisp-get-map-resolver netconf:192.168.10.1:830```
  
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


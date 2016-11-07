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
- [ ] IPv6 support
- [ ] REST API support

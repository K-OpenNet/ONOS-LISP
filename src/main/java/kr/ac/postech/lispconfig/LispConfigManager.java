/*
 * Copyright 2016-present Open Networking Laboratory
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package kr.ac.postech.lispconfig;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.Service;
import org.onlab.packet.IpAddress;
import org.onosproject.core.ApplicationId;
import org.onosproject.core.CoreService;
import org.onosproject.lisp.msg.protocols.DefaultLispMapRecord;
import org.onosproject.lisp.msg.protocols.LispLocator;
import org.onosproject.lisp.msg.protocols.LispMapRecord;
import org.onosproject.net.DeviceId;
import org.onosproject.net.config.NetworkConfigService;
import org.onosproject.net.driver.DriverHandler;
import org.onosproject.net.driver.DriverService;
import org.onosproject.netconf.NetconfController;
import org.onosproject.netconf.NetconfException;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * NetConf/Yang base LISP data plane configuration tool.
 * The all methods are base on lispsimple.yang model file.
 *
 * See {https://github.com/OpenOverlayRouter/oor/blob/master/netconf/lispsimple.yang}
 */

@Component(immediate = true)
@Service
public class LispConfigManager implements LispConfigService {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected NetworkConfigService cfgService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected CoreService coreService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected DriverService driverService;

    ComponentContext context;

    Map<DeviceId, List<String>> mapResolverMap;
    Map<DeviceId, List<LispMapRecord>> eidDbMap;

    private final static String ITR_HEADER = "<itr-cfg xmlns=\"urn:ietf:params:" +
            "xml:ns:yang:lispsimple\">\n<map-resolvers>\n<map-resolver>";
    private final static String ITR_FOOTER = "</map-resolver>\n</map-resolvers>\n</itr-cfg>\n";
    private final static String RESOLVER_BEGIN_TAG = "<map-resolver-address>";
    private final static String RESOLVER_END_TAG = "</map-resolver-address>\n";

    private final static String ETR_HEADER = "<etr-cfg xmlns=\"urn:ietf:params:" +
            "xml:ns:yang:lispsimple\">";
    private final static String ETR_FOOTER = "</etr-cfg>\n";
    private final static String LOCAL_EIDS_HEADER = "<local-eids>";
    private final static String LOCAL_EIDS_FOOTER = "</local-eids>";

    @Activate
    protected void activate(ComponentContext context) {
        this.context = context;

        mapResolverMap = Maps.newConcurrentMap();
        eidDbMap = Maps.newConcurrentMap();
        log.info("Started");
    }

    @Deactivate
    protected void deactivate() {
        log.info("Stopped");
    }

    @Override
    public String getConfig(DeviceId deviceId) {
        DriverHandler handler = driverService.createHandler(deviceId);
        NetconfController controller = handler.get(NetconfController.class);

        try {
            return controller.getNetconfDevice(deviceId).getSession().getConfig("running");
        } catch (NetconfException e) {
            e.printStackTrace();
        }

        return "Error to obtain GET_CONFIG";
    }

    @Override
    public String getConfigWithFilter(DeviceId deviceId, String filter) {
        DriverHandler handler = driverService.createHandler(deviceId);
        NetconfController controller = handler.get(NetconfController.class);

        try {
            return controller.getNetconfDevice(deviceId).getSession()
                    .getConfig("running", filter);
        } catch (NetconfException e) {
            e.printStackTrace();
        }

        return "Error to obtain GET_CONFIG for ITR";
    }

    @Override
    public boolean addItrMapResolver(DeviceId deviceId, String address) {
        List<String> resolverList = mapResolverMap.get(deviceId);

        if (resolverList == null) {
            resolverList = Lists.newLinkedList();
            mapResolverMap.put(deviceId, resolverList);
        }

        if (resolverList.stream().filter(s -> s.equals(address)).count() == 0 ) {
            resolverList.add(address);
            return updateItrMapResolver(deviceId);
        } else {
            log.info("Map resolver {} is already exist", address);
        }

        return false;
    }

    @Override
    public boolean removeItrMapResolver(DeviceId deviceId, String address) {
        List<String> resolverList = mapResolverMap.get(deviceId);

        if (resolverList != null) {
            resolverList.remove(address);
            return updateItrMapResolver(deviceId);
        } else {
            log.info("Map resolver {} is not exist on {}", address, deviceId);
        }

        return false;
    }

    @Override
    public boolean addEtrEidDataBase(DeviceId deviceId, LispMapRecord record) {
        log.info(record.toString());
        List<LispMapRecord> eidDb = eidDbMap.get(deviceId);

        if (eidDb == null) {
            eidDb = Lists.newLinkedList();
            eidDbMap.put(deviceId, eidDb);
        }

        log.info("Trace Point 1");
        List<LispMapRecord> records = eidDb.stream()
                .filter(s -> s.getEidPrefixAfi().equals(record.getEidPrefixAfi()))
                .collect(Collectors.toList());
        if (records.size() == 0) {
            log.info("Add new map record");
            eidDb.add(record);
        } else {
            log.info("Update new map record");
            LispMapRecord oldRecord = records.get(0);

            List<LispLocator> newLocators = record.getLocators();
            List<LispLocator> oldLocators = oldRecord.getLocators();

            List<LispLocator> actualLocators = Lists.newArrayList();
            actualLocators.addAll(newLocators);
            actualLocators.addAll(oldLocators);

            for (LispLocator loc : newLocators) {
                for (LispLocator ol : oldLocators) {
                    if(loc.getLocatorAfi().equals(ol.getLocatorAfi())){
                        actualLocators.remove(ol);
                    }
                }
            }

            DefaultLispMapRecord.DefaultMapRecordBuilder builder =
                    new DefaultLispMapRecord.DefaultMapRecordBuilder();

            builder.withEidPrefixAfi(record.getEidPrefixAfi());
            builder.withMaskLength(record.getMaskLength());
            builder.withRecordTtl(record.getRecordTtl());
            builder.withLocators(actualLocators);

            eidDb.remove(oldRecord);
            eidDb.add(builder.build());
        }

        return updateEtrEidDatabase(deviceId);
    }

    @Override
    public boolean removeEtrEidDataBase(DeviceId deviceId, LispMapRecord record) {
        List<LispMapRecord> eidDb = eidDbMap.get(deviceId);

        if (eidDb != null) {
            eidDb.remove(record);
            return updateEtrEidDatabase(deviceId);
        } else {
            log.info("EID-RLOC mapping record {} is not exist", record.toString());
        }

        return false;
    }

    @Override
    public boolean connectDevice(String name, String password,
                                String address, String port){

        //Setup netconf
        ObjectNode nodeNetconfData = new ObjectMapper().createObjectNode();
        nodeNetconfData.put("ip", address);
        nodeNetconfData.put("port", port);
        nodeNetconfData.put("username", name);
        nodeNetconfData.put("password", password);

        ObjectNode nodeNetconf = new ObjectMapper().createObjectNode();
        nodeNetconf.set("netconf", nodeNetconfData);

        ObjectNode nodeDriver = new ObjectMapper().createObjectNode();
        nodeDriver.put("driver", "netconf");

        ObjectNode nodeBasic = new ObjectMapper().createObjectNode();
        nodeBasic.set("basic", nodeDriver);

        ObjectNode nodeUnion = new ObjectMapper().createObjectNode();
        nodeUnion.setAll(nodeNetconf);
        nodeUnion.setAll(nodeBasic);

        ObjectNode nodeDevice = new ObjectMapper().createObjectNode();
        String newDeviceName = "netconf:"+address+":"+port;
        DeviceId deviceId = DeviceId.deviceId(newDeviceName);
        nodeDevice.set(newDeviceName, nodeUnion);

        ObjectNode root = new ObjectMapper().createObjectNode();
        root.set("devices", nodeDevice);

        Object resultRegister = cfgService.applyConfig("devices", deviceId,
                                        "basic",
                                        nodeBasic.get("basic"));
        Object resultConfigure = cfgService.applyConfig("devices", deviceId,
                               "netconf",
                               nodeNetconf.get("netconf"));

        if (resultRegister != null && resultConfigure != null) {
           return true;
        } else {
            return false;
        }
    }

    private boolean updateItrMapResolver(DeviceId deviceId){
        List<String> resolverList = mapResolverMap.get(deviceId);

        StringBuilder builder = new StringBuilder(ITR_HEADER);
        resolverList.forEach(r -> {
            builder.append(RESOLVER_BEGIN_TAG);
            if (IpAddress.valueOf(r).isIp4() ) {
                builder.append("<ipv4>");
                builder.append(r);
                builder.append("</ipv4>");
            } else {
                builder.append("<ipv6>");
                builder.append(r);
                builder.append("</ipv6>");
            }
            builder.append(RESOLVER_END_TAG);
        });
        builder.append(ITR_FOOTER);

        DriverHandler handler = driverService.createHandler(deviceId);
        NetconfController controller = handler.get(NetconfController.class);

        try {
            return controller.getNetconfDevice(deviceId).getSession()
                    .copyConfig("running", builder.toString());
        } catch (NetconfException e) {
            e.printStackTrace();
        }
       return false;
    }

    private boolean updateEtrEidDatabase(DeviceId deviceId){
        List<LispMapRecord> eidDb = eidDbMap.get(deviceId);


        StringBuilder builder = new StringBuilder(ETR_HEADER);
        builder.append(LOCAL_EIDS_HEADER);
        eidDb.forEach( record -> builder.append(serializeMapRecord(record)));
        builder.append(LOCAL_EIDS_FOOTER);
        builder.append(ETR_FOOTER);

        DriverHandler handler = driverService.createHandler(deviceId);
        NetconfController controller = handler.get(NetconfController.class);

        try {
            log.info(builder.toString());
            return controller.getNetconfDevice(deviceId).getSession()
                    .copyConfig("running", builder.toString());
        } catch (NetconfException e) {
            e.printStackTrace();
        }
        return false;
    }

    private  String serializeMapRecord(LispMapRecord mapRecord){
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append("<local-eid>");

        //Todo: now only support IPv4
        strBuilder.append("<eid-address>");
        if (mapRecord.getEidPrefixAfi().getAfi().getIanaCode() == 1) {
            strBuilder.append("<afi>").append("ipv4").append("</afi>");
            strBuilder.append("<ipv4>").append(mapRecord.getEidPrefixAfi().toString()).append("</ipv4>");
        }
        strBuilder.append("</eid-address>");


        strBuilder.append("<rlocs>");
        mapRecord.getLocators().forEach(
                l -> {
                    strBuilder.append("<rloc>");
                    if (l.getLocatorAfi().getAfi().getIanaCode() == 1){
                        strBuilder.append("<locator-address>");
                        strBuilder.append("<afi>").append("ipv4").append("</afi>");
                        strBuilder.append("<ipv4>").append(l.getLocatorAfi().toString()).append("</ipv4>");
                        strBuilder.append("</locator-address>");
                    }
                    strBuilder.append("<priority>").append(l.getPriority()).append("</priority>");
                    strBuilder.append("<weight>").append(l.getWeight()).append("</weight>");
                    strBuilder.append("</rloc>");
                }
        );
        strBuilder.append("</rlocs>");

        strBuilder.append("<record-ttl>").append(mapRecord.getRecordTtl()).append("</record-ttl>");

        strBuilder.append("</local-eid>");

        return strBuilder.toString();
    }

}

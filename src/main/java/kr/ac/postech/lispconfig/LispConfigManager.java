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
import org.onosproject.core.ApplicationId;
import org.onosproject.core.CoreService;
import org.onosproject.net.Device;
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

    private final static String APP_SUBJECT_CLASS_KEY = "apps";
    private final static String DEVICE_SUBJECT_CLASS_KEY= "devices";

    private final static String APP_CONFIG_KEY = "devices";
    private final static String DEVICE_CONFIG_KEY = "basic";

    private final static String DEVICE_CFG_JSON = "netconf-cfg.json";
    private final static String NETCONF_APP_NAME = "org.onosproject.netconf";

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected NetworkConfigService cfgService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected CoreService coreService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected DriverService driverService;

    ComponentContext context;

    Map<DeviceId, List<String>> mapResolverMap;

    private final static String ITR_HEADER = "<itr-cfg xmlns=\"urn:ietf:params:" +
            "xml:ns:yang:lispsimple\">\n\t<map-resolvers>\n";
    private final static String ITR_FOOTER = "\t</map-resolvers>\n</itr-cfg>\n";
    private final static String RESOLVER_BEGIN_TAG = "\t\t<map-resolver-address>";
    private final static String RESOLVER_END_TAG = "\t\t</map-resolver-address>";


    @Activate
    protected void activate(ComponentContext context) {
        this.context = context;

        mapResolverMap = Maps.newConcurrentMap();
        log.info("Started");
    }

    @Deactivate
    protected void deactivate() {
        log.info("Stopped");
    }

    @Override
    public String getConfig(DeviceId deviceId, String target) {
        DriverHandler handler = driverService.createHandler(deviceId);
        NetconfController controller = handler.get(NetconfController.class);

        try {
            return controller.getNetconfDevice(deviceId).getSession().getConfig(target);
        } catch (NetconfException e) {
            e.printStackTrace();
        }

        return "Error to obtain GET_CONFIG";
    }

    @Override
    public boolean addItrMapResolver(DeviceId deviceId, String target, String address) {
        List<String> resolverList = mapResolverMap.get(deviceId);

        if (resolverList == null) {
            resolverList = Lists.newLinkedList();
            mapResolverMap.put(deviceId, resolverList);
        }

        StringBuilder builder = new StringBuilder(ITR_HEADER);
        resolverList.forEach(r -> {
            builder.append(RESOLVER_BEGIN_TAG);
            builder.append(r);
            builder.append(RESOLVER_END_TAG);
        });
        builder.append(ITR_FOOTER);

        DriverHandler handler = driverService.createHandler(deviceId);
        NetconfController controller = handler.get(NetconfController.class);

        try {
            return controller.getNetconfDevice(deviceId).getSession()
                    .copyConfig(target, builder.toString());
        } catch (NetconfException e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public boolean removeItrMapResolver(DeviceId deviceId, String target, String address) {
        return false;
    }

    @Override
    public boolean connectDevice(String name, String password,
                                String address, String port){
        boolean registered = registerDevice(address, port);
        boolean configured = configureDevice(name, password, address, port);

        if(registered && configured){
            return true;
        } else {
            return false;
        }
    }

    public boolean registerDevice(String address, String port){
        log.info("{} {}", address, port);
        InputStream is = null;
        try {
            is = context.getBundleContext().getBundle()
                    .getEntry(DEVICE_CFG_JSON).openStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        ObjectMapper mapper = new ObjectMapper();
        JsonFactory jsonFactory = mapper.getFactory();
        JsonParser jsonParser = null;
        try {
            jsonParser = jsonFactory.createParser(is);
        } catch (IOException e) {
            e.printStackTrace();
        }

        JsonNode jsonNode = null;
        try {
            jsonNode = mapper.readTree(jsonParser);
        } catch (IOException e) {
            e.printStackTrace();
        }

        JsonNode deviceRoot = jsonNode.get(DEVICE_SUBJECT_CLASS_KEY);

        String deviceName = deviceRoot.fieldNames().next();
        JsonNode removed = ((ObjectNode) deviceRoot).remove(deviceName);

        String newDeviceName = "netconf:"+address+":"+port;
        ((ObjectNode) deviceRoot).set(newDeviceName, removed);

        DeviceId deviceId = DeviceId.deviceId(newDeviceName);
        JsonNode subjectNode = deviceRoot.path(newDeviceName);

        Object result = cfgService.applyConfig(DEVICE_SUBJECT_CLASS_KEY, deviceId,
                               DEVICE_CONFIG_KEY,
                               subjectNode.get(DEVICE_CONFIG_KEY));
        return result == null ? true : false;
    }

    public boolean configureDevice(String name, String password,
                                String address, String port){
        ApplicationId netConfAppId = coreService.getAppId(NETCONF_APP_NAME);

        InputStream is = null;
        try {
            is = context.getBundleContext().getBundle()
                    .getEntry(DEVICE_CFG_JSON).openStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        ObjectMapper mapper = new ObjectMapper();
        JsonFactory jsonFactory = mapper.getFactory();
        JsonParser jsonParser = null;
        try {
            jsonParser = jsonFactory.createParser(is);
        } catch (IOException e) {
            e.printStackTrace();
        }

        JsonNode jsonNode = null;
        try {
            jsonNode = mapper.readTree(jsonParser);
        } catch (IOException e) {
            e.printStackTrace();
        }

        JsonNode appRoot = jsonNode.get(APP_SUBJECT_CLASS_KEY);
        appRoot = appRoot.get(NETCONF_APP_NAME);

        jsonNode = appRoot.get(APP_CONFIG_KEY).get(0);

        ((ObjectNode) jsonNode).put("name", name);
        ((ObjectNode) jsonNode).put("password", password);
        ((ObjectNode) jsonNode).put("ip", address);
        ((ObjectNode) jsonNode).put("port", port);

        log.info(appRoot.toString());

        Object result = cfgService.applyConfig(APP_SUBJECT_CLASS_KEY, netConfAppId,
                               APP_CONFIG_KEY, appRoot.path(APP_CONFIG_KEY));

        return result == null ? true : false;
    }




}

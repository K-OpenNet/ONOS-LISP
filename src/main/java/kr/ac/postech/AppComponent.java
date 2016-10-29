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
package kr.ac.postech;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.onosproject.core.ApplicationId;
import org.onosproject.core.CoreService;
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

/**
 * Skeletal ONOS application component.
 */
@Component(immediate = true)
public class AppComponent {

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

    NetconfController controller;

    ComponentContext context;

    @Activate
    protected void activate(ComponentContext context) {
        this.context = context;

        connectDevice("lisp", "lisp", "192.168.56.10", "830");

        log.info("Started");
    }

    public String getConfig(DeviceId deviceId, String type) {
        DriverHandler handler = driverService.createHandler(deviceId);
        NetconfController controller = handler.get(NetconfController.class);

        try {
            return controller.getNetconfDevice(deviceId).getSession().getConfig(type);
        } catch (NetconfException e) {
            e.printStackTrace();
        }

        return "Error to obtain GET_CONFIG";
    }



    public void connectDevice(String name, String password,
                                String address, String port){
        registerDevice(address, port);
        configureDevice(name, password, address, port);
    }

    public void registerDevice(String address, String port){
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

        cfgService.applyConfig(DEVICE_SUBJECT_CLASS_KEY, deviceId,
                               DEVICE_CONFIG_KEY,
                               subjectNode.get(DEVICE_CONFIG_KEY));
    }

    public void configureDevice(String name, String password,
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

        cfgService.applyConfig(APP_SUBJECT_CLASS_KEY, netConfAppId,
                               APP_CONFIG_KEY, appRoot.path(APP_CONFIG_KEY));
    }

    @Deactivate
    protected void deactivate() {
        log.info("Stopped");
    }

}

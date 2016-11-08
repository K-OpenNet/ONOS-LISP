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

package kr.ac.postech.lispconfig.rest;

import com.fasterxml.jackson.databind.node.ObjectNode;
import kr.ac.postech.lispconfig.LispConfigService;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.onosproject.net.DeviceId;
import org.onosproject.rest.AbstractWebResource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Set;


/**
 * Sample web resource.
 */
@Path("")
public class AppWebResource extends AbstractWebResource {

    static final String ITR_CFG = "<itr-cfg/>";

    /**
     * Get hello world greeting.
     *
     * @return 200 OK
     */
    @GET
    @Path("hello")
    public Response getGreeting() {
        ObjectNode node = mapper().createObjectNode().put("AppName", "Lispconfig");
        node.put("Version", "1.0.0");
        node.put("Description", "This tool is developed to configure OOR " +
                "(Open Overay Router, a dataplane implementation of LISP) " +
                "through NetConf/Yang");

        return ok(node).build();
    }

    @GET
    @Path("/map-resolver/{deviceId}")
    public Response getMapResolver(@PathParam("deviceId") String deviceId) {

        LispConfigService service = get(LispConfigService.class);
        DeviceId devId = DeviceId.deviceId(deviceId);
        String result = service.getConfigWithFilter(devId, ITR_CFG);

        return ok(result).build();
    }

}

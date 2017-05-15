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

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ImmutableList;
import kr.ac.postech.lispconfig.LispConfigService;
import org.onlab.packet.IpAddress;
import org.onosproject.lisp.msg.protocols.DefaultLispLocator;
import org.onosproject.lisp.msg.protocols.DefaultLispMapRecord;
import org.onosproject.lisp.msg.types.LispIpv4Address;
import org.onosproject.net.DeviceId;
import org.onosproject.net.device.DeviceService;
import org.onosproject.rest.AbstractWebResource;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.InputStream;


/**
 * Sample web resource.
 */
@Path("")
public class AppWebResource extends AbstractWebResource {

    static final String ITR_CFG = "<itr-cfg/>";
    static final String ETR_CFG = "<etr-cfg/>";

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

    @POST
    @Path("/devices")
    public Response connect(@QueryParam("username") String username,
                            @QueryParam("password") String password,
                            @QueryParam("address") String address,
                            @QueryParam("port") String port
    ) {

        LispConfigService service = get(LispConfigService.class);
        boolean result = service.connectDevice(username, password, address, port);

        return ok(result).build();
    }

    @GET
    @Path("/devices")
    public Response getDevices() {
        DeviceService deviceService = get(DeviceService.class);
        ObjectNode node = mapper().createObjectNode();
        ArrayNode arrayNode = node.putArray("devices");

        deviceService.getDevices().forEach(
                d -> arrayNode.add(mapper().createObjectNode()
                                           .put("deviceId", d.id().toString()))
        );

        return ok(node).build();
    }

    @GET
    @Path("{deviceId}/map-resolver")
    @Produces(MediaType.APPLICATION_XML)
    public Response getMapResolvers(@PathParam("deviceId") String deviceId) {

        LispConfigService service = get(LispConfigService.class);
        DeviceId devId = DeviceId.deviceId(deviceId);
        String result = service.getConfigWithFilter(devId, ITR_CFG);

        return ok(result).build();
    }

    @POST
    @Path("{deviceId}/map-resolver")
    public Response addMapResolvers(@PathParam("deviceId") String deviceId,
    @QueryParam("address") String address) {

        LispConfigService service = get(LispConfigService.class);
        DeviceId devId = DeviceId.deviceId(deviceId);
        boolean result = service.addItrMapResolver(devId, address);

        return ok(result).build();
    }

    @DELETE
    @Path("{deviceId}/map-resolver")
    public Response removeMapResolvers(@PathParam("deviceId") String deviceId,
                                    @QueryParam("address") String address) {

        LispConfigService service = get(LispConfigService.class);
        DeviceId devId = DeviceId.deviceId(deviceId);
        boolean result = service.removeItrMapResolver(devId, address);

        return ok(result).build();
    }

    @GET
    @Path("{deviceId}/local-db")
    @Produces(MediaType.APPLICATION_XML)
    public Response getLocalDb(@PathParam("deviceId") String deviceId) {
        LispConfigService service = get(LispConfigService.class);
        DeviceId devId = DeviceId.deviceId(deviceId);
        String result = service.getConfigWithFilter(devId, ETR_CFG);

        return ok(result).build();
    }

    @POST
    @Path("{deviceId}/local-db")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addLocalDb(@PathParam("deviceId") String deviceId,
                               @QueryParam("eid") String eid,
                               @QueryParam("eid_mask") Byte eid_mask,
                               @QueryParam("rloc") String rloc,
                               @QueryParam("priority") byte priority,
                               @QueryParam("weight") byte weight,
                               @QueryParam("ttl") int ttl,
                               InputStream inputStream) {
        if(eid != null) {
            LispConfigService service = get(LispConfigService.class);
            DeviceId devId = DeviceId.deviceId(deviceId);

            DefaultLispMapRecord.DefaultMapRecordBuilder builder =
                    new DefaultLispMapRecord.DefaultMapRecordBuilder();

            builder.withEidPrefixAfi(new LispIpv4Address(IpAddress.valueOf(eid)));
            builder.withMaskLength(eid_mask);
            builder.withRecordTtl(ttl);

            DefaultLispLocator.DefaultLocatorBuilder locatorRecordBuilder
                    = new DefaultLispLocator.DefaultLocatorBuilder();

            locatorRecordBuilder.withLocatorAfi(new LispIpv4Address(IpAddress.valueOf(rloc)));
            locatorRecordBuilder.withPriority(priority);
            locatorRecordBuilder.withWeight(weight);

            builder.withLocators(ImmutableList.of(locatorRecordBuilder.build()));

            Boolean result = service.addEtrEidDataBase(devId, builder.build());

            return ok(result).build();
        } else {
            //TODO: JSON support
        }
        return ok(false).build();
    }

    @DELETE
    @Path("{deviceId}/local-db")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response removeLocalDb(@PathParam("deviceId") String deviceId,
                               @QueryParam("eid") String eid,
                               @QueryParam("eid_mask") Byte eid_mask,
                               @QueryParam("rloc") String rloc,
                               @QueryParam("priority") byte priority,
                               @QueryParam("weight") byte weight,
                               @QueryParam("ttl") int ttl,
                               InputStream inputStream) {
        if(eid != null) {
            LispConfigService service = get(LispConfigService.class);
            DeviceId devId = DeviceId.deviceId(deviceId);

            DefaultLispMapRecord.DefaultMapRecordBuilder builder =
                    new DefaultLispMapRecord.DefaultMapRecordBuilder();

            builder.withEidPrefixAfi(new LispIpv4Address(IpAddress.valueOf(eid)));
            builder.withMaskLength(eid_mask);

            DefaultLispLocator.DefaultLocatorBuilder locatorRecordBuilder
                    = new DefaultLispLocator.DefaultLocatorBuilder();

            locatorRecordBuilder.withLocatorAfi(new LispIpv4Address(IpAddress.valueOf(rloc)));
            locatorRecordBuilder.withPriority(priority);
            locatorRecordBuilder.withWeight(weight);
            builder.withRecordTtl(ttl);

            builder.withLocators(ImmutableList.of(locatorRecordBuilder.build()));

            Boolean result = service.removeEtrEidDataBase(devId, builder.build());

            return ok(result).build();
        } else {
            //TODO: JSON support
        }
        return ok(false).build();
    }

}

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

package kr.ac.postech.lispconfig.cli;

import com.google.common.collect.ImmutableList;
import kr.ac.postech.lispconfig.LispConfigService;
import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.onlab.packet.IpAddress;
import org.onosproject.cli.AbstractShellCommand;
import org.onosproject.lisp.msg.protocols.DefaultLispLocatorRecord;
import org.onosproject.lisp.msg.protocols.DefaultLispMapRecord;
import org.onosproject.lisp.msg.types.LispIpv4Address;
import org.onosproject.net.DeviceId;

import java.util.Collections;

/**
 * LISP configuration GET command
 */
@Command(scope = "onos", name = "lisp-add-local-eid",
         description = "add local eid-rloc database on the device")

public class LispAddLocalMapRecordCommand extends AbstractShellCommand {

    @Argument(index = 0, name = "deviceId", description = "deviceId to get " +
            "configuration information",
            required = true, multiValued = false)
    String deviceId = null;

    @Argument(index = 1, name = "eid", description = "EID address",
            required = true, multiValued = false)
    String eid = null;

    @Argument(index = 2, name = "eid_mask", description = "EID address mask",
            required = true, multiValued = false)
    Byte eid_mask = null;

    @Argument(index = 3, name = "rloc", description = "RLOC address",
            required = true, multiValued = false)
    String rloc = null;

    @Argument(index = 4, name = "priority", description = "Priority",
            required = true, multiValued = false)
    Byte priority = null;

    @Argument(index = 5, name = "weight", description = "Weight",
            required = true, multiValued = false)
    Byte weight = null;

    @Override
    protected void execute() {
        LispConfigService service = get(LispConfigService.class);
        DeviceId deviceId = DeviceId.deviceId(this.deviceId);

        DefaultLispMapRecord.DefaultMapRecordBuilder builder =
                new DefaultLispMapRecord.DefaultMapRecordBuilder();

        builder.withEidPrefixAfi(new LispIpv4Address(IpAddress.valueOf(eid)));
        builder.withMaskLength(eid_mask);

        DefaultLispLocatorRecord.DefaultLocatorRecordBuilder locatorRecordBuilder
                = new DefaultLispLocatorRecord.DefaultLocatorRecordBuilder();

        locatorRecordBuilder.withLocatorAfi(new LispIpv4Address(IpAddress.valueOf(rloc)));
        locatorRecordBuilder.withPriority(priority);
        locatorRecordBuilder.withWeight(weight);

        builder.withLocators(ImmutableList.of(locatorRecordBuilder.build()));

        service.addEtrEidDataBase(deviceId, builder.build());
    }
}

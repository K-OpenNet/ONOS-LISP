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

import kr.ac.postech.lispconfig.LispConfigService;
import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.onosproject.cli.AbstractShellCommand;
import org.onosproject.net.DeviceId;

/**
 * LISP configuration GET command
 */
@Command(scope = "onos", name = "lisp-remove-map-resolver",
         description = "connect to a requested LISP device'")

public class LispRemoveMapResolverCommand extends AbstractShellCommand {

    @Argument(index = 0, name = "deviceId", description = "deviceId to get " +
            "configuration information",
            required = true, multiValued = false)
    String deviceId = null;

    @Argument(index = 1, name = "address", description = "IP address",
            required = true, multiValued = false)
    String address = null;

    @Override
    protected void execute() {
        LispConfigService service = get(LispConfigService.class);
        DeviceId deviceId = DeviceId.deviceId(this.deviceId);
        boolean result = service.removeItrMapResolver(deviceId, address);
    }
}

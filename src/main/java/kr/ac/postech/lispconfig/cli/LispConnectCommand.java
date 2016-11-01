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

import kr.ac.postech.lispconfig.LispConfigManager;
import kr.ac.postech.lispconfig.LispConfigService;
import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.onosproject.cli.AbstractShellCommand;

/**
 * LISP configuration GET command
 */
@Command(scope = "onos", name = "lisp-connect",
         description = "connect to a requested LISP device'")

public class LispConnectCommand extends AbstractShellCommand {

    @Argument(index = 0, name = "username", description = "username",
            required = true, multiValued = false)
    String username = null;

    @Argument(index = 1, name = "password", description = "password",
            required = true, multiValued = false)
    String password = null;

    @Argument(index = 2, name = "password", description = "IP address",
            required = true, multiValued = false)
    String address = null;

    @Argument(index = 3, name = "password", description = "Port, default is 830",
            required = false, multiValued = false)
    String port = "830";

    @Override
    protected void execute() {
        LispConfigService service = get(LispConfigService.class);
        boolean result = service.connectDevice(username, password, address, port);

        if(result){
            print("The {} is successfully connected", address+":"+port);
        }
    }
}

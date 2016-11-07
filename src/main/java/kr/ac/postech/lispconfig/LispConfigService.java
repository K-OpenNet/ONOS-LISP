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

import org.onosproject.net.DeviceId;

public interface LispConfigService {

    /**
     * Connect device through NetConf.
     * To do, this should be improved in terms of security
     *
     * @param name username for NetConf
     * @param password password for NetConf
     * @param address IP address of the NetConf device
     * @param port Port
     * @return true when connection have established sucessfully
     */
    boolean connectDevice(String name, String password, String address,
                          String port);

    /**
     * NetConf Get-Config call
     *
     * @param deviceId the target device
     * @param target the scope of config such as running, startup, and candidate
     * @return the get-config result
     */
    String getConfig(DeviceId deviceId, String target);

    /**
     * NetConf Get-Config call
     *
     * @param deviceId the target device
     * @param target the scope of config such as running, startup, and candidate
     * @param filter the filter XML document
     * @return the get-config result
     */
    String getConfigWithFilter(DeviceId deviceId, String target, String filter);

    /**
     * Add Map resolver as an ITR configuration
     *
     * @param deviceId The target device
     * @param target the scope of config such as running, startup, and candidate
     * @param address The address of map resolver to add
     * @return true when a map resolver is added successfully
     */
    boolean addItrMapResolver(DeviceId deviceId, String target, String address);

    /**
     * Remove Map resolver as an ITR configuration
     *
     * @param deviceId The target device
     * @param target the scope of config such as running, startup, and candidate
     * @param address The address of map resolver to remove
     * @return true when a map resolver is removed successfully
     */
    boolean removeItrMapResolver(DeviceId deviceId, String target, String address);
}

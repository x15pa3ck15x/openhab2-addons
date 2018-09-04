/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.domintell.internal.protocol.model;

import org.openhab.binding.domintell.internal.protocol.ModuleAddress;
import org.openhab.binding.domintell.internal.protocol.DomintellConnection;
import org.openhab.binding.domintell.internal.protocol.ModuleType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Variable module
 *
 * @author Gabor Bicskei - Initial contribution
 */
public class VariableModule extends AbstractBaseModule {
    /**
     * Domintell logger. Uses a common category for all Domintell related logging.
     */
    private final Logger log = LoggerFactory.getLogger(VariableModule.class);

    private Integer value = 0;
    private ThingInfo thingInfo;

    public VariableModule(DomintellConnection connection, ModuleType moduleType, ModuleAddress address) {
        super(connection, moduleType, address);
    }

    @Override
    public void processUpdate(String info) {
        if (info != null) {
            if (info.contains("[")) {
                //processing APPINFO
                thingInfo = ThingInfo.parseThingInfo(info.trim());
            } else {
                //processing data message
                try {
                    // O00 or D00
                    value = Integer.parseInt(info.substring(1).trim(), 16);
                    if (getUpdateListener() != null) {
                        getUpdateListener().moduleStateUpdated(this, value);
                    }
                } catch (NumberFormatException e) {
                    log.warn("Invalid feedback received {}", info, e);
                }
            }
        }
        super.processUpdate(info);
    }

    @Override
    public void executeCommand(Command command, Integer input) {
    }

    public void toggle() {
        connection.sendCommand(moduleType.toString() + address);
    }
}

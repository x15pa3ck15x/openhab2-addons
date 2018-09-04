/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.domintell.internal.protocol.model;

import org.openhab.binding.domintell.internal.protocol.DomintellConnection;
import org.openhab.binding.domintell.internal.protocol.ModuleAddress;
import org.openhab.binding.domintell.internal.protocol.ModuleType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base class for multi IO modules
 *
 * @author Gabor Bicskei - Initial contribution
 */
public abstract class AbstractMultiIOModule extends AbstractBaseModule {
    /**
     * Domintell logger. Uses a common category for all Domintell related logging.
     */
    private final Logger logger = LoggerFactory.getLogger(AbstractMultiIOModule.class);

    private ThingInfo[] thingInfo = new ThingInfo[8];

    AbstractMultiIOModule(DomintellConnection connection, ModuleType moduleType, ModuleAddress address) {
        super(connection, moduleType, address);
    }

    // Feedback method from Domintell system ---------------------------------------------------------
    @Override
    public void processUpdate(String info) {
        if (info != null) {
            if (info.contains("[")) {
                //processing APPINFO
                int idx = Integer.parseInt(info.substring(1, 2));
                thingInfo[idx - 1] = ThingInfo.parseThingInfo(info.substring(2));
            } else {
                //processing data message
                try {
                    // I00
                    Integer value = Integer.parseInt(info.substring(1), 16);
                    if (getUpdateListener() != null) {
                        getUpdateListener().moduleStateUpdated(this, value);
                    }
                } catch (NumberFormatException e) {
                    logger.warn("Invalid feedback received {}", info, e);
                }
            }
        }
        super.processUpdate(info);
    }

    public ThingInfo getThingInfo(int idx) {
        return thingInfo[idx - 1];
    }
}

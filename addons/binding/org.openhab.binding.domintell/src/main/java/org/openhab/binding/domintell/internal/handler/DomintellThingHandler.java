/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.domintell.internal.handler;

import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.openhab.binding.domintell.internal.protocol.DomintellConnection;
import org.openhab.binding.domintell.internal.protocol.ModuleAddress;
import org.openhab.binding.domintell.internal.protocol.ModuleType;
import org.openhab.binding.domintell.internal.protocol.exception.ModuleException;
import org.openhab.binding.domintell.internal.protocol.model.AbstractBaseModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link DomintellThingHandler} is responsible for handling commands which are sent to one of the channels.
 *
 * @author Gabor Bicskei - Initial contribution
 */
abstract class DomintellThingHandler extends BaseThingHandler {
    /**
     * Domintell logger. Uses a common category for all Domintell related logging.
     */
    private final Logger logger = LoggerFactory.getLogger(DomintellBridgeHandler.class);

    /**
     * Domintell connection
     */
    private DomintellConnection connection;

    /**
     * Constructor.
     *
     * @param thing The thing behind the module.
     * @param connection Domintell connection
     */
    DomintellThingHandler(Thing thing, DomintellConnection connection) {
        super(thing);
        this.connection = connection;

        //set the handler as the state update listener
        ModuleType moduleType = ModuleType.valueOf(thing.getThingTypeUID().getId().toUpperCase());
        ModuleAddress address = new ModuleAddress(thing.getUID().getId());
        try {
            AbstractBaseModule domintellModule = connection.getDomintellModule(moduleType, address);
            if (domintellModule != null) {
                domintellModule.setUpdateListener(this::moduleStateUpdated);
            }
        } catch (ModuleException e) {
            logger.debug("Domintell module not found for thing: {}", moduleType);
        }
    }

    protected abstract void moduleStateUpdated(AbstractBaseModule module, Object states);

    /**
     * Request module status update.
     */
    void requestStatusUpdate() {
        //updating module status
        if (connection.isOnline()) {
            ModuleType mt = ModuleType.valueOf(thing.getThingTypeUID().getId().toUpperCase());
            ModuleAddress address = new ModuleAddress(thing.getUID().getId());
            String command = mt.toString() + address + "%S";
            logger.debug("Update module status: {}", address);
            connection.sendCommand(command);
        }
    }
}

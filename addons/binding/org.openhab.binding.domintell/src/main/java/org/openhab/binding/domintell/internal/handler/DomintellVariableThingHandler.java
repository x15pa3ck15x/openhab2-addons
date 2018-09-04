/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.domintell.internal.handler;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.domintell.internal.DomintellBindingConstants;
import org.openhab.binding.domintell.internal.protocol.DomintellConnection;
import org.openhab.binding.domintell.internal.protocol.model.AbstractBaseModule;
import org.openhab.binding.domintell.internal.protocol.model.VariableModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Variable module thing handler.
 *
 * @author Gabor Bicskei - Initial contribution
 */
public class DomintellVariableThingHandler extends DomintellThingHandler {
    /**
     * Domintell logger. Uses a common category for all Domintell related logging.
     */
    private Logger logger = LoggerFactory.getLogger(DomintellVariableThingHandler.class);

    /**
     * Parent module.
     */
    private VariableModule module;

    /**
     * Current module state.
     */
    private Integer state;

    /**
     * Constructor.
     *
     * @param thing Parent thing
     * @param connection Domintell connection
     */
    public DomintellVariableThingHandler(Thing thing, DomintellConnection connection) {
        super(thing, connection);
    }

    /**
     * Callback for module state updates.
     *
     * @param module Module.
     * @param value New value.
     */
    @Override
    public void moduleStateUpdated(AbstractBaseModule module, Object value) {
        this.module = (VariableModule) module;
        state = (Integer) value;
        refreshState();
        updateStatus(ThingStatus.ONLINE);
    }

    /**
     * Update thing channels based on the current state.
     */
    private void refreshState() {
        if (state != null) {
            updateState(DomintellBindingConstants.CHANNEL_NUM_VALUE, new DecimalType(state));
            updateState(DomintellBindingConstants.CHANNEL_BOOLEAN_VALUE, state == 0 ? OnOffType.OFF : OnOffType.ON);
        }
    }

    /**
     * Handle variable commands.
     *
     * @param channelUID Channel.
     * @param command Command to handle.
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            requestStatusUpdate();
        } else {
            if (module != null) {
                module.toggle();
            } else {
                logger.debug("Module needs an update before it can execute any command: {}", getThing().getUID());
            }
        }
    }
}

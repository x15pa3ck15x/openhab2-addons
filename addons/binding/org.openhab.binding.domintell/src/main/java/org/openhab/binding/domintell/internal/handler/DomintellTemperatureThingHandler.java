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
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.domintell.internal.DomintellBindingConstants;
import org.openhab.binding.domintell.internal.protocol.DomintellConnection;
import org.openhab.binding.domintell.internal.protocol.RegulationMode;
import org.openhab.binding.domintell.internal.protocol.model.AbstractBaseModule;
import org.openhab.binding.domintell.internal.protocol.model.TemperatureModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Temperature module thing handler.
 *
 * @author Gabor Bicskei - Initial contribution
 */
public class DomintellTemperatureThingHandler extends DomintellThingHandler {
    /**
     * Class logger
     */
    private Logger logger = LoggerFactory.getLogger(DomintellTemperatureThingHandler.class);

    /**
     * Parent module.
     */
    private TemperatureModule module;

    /**
     * Module state.
     */
    private String[] state = new String[]{"0.0", "0.0", "AUTO", "0.0"};

    /**
     * Constructor.
     *
     * @param thing Parent thing.
     * @param connection Domintell connection
     */
    public DomintellTemperatureThingHandler(Thing thing, DomintellConnection connection) {
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
        this.module = (TemperatureModule) module;
        state = (String[]) value;
        refreshState();
        updateStatus(ThingStatus.ONLINE);
    }

    /**
     * Update thing channels based on the current state.
     */
    private void refreshState() {
        ChannelUID channel = new ChannelUID(getThing().getUID(), DomintellBindingConstants.CHANNEL_GROUP_THERMOSTAT, DomintellBindingConstants.CHANNEL_CURRENT_VALUE);
        updateState(channel, new DecimalType(Double.parseDouble(state[0])));

        channel = new ChannelUID(getThing().getUID(), DomintellBindingConstants.CHANNEL_GROUP_THERMOSTAT, DomintellBindingConstants.CHANNEL_PRESET_VALUE);
        updateState(channel, new DecimalType(Double.parseDouble(state[1])));

        channel = new ChannelUID(getThing().getUID(), DomintellBindingConstants.CHANNEL_GROUP_THERMOSTAT, DomintellBindingConstants.CHANNEL_MODE);
        updateState(channel, new DecimalType(RegulationMode.valueOf(state[2]).getValue()));

        channel = new ChannelUID(getThing().getUID(), DomintellBindingConstants.CHANNEL_GROUP_THERMOSTAT, DomintellBindingConstants.CHANNEL_PROFILE_VALUE);
        updateState(channel, new DecimalType(Double.parseDouble(state[3])));
    }

    /**
     * Handle thermostat commands.
     *
     * @param channelUID Channel.
     * @param command Command to handle.
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (!(command instanceof RefreshType)) {
            if (module != null) {
                if (DomintellBindingConstants.CHANNEL_ID_THERMOSTAT_MODE.equals(channelUID.getId())) {
                    if (command instanceof DecimalType) {
                        module.setMode(RegulationMode.byValue(((DecimalType) command).intValue()));
                    } else if (command instanceof StringType) {
                        module.setMode(RegulationMode.valueOf(command.toString()));
                    }
                } else if (DomintellBindingConstants.CHANNEL_ID_THERMOSTAT_PRESET_VALUE.equals(channelUID.getId())) {
                    module.setSetPoint(((DecimalType) command).floatValue());
                } else {
                    logger.debug("Not supported command for channel: {} -> {}", channelUID.getId(), command);
                }
                updateStatus(ThingStatus.ONLINE);
            } else {
                logger.debug("Module needs an update before it can execute any command: {}", getThing().getUID());
            }
        } else {
            requestStatusUpdate();
        }
    }
}

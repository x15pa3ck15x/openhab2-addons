/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.domintell.internal.handler;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.domintell.internal.config.ThingConfig;
import org.openhab.binding.domintell.internal.protocol.DomintellConnection;
import org.openhab.binding.domintell.internal.protocol.ModuleType;
import org.openhab.binding.domintell.internal.protocol.model.AbstractBaseModule;
import org.openhab.binding.domintell.internal.protocol.model.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Thing handler for relay and input modules.
 *
 * @author Gabor Bicskei - Initial contribution
 */
public class DomintellIOThingHandler extends DomintellThingHandler {
    /**
     * Class logger
     */
    private final Logger logger = LoggerFactory.getLogger(DomintellIOThingHandler.class);

    /**
     * Parent module
     */
    private AbstractBaseModule module;

    /**
     * Module state
     */
    private Integer state = 0;

    /**
     * Thing configuration
     */
    private ThingConfig config;

    /**
     * Constructor.
     *
     * @param thing Parent thing
     * @param connection Domintell connection.
     */
    public DomintellIOThingHandler(Thing thing, DomintellConnection connection) {
        super(thing, connection);
        config = getConfigAs(ThingConfig.class);
    }

    @Override
    public void initialize() {
        requestStatusUpdate();
    }

    /**
     * Callback for module state updates.
     *
     * @param module Updated module.
     * @param state New state
     */
    @Override
    public void moduleStateUpdated(AbstractBaseModule module, Object state) {
        this.module = module;
        if ((state instanceof Integer)) {
            if (config.getInverterMask() != 0) {
                this.state = ((Integer) state) ^ config.getInverterMask();
                logger.trace("State inverted: {}, {} -> {}",
                        Integer.toBinaryString((Integer) state),
                        Integer.toBinaryString(config.getInverterMask()),
                        Integer.toBinaryString(this.state));
            } else {
                this.state = (Integer) state;
            }
            refreshState();
        }
    }

    /**
     * Update thing channels based on the module state.
     */
    private void refreshState() {
        if (module != null) {
            logger.trace("Refreshing state: {}", Integer.toBinaryString(this.state));
            int bit = 1;
            for (int i = 1; i < 9; i++) {
                boolean b = (state & bit) == bit;
                State state = isRelayModule() ? b ? OnOffType.ON : OnOffType.OFF :
                        b ? OpenClosedType.OPEN : OpenClosedType.CLOSED;
                updateState(String.format("%s-%d", module.getModuleType().getChannelPrefix(), i), state);
                bit = bit << 1;
            }

            updateStatus(ThingStatus.ONLINE);
            logger.debug("State refreshed: {} -> {}", module.getAddress().toString(), state);
        }
    }

    /**
     * Module type check;
     *
     * @return TRUE for relay type modules
     */
    private boolean isRelayModule() {
        return module != null && (module.getModuleType() == ModuleType.DMR || module.getModuleType() == ModuleType.BIR);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, org.eclipse.smarthome.core.types.Command command) {
        if (command instanceof RefreshType) {
            requestStatusUpdate();
        } else {
            if (module != null) {
                if (isRelayModule()) {
                    String channelId = channelUID.getId();
                    int idx = channelId.indexOf("-");
                    String id = channelId.substring(idx + 1);
                    module.executeCommand(Command.valueOf(command.toString()), Integer.parseInt(id));
                } else {
                    String commandStr = command.toString();
                    String[] parts = commandStr.split("-");
                    if (parts.length == 2) {
                        String pushCommand = parts[0];
                        int input = Integer.parseInt(parts[1]);
                        module.executeCommand(Command.valueOf(pushCommand), input);
                    }
                }
            } else {
                logger.trace("Module needs an update before it can execute any command: {}", getThing().getUID());
            }
        }
    }
}

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
 * Dimmer module
 *
 * @author Gabor Bicskei - Initial contribution
 */
public class DimmerModule extends AbstractBaseModule {
    /**
     * Domintell logger. Uses a common category for all Domintell related logging.
     */
    private final Logger logger = LoggerFactory.getLogger(DimmerModule.class);

    public DimmerModule(DomintellConnection connection, ModuleType moduleType, ModuleAddress address) {
        super(connection, moduleType, address);
    }

    public void on(Integer output) {
        connection.sendCommand(moduleType.toString() + address + "-" + Integer.toString(output) + "%I");
    }

    public void off(Integer output) {
        connection.sendCommand(moduleType.toString() + address + "-" + Integer.toString(output) + "%O");
    }

    public void toggle(Integer output) {
        connection.sendCommand(moduleType.toString() + address + "-" + Integer.toString(output));
    }

    public void queryState(Integer output) {
        connection.sendCommand(moduleType.toString() + address + "-" + Integer.toString(output) + "%S");
    }

    public void setLevel(Integer output, int level) {
        connection.sendCommand(moduleType.toString() + address + "-" + Integer.toString(output) + "%D" + Integer.toString(level));
    }

    // Feedback method from HomeWorksDevice ---------------------------------------------------------
    @Override
    public void processUpdate(String info) {
        try {
            // D 064 0 0 0 0 0 0
            int[] state = new int[8];
            for (int i = 0; i < 8; i++) {
                state[i] = Integer.parseInt(info.substring(1 + i * 2, 3 + i * 2).trim(), 16);
            }
            getUpdateListener().moduleStateUpdated(this, state);
        } catch (NumberFormatException e) {
            logger.warn("Invalid feedback received {}", info, e);
        }

        super.processUpdate(info);
    }

    @Override
    public void executeCommand(Command command, Integer input) {
        //not implemented
    }
}

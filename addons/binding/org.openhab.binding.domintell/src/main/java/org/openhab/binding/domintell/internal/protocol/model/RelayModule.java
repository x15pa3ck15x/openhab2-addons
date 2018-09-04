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
import org.openhab.binding.domintell.internal.protocol.exception.CommandException;

/**
 * Relay module
 *
 * @author Gabor Bicskei - Initial contribution
 */
public class RelayModule extends AbstractMultiIOModule {

    public RelayModule(DomintellConnection connection, ModuleType moduleType, ModuleAddress address) {
        super(connection, moduleType, address);
    }

    public void on(Integer output) {
        connection.sendCommand(moduleType.toString() + address + "-" + Integer.toString(output) + "%I");
    }

    private void off(Integer output) {
        connection.sendCommand(moduleType.toString() + address + "-" + Integer.toString(output) + "%O");
    }

    private void toggle(Integer output) {
        connection.sendCommand(moduleType.toString() + address + "-" + Integer.toString(output));
    }

    private void queryState(Integer output) {
        connection.sendCommand(moduleType.toString() + address + "-" + Integer.toString(output) + "%S");
    }

    public void executeCommand(Command command, Integer input) {
        switch (command) {
            case ON:
                on(input);
                break;
            case OFF:
                off(input);
                break;
            case TOGGLE:
                toggle(input);
                break;
            case REFRESH:
                queryState(input);
                break;
            default:
                throw new CommandException("Unsupported command", command);
        }
    }
}

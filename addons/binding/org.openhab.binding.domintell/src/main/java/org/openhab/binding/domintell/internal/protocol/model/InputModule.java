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
 * Input module
 *
 * @author Gabor Bicskei - Initial contribution
 */
public class InputModule extends AbstractMultiIOModule {
    public InputModule(DomintellConnection connection, ModuleType moduleType, ModuleAddress address) {
        super(connection, moduleType, address);
    }

    private void beginShortPush(Integer input) {
        connection.sendCommand(moduleType.toString() + address + "-" + Integer.toString(input) + "%P1");
    }

    private void endShortPush(Integer input) {
        connection.sendCommand(moduleType.toString() + address + "-" + Integer.toString(input) + "%P2");
    }

    private void beginLongPush(Integer input) {
        connection.sendCommand(moduleType.toString() + address + "-" + Integer.toString(input) + "%P3");
    }

    private void endLongPush(Integer input) {
        connection.sendCommand(moduleType.toString() + address + "-" + Integer.toString(input) + "%P4");
    }

    private void queryState(Integer input) {
        connection.sendCommand(moduleType.toString() + address + "-" + Integer.toString(input) + "%S");
    }

    public void executeCommand(Command command, Integer input) {
        switch (command) {
            case SHORT_PUSH:
                beginShortPush(input);
                endShortPush(input);
                break;
            case LONG_PUSH:
                beginLongPush(input);
                endLongPush(input);
                break;
            case REFRESH:
                queryState(input);
                break;
            default:
                throw new CommandException("Unsupported command", command);
        }
    }
}

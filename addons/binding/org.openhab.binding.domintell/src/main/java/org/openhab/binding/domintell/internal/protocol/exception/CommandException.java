/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.domintell.internal.protocol.exception;

import org.openhab.binding.domintell.internal.protocol.model.Command;

/**
 * This exception is raised for errors occurred during executing commands on Domintell modules.
 *
 * @author Gabor Bicskei - Initial contribution
 */
public class CommandException extends RuntimeException {
    public CommandException(String message, Command command) {
        super(String.format("%s: %s", message, command.toString()));
    }
}

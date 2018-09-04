/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.domintell.internal.protocol.exception;

import org.openhab.binding.domintell.internal.protocol.ModuleAddress;

/**
 * This exception is thrown in case of improper module references.
 *
 * @author Gabor Bicskei - Initial contribution
 */
public class ModuleException extends Exception {
    private ModuleAddress address;

    public ModuleException(String message, ModuleAddress address, Throwable cause) {
        super(message, cause);
        this.address = address;
    }

    public ModuleAddress getAddress() {
        return address;
    }
}

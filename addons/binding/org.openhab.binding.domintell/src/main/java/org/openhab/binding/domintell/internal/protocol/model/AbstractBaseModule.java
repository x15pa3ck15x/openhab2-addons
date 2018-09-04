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
import org.openhab.binding.domintell.internal.protocol.ModuleUpdateListener;

/**
 * Abstract Domintell module
 *
 * @author Gabor Bicskei - Initial contribution
 */
public abstract class AbstractBaseModule {
    /**
     * Connection used to send messages.
     */
    DomintellConnection connection;

    /**
     * MOdule type
     */
    protected ModuleType moduleType;

    /**
     * Address of this module.
     */
    protected ModuleAddress address;

    /**
     * Listener for module changes.
     */
    private ModuleUpdateListener updateListener;

    // Constructors ---------------------------------------------------------------------------------

    public AbstractBaseModule(DomintellConnection connection, ModuleType moduleType, ModuleAddress address) {
        super();
        this.connection = connection;
        this.moduleType = moduleType;
        this.address = address;
    }

   // Public methods -------------------------------------------------------------------------------

    public void processUpdate(String info) {}

    public ModuleUpdateListener getUpdateListener() {
        return updateListener;
    }

    public ModuleType getModuleType() {
        return moduleType;
    }

    public ModuleAddress getAddress() {
        return address;
    }

    public void setUpdateListener(ModuleUpdateListener updateListener) {
        this.updateListener = updateListener;
    }

    public abstract void executeCommand(Command command, Integer input);
}

/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.domintell.internal.protocol;

import org.openhab.binding.domintell.internal.protocol.model.*;

/**
 * Module type enumeration
 *
 * @author Gabor Bicskei - Initial contribution
 */
public enum ModuleType {
    BIR(RelayModule.class, "output"),
    DMR(RelayModule.class, "output"),
    TRP(RelayModule.class),
    DIM(DimmerModule.class),
    D10(DimmerModule.class),
    TSB(TemperatureModule.class),
    TE1(TemperatureModule.class),
    TE2(TemperatureModule.class),
    LC3(TemperatureModule.class),
    PBL(TemperatureModule.class),
    IS4(InputModule.class, "contact"),
    IS8(InputModule.class, "contact"),
    VAR(VariableModule.class);

    private Class<? extends AbstractBaseModule> clazz;

    private String channelPrefix;

    ModuleType(Class<? extends AbstractBaseModule> clazz) {
        this.clazz = clazz;
    }

    ModuleType(Class<? extends AbstractBaseModule> clazz, String channelPrefix) {
        this.channelPrefix = channelPrefix;
        this.clazz = clazz;
    }

    public Class<? extends AbstractBaseModule> getClazz() {
        return clazz;
    }

    public String getChannelPrefix() {
        return channelPrefix;
    }
}

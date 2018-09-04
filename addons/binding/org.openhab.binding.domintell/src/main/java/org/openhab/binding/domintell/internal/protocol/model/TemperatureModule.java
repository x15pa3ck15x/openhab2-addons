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
import org.openhab.binding.domintell.internal.protocol.RegulationMode;
import org.openhab.binding.domintell.internal.protocol.exception.CommandException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.StringTokenizer;

/**
 * Temperature module
 *
 * @author Gabor Bicskei - Initial contribution
 */
public class TemperatureModule extends AbstractBaseModule {

    /**
     * Class logger
     */
    private final Logger log = LoggerFactory.getLogger(TemperatureModule.class);

    /**
     * Thing info.
     */
    private ThingInfo thingInfo;

    /**
     * Temperature format
     */
    private NumberFormat temperatureFormat;

    public TemperatureModule(DomintellConnection connection, ModuleType moduleType, ModuleAddress address) {
        super(connection, moduleType, address);
        temperatureFormat = NumberFormat.getInstance(Locale.US);
        temperatureFormat.setMinimumIntegerDigits(2);
        temperatureFormat.setMaximumIntegerDigits(2);
        temperatureFormat.setMinimumFractionDigits(1);
        temperatureFormat.setMaximumFractionDigits(1);
    }

    public void setSetPoint(Float setPoint) {
        connection.sendCommand(moduleType.toString() + address + "%T" + temperatureFormat.format(setPoint));
    }

    public void setMode(RegulationMode mode) {
        connection.sendCommand(moduleType.toString() + address + "%M" + mode.getValue());
    }

    private void queryState() {
        connection.sendCommand(moduleType.toString() + address + "%S");
    }

    // Feedback method from HomeWorksDevice ---------------------------------------------------------
    @Override
    public void processUpdate(String info) {
        if (info != null) {
            if (info.contains("[")) {
                //processing APPINFO
                thingInfo = ThingInfo.parseThingInfo(info.substring(2));
            } else {
                try {
                    // T 0.0 18.0 AUTO 18.0
                    StringTokenizer st = new StringTokenizer(info.substring(1));
                    String[] state = new String[4];
                    for (int i = 0; i < 4; i++) {
                        state[i] = st.nextToken();
                    }
                    if (getUpdateListener() != null) {
                        getUpdateListener().moduleStateUpdated(this, state);
                    }
                } catch (NumberFormatException e) {
                    log.warn("Invalid feedback received {}", info, e);
                }
            }
        }
        super.processUpdate(info);
    }

    public ThingInfo getThingInfo() {
        return thingInfo;
    }

    @Override
    public void executeCommand(Command command, Integer input) {
        switch (command) {
            case REFRESH:
                queryState();
                break;
            default:
                throw new CommandException("Unsupported command", command);
        }
    }
}

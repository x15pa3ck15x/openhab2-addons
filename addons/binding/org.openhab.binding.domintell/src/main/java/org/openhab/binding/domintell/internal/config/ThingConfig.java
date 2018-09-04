/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.domintell.internal.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Domintell module configuration
 *
 * @author Gabor Bicskei - Initial contribution
 */
public class ThingConfig {
    /**
     * Class logger
     */
    private final Logger logger = LoggerFactory.getLogger(ThingConfig.class);

    private Integer resetTimeout;
    private String autoResetMask;
    private String inverterMask;

    private Integer parsedInverterMask;
    private Integer parsedAutoResetMask;

    public void setResetTimeout(Integer resetTimeout) {
        this.resetTimeout = resetTimeout;
    }

    public void setAutoResetMask(String autoResetMask) {
        this.autoResetMask = autoResetMask;
    }

    public void setInverterMask(String inverterMask) {
        this.inverterMask = inverterMask;
    }

    public Integer getInverterMask() {
        if (parsedInverterMask == null) {
            try {
                parsedInverterMask = Integer.parseInt(inverterMask, 2);
            } catch (NumberFormatException ne) {
                logger.debug("Invalid inverter mask. Using 0 instead.");
                parsedInverterMask = 0;
            }
        }
        return parsedInverterMask;
    }

    public Integer getAutoResetMask() {
        if (parsedAutoResetMask == null) {
            try {
                parsedAutoResetMask = Integer.parseInt(autoResetMask, 2);
            } catch (NumberFormatException ne) {
                logger.debug("Invalid auto reset mask. Using 0 instead.");
                parsedAutoResetMask = 0;
            }
        }
        return parsedAutoResetMask;
    }

    public Integer getResetTimeout() {
        return resetTimeout;
    }
}

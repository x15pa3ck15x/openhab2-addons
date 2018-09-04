/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.domintell.internal.discovery;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.domintell.internal.DomintellBindingConstants;
import org.openhab.binding.domintell.internal.util.TranslationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.openhab.binding.domintell.internal.DomintellBindingConstants.BRIDGE_THING_TYPE;
import static org.openhab.binding.domintell.internal.DomintellBindingConstants.LANGUAGE_BRIDGE_KEY;

/**
 * Discovery service for bridge
 *
 * @author Gabor Bicskei - Initial contribution
 */
public class BridgeDiscoveryService extends AbstractDiscoveryService {
    /**
     * Domintell logger. Uses a common category for all Domintell related logging.
     */
    private final Logger logger = LoggerFactory.getLogger(BridgeDiscoveryService.class);

    private static final int TIMEOUT = 5;

    /**
     * Helper to localize bridge name.
     */
    private TranslationUtil translationUtil;

    /**
     * Constructor.
     *
     * @param translationUtil Translation utility
     */
    public BridgeDiscoveryService(TranslationUtil translationUtil) {
        super(DomintellBindingConstants.BRIDGE_THING_TYPES_UIDS, TIMEOUT, true);
        this.translationUtil = translationUtil;
        logger.debug("Bridge discovery service initiated.");
    }

    /**
     * Add new bridge.
     */
    public void addBridge() {
        ThingUID uid = new ThingUID(BRIDGE_THING_TYPE, DomintellBindingConstants.DETH02);
        String name = translationUtil.getText(LANGUAGE_BRIDGE_KEY);
        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(uid)
                .withThingType(BRIDGE_THING_TYPE)
                .withLabel(name)
                .build();
        thingDiscovered(discoveryResult);
        logger.debug("Bridge discovered.");
    }

    @Override
    protected void startScan() {
    }
}

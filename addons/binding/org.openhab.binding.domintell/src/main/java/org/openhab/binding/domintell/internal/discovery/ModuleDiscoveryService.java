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
import org.openhab.binding.domintell.internal.protocol.DomintellConnection;
import org.openhab.binding.domintell.internal.protocol.ModuleAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

/**
 * Discovery service for modules.
 *
 * @author Gabor Bicskei - Initial contribution
 */
public class ModuleDiscoveryService extends AbstractDiscoveryService {
    /**
     * Domintell logger. Uses a common category for all Domintell related logging.
     */
    private final Logger logger = LoggerFactory.getLogger(ModuleDiscoveryService.class);

    private static final int TIMEOUT = 5;

    /**
     * Domintell connection
     */
    private DomintellConnection domintellConnection;

    /**
     * Domintell bridge uid.
     */
    private ThingUID bridgeUid;

    /**
     * Constructor.
     *
     * @param bridgeUid Bridge id.
     */
    public ModuleDiscoveryService(ThingUID bridgeUid) {
        super(DomintellBindingConstants.SUPPORTED_THING_TYPES_UIDS, TIMEOUT, true);
        this.bridgeUid = bridgeUid;
        logger.debug("Module discovery service created");
    }

    @Override
    public void deactivate() {
        removeOlderResults(new Date().getTime());
        super.deactivate();
    }

    /**
     * Discover new module.
     *
     * @param uid Thing id.
     * @param thingName Thing localized name.
     * @param address Module address.
     */
    public void discoverModule(ThingUID uid, String thingName, ModuleAddress address) {
        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(uid)
                .withBridge(bridgeUid)
                .withLabel(thingName)
                .build();
        thingDiscovered(discoveryResult);
        logger.debug("Module discovered: {}", address.toString());
    }

    public void setDomintellConnection(DomintellConnection domintellConnection) {
        this.domintellConnection = domintellConnection;
    }

    /**
     * Start the module scan by putting the connection into discovery mode.
     */
    @Override
    protected void startScan() {
        if (domintellConnection != null) {
            domintellConnection.discover();
        }
    }
}

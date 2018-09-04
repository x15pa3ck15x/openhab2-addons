/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.domintell.internal.handler;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.thing.*;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.domintell.internal.config.BridgeConfig;
import org.openhab.binding.domintell.internal.discovery.ModuleDiscoveryService;
import org.openhab.binding.domintell.internal.protocol.DomintellConnection;
import org.openhab.binding.domintell.internal.protocol.StateListener;
import org.openhab.binding.domintell.internal.protocol.model.AbstractBaseModule;
import org.openhab.binding.domintell.internal.util.TranslationUtil;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.TimeZone;

import static org.openhab.binding.domintell.internal.DomintellBindingConstants.*;

/**
 * Bridge handler managing the Domintell connection
 *
 * @author Gabor Bicskei - Initial contribution
 */
public class DomintellBridgeHandler extends BaseBridgeHandler {
    /**
     * Domintell logger. Uses a common category for all Domintell related logging.
     */
    private final Logger logger = LoggerFactory.getLogger(DomintellBridgeHandler.class);

    /**
     * Module discovery service
     */
    private ModuleDiscoveryService moduleDiscoveryService;

    /**
     * Domintell connection
     */
    private DomintellConnection connection;

    /**
     * Translation helper. Module labels are customized in order to include the address in their labels. This helps to
     * identify the module in the Domintell configuration software.
     */
    private TranslationUtil translationUtil;

    /**
     * Configuration
     */
    private BridgeConfig config;

    /**
     * Constructor.
     *
     * @param bridge Bridge thing.
     */
    public DomintellBridgeHandler(Bridge bridge) {
        super(bridge);
        config = getConfigAs(BridgeConfig.class);
        connection = new DomintellConnection(config, this::updateGatewayState, this::handleNewModule);
        logger.debug("Bridge handler created.");
    }

    @Override
    protected void updateConfiguration(Configuration configuration) {
        super.updateConfiguration(configuration);
        config = getConfigAs(BridgeConfig.class);
        connection.setConfig(config);
        logger.debug("Bridge configuration updated.");
    }

    @Override
    public void setBundleContext(BundleContext bundleContext) {
        super.setBundleContext(bundleContext);
        translationUtil = new TranslationUtil(bundleContext);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        //no command handling for this bridge
    }

    @Override
    public void initialize() {
        logger.debug("Starting Domintell connection");
        updateStatus(ThingStatus.UNKNOWN);
        connection.startGateway();
    }

    public DomintellConnection getGateway() {
        return connection;
    }

    /**
     * Callback function for the connection to update the state of the bridge.
     *
     * @param state New state
     * @param msg Error message (optional)
     */
    private void updateGatewayState(StateListener.State state, @Nullable String msg) {
        if (isInitialized()) {
            switch (state) {
                case ONLINE:
                    updateStatus(ThingStatus.ONLINE);
                    Date domintellDate = connection.getDomintellSysdate();
                    if (domintellDate != null) {
                        DateTimeType value = new DateTimeType(
                                ZonedDateTime.ofInstant(
                                        domintellDate.toInstant(), TimeZone.getDefault().toZoneId()));
                        updateState(CHANNEL_SYSTEM_DATE, value);
                    }
                    break;
                case STARTING_SESSION:
                case INITIALIZING:
                    updateStatus(ThingStatus.UNKNOWN);
                    break;
                case STALE:
                case STOPPING:
                case OFFLINE:
                    updateStatus(ThingStatus.OFFLINE);
                    break;
                case FATAL:
                case ERROR:
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, msg);
                    logger.debug("Error received from the connection: {}", msg);
            }
        }
    }

    /**
     * Callback method for Domintell connection for module discovery.
     *
     * @param module The new module in the installation the connection received message from
     */
    private void handleNewModule(AbstractBaseModule module) {
        ThingUID thingUID = getThingUID(module);
        if (thingUID != null) {
            if (getThingByUID(thingUID) == null) {
                String key = String.format(LANGUAGE_LABEL_KEY, module.getModuleType().toString().toLowerCase());
                String name = translationUtil.getText(key, module.getAddress().getAddressHex(), module.getAddress().getAddress().toString());
                moduleDiscoveryService.discoverModule(thingUID, name, module.getAddress());
            }
        } else {
            logger.debug("Module is not supported by the binding: {}", module.getAddress().toString());
        }
    }

    /**
     * Construct thing uid for module.
     *
     * @param module Domintell module.
     * @return Thing uid
     */
    private ThingUID getThingUID(AbstractBaseModule module) {
        switch (module.getModuleType()) {
            case BIR:
                return new ThingUID(THING_TYPE_BIR, getThing().getUID(), module.getAddress().getAddressHex());
            case DMR:
                return new ThingUID(THING_TYPE_DMR, getThing().getUID(), module.getAddress().getAddressHex());
            case IS4:
                return new ThingUID(THING_TYPE_IS4, getThing().getUID(), module.getAddress().getAddressHex());
            case IS8:
                return new ThingUID(THING_TYPE_IS8, getThing().getUID(), module.getAddress().getAddressHex());
            case TE1:
                return new ThingUID(THING_TYPE_TE1, getThing().getUID(), module.getAddress().getAddressHex());
            case VAR:
                return new ThingUID(THING_TYPE_VAR, getThing().getUID(), module.getAddress().getAddressHex());
            //add new module type support here
        }
        return null;
    }

    public void setModuleDiscoveryService(ModuleDiscoveryService moduleDiscoveryService) {
        this.moduleDiscoveryService = moduleDiscoveryService;
        moduleDiscoveryService.setDomintellConnection(connection);
    }

    /**
     * Stopping the connection when the bridge is disposed.
     */
    @Override
    public void dispose() {
        connection.stopGateway();
        super.dispose();
    }
}

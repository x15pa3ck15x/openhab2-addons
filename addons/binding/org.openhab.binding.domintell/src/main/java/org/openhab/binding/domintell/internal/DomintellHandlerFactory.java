/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.openhab.binding.domintell.internal;

import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.openhab.binding.domintell.internal.discovery.BridgeDiscoveryService;
import org.openhab.binding.domintell.internal.discovery.ModuleDiscoveryService;
import org.openhab.binding.domintell.internal.handler.DomintellBridgeHandler;
import org.openhab.binding.domintell.internal.handler.DomintellIOThingHandler;
import org.openhab.binding.domintell.internal.handler.DomintellTemperatureThingHandler;
import org.openhab.binding.domintell.internal.handler.DomintellVariableThingHandler;
import org.openhab.binding.domintell.internal.protocol.DomintellConnection;
import org.openhab.binding.domintell.internal.protocol.ModuleType;
import org.openhab.binding.domintell.internal.util.TranslationUtil;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Hashtable;

import static org.openhab.binding.domintell.internal.DomintellBindingConstants.BRIDGE_THING_TYPES_UIDS;
import static org.openhab.binding.domintell.internal.DomintellBindingConstants.SUPPORTED_THING_TYPES_UIDS;

/**
 * The {@link DomintellHandlerFactory} is responsible for creating things and thing handlers.
 *
 * @author Gabor Bicskei - Initial contribution
 */
@Component(service = ThingHandlerFactory.class, immediate = true, configurationPid = "binding.domintell", configurationPolicy = ConfigurationPolicy.OPTIONAL)
public class DomintellHandlerFactory extends BaseThingHandlerFactory {
    /**
     * Domintell logger. Uses a common category for all Domintell related logging.
     */
    private final Logger logger = LoggerFactory.getLogger(DomintellHandlerFactory.class);

    /**
     * Module discovery service registration
     */
    private ServiceRegistration<?> moduleDiscoveryServiceReg;

    /**
     * Bridge discovery service registration
     */
    private ServiceRegistration<?> bridgeDiscoveryServiceReg;

    /**
     * Domintell connection
     */
    private DomintellConnection connection;

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID) || BRIDGE_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    /**
     * By activating the component the bridge discovery service is also started.
     *
     * @param componentContext Component context.
     */
    @Override
    protected void activate(ComponentContext componentContext) {
        super.activate(componentContext);
        registerBridgeDiscoveryService();
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        if (BRIDGE_THING_TYPES_UIDS.contains(thing.getThingTypeUID())) {
            DomintellBridgeHandler handler = new DomintellBridgeHandler((Bridge) thing);
            connection = handler.getGateway();

            //registering module discovery service
            registerModuleDiscoveryService(handler);

            //unregistering bridge discovery service as we have the bridge
            unregisterBridgeDiscoveryService();

            return handler;
        } else {
            ModuleType mt = ModuleType.valueOf(thingTypeUID.getId().toUpperCase());
            try {
                switch (mt) {
                    case BIR:
                    case DMR:
                    case IS8:
                    case IS4:
                        return new DomintellIOThingHandler(thing, connection);
                    case TE1:
                        return new DomintellTemperatureThingHandler(thing, connection);
                    case VAR:
                        return new DomintellVariableThingHandler(thing, connection);
                }
            } catch (IllegalArgumentException e) {
                logger.debug("Unknown module type: {}", thingTypeUID.getId());
            }
        }
        return null;
    }

    /**
     * Register module discovery service
     */
    private void registerModuleDiscoveryService(DomintellBridgeHandler bridgeHandler) {
        ModuleDiscoveryService service = new ModuleDiscoveryService(bridgeHandler.getThing().getUID());
        moduleDiscoveryServiceReg = bundleContext.registerService(DiscoveryService.class.getName(),
                service, new Hashtable<>());
        bridgeHandler.setModuleDiscoveryService(service);
    }

    /**
     * Register bridge discovery service
     */
    private void registerBridgeDiscoveryService() {
        BridgeDiscoveryService service = new BridgeDiscoveryService(new TranslationUtil(bundleContext));
        moduleDiscoveryServiceReg = bundleContext.registerService(
                DiscoveryService.class.getName(), service, new Hashtable<>());
        service.addBridge();
    }

    /**
     * Unregister bridge discovery service
     */
    private void unregisterBridgeDiscoveryService() {
        if (bridgeDiscoveryServiceReg != null) {
            bridgeDiscoveryServiceReg.unregister();
            bridgeDiscoveryServiceReg = null;
        }
    }

    /**
     * Unregister module discovery service when the bridge handler is removed.
     *
     * @param thingHandler Bridge thing handler
     */
    @Override
    protected synchronized void removeHandler(ThingHandler thingHandler) {
        if (thingHandler instanceof DomintellBridgeHandler) {
            if (moduleDiscoveryServiceReg != null) {
                // remove discovery service, if bridge handler is removed
                ModuleDiscoveryService dDiscoveryService = (ModuleDiscoveryService) bundleContext
                        .getService(moduleDiscoveryServiceReg.getReference());
                dDiscoveryService.deactivate();
                moduleDiscoveryServiceReg.unregister();
                moduleDiscoveryServiceReg = null;
            }
        }
    }
}

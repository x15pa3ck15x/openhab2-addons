/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.domintell.internal;

import com.google.common.collect.Sets;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.openhab.binding.domintell.internal.protocol.ModuleType;

import java.util.Collections;
import java.util.Set;

/**
 * The {@link DomintellBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Gabor Bicskei - Initial contribution
 */
public class DomintellBindingConstants {
    private static final String BINDING_ID = "domintell";

    //bridge
    private static final String BRIDGE = "bridge";
    public static final String DETH02 = "DETH02";
    public static final ThingTypeUID BRIDGE_THING_TYPE = new ThingTypeUID(BINDING_ID, BRIDGE);

    //generic thing types
    public static final ThingTypeUID THING_TYPE_BIR = new ThingTypeUID(BINDING_ID, ModuleType.BIR.toString().toLowerCase());
    public static final ThingTypeUID THING_TYPE_DMR = new ThingTypeUID(BINDING_ID, ModuleType.DMR.toString().toLowerCase());
    public static final ThingTypeUID THING_TYPE_IS8 = new ThingTypeUID(BINDING_ID, ModuleType.IS8.toString().toLowerCase());
    public static final ThingTypeUID THING_TYPE_IS4 = new ThingTypeUID(BINDING_ID, ModuleType.IS4.toString().toLowerCase());
    public static final ThingTypeUID THING_TYPE_TE1 = new ThingTypeUID(BINDING_ID, ModuleType.TE1.toString().toLowerCase());
    public static final ThingTypeUID THING_TYPE_VAR = new ThingTypeUID(BINDING_ID, ModuleType.VAR.toString().toLowerCase());

    // thing type sets
    public static final Set<ThingTypeUID> BRIDGE_THING_TYPES_UIDS = Collections.singleton(BRIDGE_THING_TYPE);
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Sets.newHashSet(THING_TYPE_BIR, THING_TYPE_DMR,
            THING_TYPE_IS8, THING_TYPE_IS4, THING_TYPE_TE1, THING_TYPE_VAR);

    //channel names
    public static final String CHANNEL_SYSTEM_DATE = "systemDate";
    public static final String CHANNEL_GROUP_THERMOSTAT = "thermostat";
    public static final String CHANNEL_CURRENT_VALUE = "currentValue";
    public static final String CHANNEL_PRESET_VALUE = "presetValue";
    public static final String CHANNEL_MODE = "mode";
    public static final String CHANNEL_PROFILE_VALUE = "profileValue";
    public static final String CHANNEL_ID_THERMOSTAT_MODE = CHANNEL_GROUP_THERMOSTAT + "#" + CHANNEL_MODE;
    public static final String CHANNEL_ID_THERMOSTAT_PRESET_VALUE = CHANNEL_GROUP_THERMOSTAT + "#" + CHANNEL_PRESET_VALUE;
    public static final String CHANNEL_NUM_VALUE = "numValue";
    public static final String CHANNEL_BOOLEAN_VALUE = "booleanValue";

    //language keys for localization
    public static final String LANGUAGE_LABEL_KEY = "thing-type." + BINDING_ID + ".%s.description";
    public static final String LANGUAGE_BRIDGE_KEY = "thing-type.domintell.bridge.description";
}

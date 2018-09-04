/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.domintell.internal.protocol.model;

/**
 * Thing information received from Domintell system
 *
 * @author Gabor Bicskei - Initial contribution
 */
public class ThingInfo {
    private String name;
    private String location;
    private String extra;

    public ThingInfo(String name, String location, String extra) {
        this.name = name;
        this.location = location;
        this.extra = extra;
    }

    public String getName() {
        return name;
    }

    public String getLocation() {
        return location;
    }

    public String getExtra() {
        return extra;
    }

    public static ThingInfo parseThingInfo(String info) {
        int nameEnd = info.indexOf('[');
        String name = info.substring(0, nameEnd);
        int locationEnd = info.indexOf(']', nameEnd);
        String location = info.substring(nameEnd + 1, locationEnd);
        int extraStart = info.indexOf('[', locationEnd);
        String extra = extraStart != -1 ? info.substring(extraStart + 1, info.length() - 1) : null;
        return new ThingInfo(name, location, extra);
    }
}

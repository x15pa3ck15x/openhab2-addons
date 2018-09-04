/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.domintell.internal.protocol;

/**
 * Domintell module address
 *
 * @author Gabor Bicskei - Initial contribution
 */
public class ModuleAddress {
    /**
     * Address in decimal format
     */
    private int address;

    /**
     * Constructor.
     *
     * @param address Hex address to parse.
     */
    public ModuleAddress(String address) {
        this.address = Integer.parseInt(address.trim(), 16);
    }

    public String getAddressHex() {
        return Integer.toString(address, 16).toUpperCase();
    }

    public Integer getAddress() {
        return address;
    }

    /**
     * Writing the address into 6 char long hes format.
     *
     * @return Address string.
     */
    public String toString() {
        String str = "      " + Integer.toString(address, 16).toUpperCase();
        return str.length() <= 6 ? str : str.substring(str.length() - 6);
    }
}

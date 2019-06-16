/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.dominoswiss;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link DominoswissBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Frieso Aeschbacher - Initial contribution
 */
public class DominoswissBindingConstants {

    public static final String BINDING_ID = "dominoswiss";
    // List of all Thing Type UIDs
    public static final ThingTypeUID DOMINOSWISSBLINDS_THING_TYPE = new ThingTypeUID(BINDING_ID, "blind");
    public static final ThingTypeUID DOMINOSWISSEGATE_THING_TYPE = new ThingTypeUID(BINDING_ID, "egate");

    // List of all Channel ids
    public static final String CHANNEL_PULSEUP = "pulseUp";
    public static final String CHANNEL_PULSEDOWN = "pulseDown";
    public static final String CHANNEL_CONTINOUSUP = "continousUp";
    public static final String CHANNEL_CONTINOUSDOWN = "continousDown";
    public static final String CHANNEL_STOP = "STOP";
    public static final String UP = "UP";
    public static final String DOWN = "DOWN";
    public static final String SHUTTER = "shutter";
    public static final String TILTUP = "tiltup";
    public static final String TILTDOWN = "tiltDown";
    public static final String TILT = "tilt";

    public static final String GETCONFIG = "getconfig";

    public static final String CR = "\r";

}

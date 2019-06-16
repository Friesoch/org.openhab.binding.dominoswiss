/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.openhab.binding.dominoswiss.internal;

/**
 * Configuration of a server.
 *
 * @author Frieso Aeschbacher - Initial contribution
 */

public class DominoswissConfig {

    /**
     * Server ip address
     */
    public String ipAddress;
    /**
     * Server web port for REST calls
     */
    public int port;

    /**
     * Language for TTS
     */
    public String language;
}

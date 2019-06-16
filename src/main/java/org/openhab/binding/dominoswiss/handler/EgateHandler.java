/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.dominoswiss.handler;

import static org.openhab.binding.dominoswiss.DominoswissBindingConstants.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link EgateHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Frieso Aeschbacher - Initial contribution
 */
public class EgateHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(EgateHandler.class);
    private Socket egateSocket;

    private int PORT;
    private String HOST;
    private static final int SOCKET_TIMEOUT = 250;
    private final Object lock = new Object();
    private BufferedWriter writer;
    private BufferedReader reader;
    private ScheduledFuture<?> refreshJob;
    private Map<String, ThingUID> registeredBlinds;
    private final ScheduledExecutorService scheduler;

    public EgateHandler(Bridge thing) {
        super(thing);
        registeredBlinds = new HashMap<String, ThingUID>();
        scheduler = Executors.newScheduledThreadPool(1);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (channelUID.getId().equals(GETCONFIG)) {
            sendCommand("EthernetGet;\r");
        }
    }

    @Override
    public void initialize() {
        logger.debug("Initializing the Dominoswiss EGate bridge handler.");

        Configuration config = this.getConfig();
        HOST = (String) config.get("ipAddress");
        PORT = 1318;
        try {
            PORT = Integer.parseInt(config.get("port").toString());
        } catch (NullPointerException e) {
            // keep default
        }
        if (HOST != null && PORT > 0) {
            // Create a socket to eGate
            synchronized (this.lock) {
                try {
                    egateSocket = new Socket();
                    // egateSocket.connect(new InetSocketAddress(HOST, PORT), SOCKET_TIMEOUT);
                    egateSocket.connect(new InetSocketAddress(HOST, PORT));
                    egateSocket.setSoTimeout(SOCKET_TIMEOUT);
                    writer = new BufferedWriter(new OutputStreamWriter(egateSocket.getOutputStream()));
                    writer.write("SilenceModeSet;Value=0;" + CR);
                    writer.flush();
                    // reader = new BufferedReader(new InputStreamReader(egateSocket.getInputStream()));

                } catch (UnknownHostException e) {
                    logger.error("unknown socket host {}", HOST);
                    try {
                        egateSocket.close();
                        updateStatus(ThingStatus.OFFLINE);
                    } catch (IOException e1) {
                        logger.error("EGate Socket not closed {}", e1);
                    }
                    egateSocket = null;
                } catch (SocketException e) {
                    logger.error("{}", e.getLocalizedMessage());
                    try {
                        egateSocket.close();
                        updateStatus(ThingStatus.OFFLINE);
                    } catch (IOException e1) {
                        logger.error("EGate Socket not closed {}", e1);
                    }
                    egateSocket = null;
                } catch (Exception e) {
                    logger.error(
                            "Error while establishing connection to Dominoswiss eGate Server with Port: {} and IP: {} : Error {} ",
                            PORT, HOST, e.toString());
                    updateStatus(ThingStatus.OFFLINE);
                }
                if (egateSocket != null) {
                    updateStatus(ThingStatus.ONLINE);
                }
                startAutomaticRefresh();

                logger.debug("EGate Handler connected and online, Status " + getThing().getStatus().toString());
            }
        } else {
            logger.error("Invalid IP address for dominoswiss eGate or wrong port : '{}'/'{}'", HOST, PORT);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                    "Cannot connect to dominoswiss eGate gateway. host IP address or port are not set.");
        }
    }

    @Override
    public void dispose() {
        try {
            egateSocket.close();
            refreshJob.cancel(true);
            logger.debug("EGate Handler connection closed, disposing");
        } catch (IOException e) {
            logger.error(e.toString());

        }

    }

    public synchronized boolean isConnected() {
        if (egateSocket == null) {
            return false;
        }

        // NOTE: isConnected() returns true once a connection is made and will
        // always return true even after the socket is closed
        // http://stackoverflow.com/questions/10163358/
        return egateSocket.isConnected() && !egateSocket.isClosed();
    }

    /**
     * Possible Instructions are:
     * FssTransmit 1 Kommandoabsetzung (Controller > eGate > Dominoswiss)
     * FssReceive 2 Empfangenes Funkpaket (Dominoswiss > eGate > Controller)
     *
     */

    public void tiltUp(String id) {
        for (int i = 0; i < 3; i++) {
            pulseUp(id);
            try {
                Thread.sleep(150);
            } catch (InterruptedException e) {
                logger.error("EGate tipUp error: ", e.toString());
            }
        }
    }

    public void tiltDown(String id) {
        for (int i = 0; i < 3; i++) {
            pulseDown(id);
            try {
                Thread.sleep(150);
            } catch (InterruptedException e) {
                logger.error("EGate tipdown error: ", e.toString());
            }
        }
    }

    public void pulseUp(String id) {
        sendCommand("Instruction=1;ID=" + id + ";Command=1;Priority=1;CheckNr=3415347;" + CR);
    }

    public void pulseDown(String id) {
        sendCommand("Instruction=1;ID=" + id + ";Command=2;Priority=1;CheckNr=2764516;" + CR);
    }

    public void continuousUp(String id) {
        sendCommand("Instruction=1;ID=" + id + ";Command=3;Priority=1;CheckNr=2867016;" + CR, 20000);
    }

    public void continuousDown(String id) {
        sendCommand("Instruction=1;ID=" + id + ";Command=4;Priority=1;CheckNr=973898;" + CR, 20000);
    }

    public void stop(String id) {
        sendCommand("Instruction=1;ID=" + id + ";Command=5;Priority=1;CheckNr=5408219;" + CR);
    }

    public void registerBlind(String id, ThingUID uid) {
        logger.debug("Registring Blind id {} with thingUID {}", id, uid);
        registeredBlinds.put(id, uid);
    }

    /**
     * Send a command to the eGate Server.
     */

    private void sendCommand(String command) {
        sendCommand(command, SOCKET_TIMEOUT);
    }

    private synchronized void sendCommand(String command, int timeout) {

        logger.debug("EGate got command: {}", command);

        if (!isConnected()) {
            logger.debug("no connection to Dominoswiss eGate server when trying to send command, returning...");
            return;
        }

        // Send plain string to eGate Server,
        logger.debug("Sending command: {}", command);
        try {

            // BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(egateSocket.getOutputStream()));

            egateSocket.setSoTimeout(SOCKET_TIMEOUT);
            writer.write(command);
            writer.flush();
            logger.debug("Sent message");
        } catch (IOException e) {
            logger.error("Error while sending command {} to Dominoswiss eGate Server {} ", command, e.toString());
        }
        /*
         * try {
         * BufferedReader reader = new BufferedReader(new InputStreamReader(egateSocket.getInputStream()));
         * logger.debug("Socket State: " + egateSocket.isConnected() + " to: " + egateSocket.toString());
         * egateSocket.setSoTimeout(SOCKET_TIMEOUT);
         * while (!reader.ready()) {
         *
         * }
         * String input = reader.readLine();
         * logger.debug("Reader got from EGATE: {}", input);
         *
         * } catch (IOException e) {
         * logger.error("Error while reading command {} from Dominoswiss eGate Server {} ", command, e.toString());
         * }
         */
    }

    private void startAutomaticRefresh() {

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    if (reader == null) {
                        reader = new BufferedReader(new InputStreamReader(egateSocket.getInputStream()));
                    }

                    // logger.debug("Socket State: " + egateSocket.isConnected() + " to: " + egateSocket.toString());
                    // egateSocket.setSoTimeout(SOCKET_TIMEOUT);
                    if (reader.ready()) {
                        String input = reader.readLine();
                        logger.debug("Reader got from EGATE: {}", input);
                        onData(input);
                    }
                } catch (IOException e) {
                    logger.error("Error while reading command from Dominoswiss eGate Server {} ", e.toString());
                    updateStatus(ThingStatus.OFFLINE);
                }
            }

        };

        refreshJob = scheduler.scheduleWithFixedDelay(runnable, 0, 1, TimeUnit.SECONDS);
    }

    protected void onData(String input) {
        // Instruction=2;ID=19;Command=1;Value=0;Priority=0;

        Map<String, String> map = new HashMap<String, String>();
        // split on ;
        String[] parts = input.split(";");

        for (int i = 0; i < parts.length; i += 2) {
            map.put(parts[i], parts[i + 1]);
        }

        // only use FSSReceive Commands
        if (map.get("Instruction").toString() == "2") {
            String id = map.get("ID");

            Thing blind = this.getThingByUID(registeredBlinds.get(map.get(id)));
            @NonNull
            List<@NonNull Channel> channels = blind.getChannels();
            logger.debug("Channels: {} of Blind {}", channels.toString(), blind.getConfiguration());

            switch (map.get("Command")) {
                case "1":

                    break;

                case "2":

                    break;

                case "3":

                    break;

                case "4":

                    break;
                default:
                    break;
            }
        }
    }

}

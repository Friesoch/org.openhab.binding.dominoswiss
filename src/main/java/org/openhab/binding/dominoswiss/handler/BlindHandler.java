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

import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.dominoswiss.internal.BlindConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link BlindHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Frieso Aeschbacher - Initial contribution
 */

public class BlindHandler extends BaseThingHandler {

    /**
     * The {@link BlindHandler} class defines common constants, which are
     * used across the whole binding.
     *
     * @author Frieso Aeschbacher - Initial contribution
     */

    private Logger logger = LoggerFactory.getLogger(BlindHandler.class);

    private EgateHandler dominoswissHandler;

    private String id;

    public BlindHandler(Thing thing) {
        super(thing);
    }

    @SuppressWarnings("null")
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {

        logger.debug("Blind got command: {} and ChannelUID: {} ", command.toFullString(),
                channelUID.getIdWithoutGroup());
        try {
            dominoswissHandler = (EgateHandler) getBridge().getHandler();
        } catch (Exception e) {
            logger.error("Could not get Bridge ", e);
        }
        if (dominoswissHandler == null) {
            logger.info("Blind thing {} has no server configured, ignoring command: {}", getThing().getUID(), command);
            return;
        }
        String id = getConfigAs(BlindConfig.class).id;

        // Some of the code below is not designed to handle REFRESH
        if (command == RefreshType.REFRESH) {
            return;
        }
        switch (channelUID.getIdWithoutGroup()) {
            case CHANNEL_PULSEUP:
                if (command instanceof Number) {
                    dominoswissHandler.pulseUp(id);
                }
                break;
            case CHANNEL_PULSEDOWN:
                if (command instanceof Number) {
                    dominoswissHandler.pulseDown(id);
                }
                break;
            case CHANNEL_CONTINOUSUP:
                if (command instanceof Number) {
                    dominoswissHandler.continuousUp(id);
                }
                break;
            case CHANNEL_CONTINOUSDOWN:
                if (command instanceof Number) {
                    dominoswissHandler.continuousDown(id);
                }
                break;
            case CHANNEL_STOP:
                if (command instanceof Number) {
                    dominoswissHandler.stop(id);
                }
                break;
            case UP:
                if (command instanceof Number) {
                    dominoswissHandler.continuousUp(id);
                }
                break;
            case DOWN:
                if (command instanceof Number) {
                    dominoswissHandler.continuousDown(id);
                }
                break;
            case SHUTTER:
                if (command.toFullString() == DOWN) {
                    dominoswissHandler.continuousDown(id);
                } else if (command.toFullString() == UP) {
                    dominoswissHandler.continuousUp(id);
                } else if (command.toFullString() == CHANNEL_STOP) {
                    dominoswissHandler.stop(id);
                } else {
                    logger.debug("Blind got command but nothing executed: {}  and ChannelUID: {}",
                            command.toFullString(), channelUID.getIdWithoutGroup());
                }

            case TILTDOWN:
                if (command instanceof Number) {
                    dominoswissHandler.tiltDown(id);
                }
                break;

            case TILTUP:
                if (command instanceof Number) {
                    dominoswissHandler.tiltUp(id);
                }
                break;

            case TILT:
                if (command.toFullString() == UP) {
                    dominoswissHandler.pulseUp(id);
                } else if (command.toFullString() == DOWN) {
                    dominoswissHandler.pulseDown(id);
                } else if (command.toFullString() == CHANNEL_STOP) {
                    dominoswissHandler.stop(id);
                } else {
                    logger.debug("Blind got command but nothing executed: {}  and ChannelUID: {}",
                            command.toFullString(), channelUID.getIdWithoutGroup());
                }

            default:
                break;
        }

    }

    @Override
    public void initialize() {

        this.id = getConfig().as(BlindConfig.class).id;
        updateBridgeStatus();
        dominoswissHandler.registerBlind(this.id, getThing().getUID());

    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        updateBridgeStatus();
    }

    /*
     * Gets the ID of this Blind
     */
    public String getID() {
        return this.id;
    }

    @SuppressWarnings("null")
    private void updateBridgeStatus() {
        try {
            ThingStatus bridgeStatus = getBridge().getStatus();
            if (bridgeStatus == ThingStatus.ONLINE && getThing().getStatus() != ThingStatus.ONLINE) {
                updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE);
                dominoswissHandler = (EgateHandler) getBridge().getHandler();
            } else if (bridgeStatus == ThingStatus.OFFLINE) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            }
        } catch (Exception e) {
            logger.error("Could not update ThingStatus ", e);

        }
    }
}

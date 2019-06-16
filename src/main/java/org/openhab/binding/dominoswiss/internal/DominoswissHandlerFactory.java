/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.dominoswiss.internal;

import static org.openhab.binding.dominoswiss.DominoswissBindingConstants.*;

import java.util.List;

import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.dominoswiss.handler.BlindHandler;
import org.openhab.binding.dominoswiss.handler.EgateHandler;

import com.google.common.collect.Lists;

/**
 * The {@link DominoswissHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Frieso Aeschbacher - Initial contribution
 */
public class DominoswissHandlerFactory extends BaseThingHandlerFactory {

    private static final List<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Lists.newArrayList(DOMINOSWISSEGATE_THING_TYPE,
            DOMINOSWISSBLINDS_THING_TYPE);

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(DOMINOSWISSEGATE_THING_TYPE)) {
            return new EgateHandler((Bridge) thing);
        }

        if (thingTypeUID.equals(DOMINOSWISSBLINDS_THING_TYPE)) {
            return new BlindHandler(thing);
        }
        return null;
    }
}

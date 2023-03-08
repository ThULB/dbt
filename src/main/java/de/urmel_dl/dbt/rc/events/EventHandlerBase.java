/*
 * This file is part of the Digitale Bibliothek Thüringen repository software.
 * Copyright (c) 2000 - 2016
 * See <https://www.db-thueringen.de/> and <https://github.com/ThULB/dbt/>
 *
 * This program is free software: you can redistribute it and/or modify it under the 
 * terms of the GNU General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or 
 * FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this
 * program. If not, see <http://www.gnu.org/licenses/>.
 */
package de.urmel_dl.dbt.rc.events;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mycore.common.MCRException;
import org.mycore.common.events.MCREvent;
import org.mycore.common.events.MCREventHandler;

import de.urmel_dl.dbt.rc.datamodel.slot.Slot;
import de.urmel_dl.dbt.rc.datamodel.slot.SlotEntry;
import de.urmel_dl.dbt.rc.persistency.SlotManager;

/**
 * Abstract helper class that can be subclassed to implement event handlers more
 * easily.
 * 
 * @author René Adler (eagle)
 *
 */
public abstract class EventHandlerBase implements MCREventHandler {

    private static final Logger LOGGER = LogManager.getLogger(EventHandlerBase.class);

    /**
     * This method handle all calls for EventHandler for the event types
     * {@link Slot} and {@link SlotEntry}.
     * 
     * @param evt
     *            The MCREvent object
     */
    @Override
    public void doHandleEvent(MCREvent evt) throws MCRException {
        if (SlotManager.SLOT_TYPE.equals(evt.getCustomObjectType())) {
            final Slot slot = (Slot) evt.get(SlotManager.SLOT_TYPE);

            if (slot != null) {
                if (evt.getEventType().equals(MCREvent.EventType.CREATE)) {
                    handleSlotCreated(evt, slot);
                } else if (evt.getEventType().equals(MCREvent.EventType.UPDATE)) {
                    handleSlotUpdated(evt, slot);
                } else if (evt.getEventType().equals(MCREvent.EventType.DELETE)) {
                    handleSlotDeleted(evt, slot);
                } else if (SlotManager.INACTIVATE_EVENT.equals(evt.getCustomEventType())) {
                    handleSlotInactivate(evt, slot);
                } else if (SlotManager.REACTIVATE_EVENT.equals(evt.getCustomEventType())) {
                    handleSlotReactivate(evt, slot);
                } else if (SlotManager.OWNER_TRANSFER_EVENT.equals(evt.getCustomEventType())) {
                    handleSlotOwnerTransfer(evt, slot);
                } else {
                    LOGGER.warn("Can't find method for an slot data handler for event type " + evt.getEventType());
                }
                return;
            }

            LOGGER.warn("Can't find method for " + SlotManager.SLOT_TYPE + " for event type " + evt.getEventType());
            return;
        }

        if (SlotManager.ENTRY_TYPE.equals(evt.getCustomObjectType())) {
            final SlotEntry<?> entry = (SlotEntry<?>) evt.get(SlotManager.ENTRY_TYPE);

            if (entry != null) {
                if (evt.getEventType().equals(MCREvent.EventType.CREATE)) {
                    handleEntryCreated(evt, entry);
                } else if (evt.getEventType().equals(MCREvent.EventType.UPDATE)) {
                    handleEntryUpdated(evt, entry);
                } else if (evt.getEventType().equals(MCREvent.EventType.DELETE)) {
                    handleEntryDeleted(evt, entry);
                } else {
                    LOGGER.warn("Can't find method for an slot data handler for event type " + evt.getEventType());
                }
                return;
            }

            LOGGER.warn("Can't find method for " + SlotManager.ENTRY_TYPE + " for event type " + evt.getEventType());
        }
    }

    /**
     * This method roll back all calls for EventHandler for the event types
     * {@link Slot} and {@link SlotEntry}.
     * 
     * @param evt
     *            The MCREvent object
     */
    @Override
    public void undoHandleEvent(MCREvent evt) throws MCRException {
        if (SlotManager.SLOT_TYPE.equals(evt.getCustomObjectType())) {
            final Slot slot = (Slot) evt.get(SlotManager.SLOT_TYPE);

            if (slot != null) {
                if (evt.getEventType().equals(MCREvent.EventType.CREATE)) {
                    undoSlotCreated(evt, slot);
                } else if (evt.getEventType().equals(MCREvent.EventType.UPDATE)) {
                    undoSlotUpdated(evt, slot);
                } else if (evt.getEventType().equals(MCREvent.EventType.DELETE)) {
                    undoSlotDeleted(evt, slot);
                } else {
                    LOGGER.warn("Can't find method for an slot data handler for event type " + evt.getEventType());
                }
                return;
            }

            LOGGER.warn("Can't find method for " + SlotManager.SLOT_TYPE + " for event type " + evt.getEventType());
            return;
        }

        if (SlotManager.ENTRY_TYPE.equals(evt.getCustomObjectType())) {
            final SlotEntry<?> entry = (SlotEntry<?>) evt.get(SlotManager.ENTRY_TYPE);

            if (entry != null) {
                if (evt.getEventType().equals(MCREvent.EventType.CREATE)) {
                    undoEntryCreated(evt, entry);
                } else if (evt.getEventType().equals(MCREvent.EventType.UPDATE)) {
                    undoEntryUpdated(evt, entry);
                } else if (evt.getEventType().equals(MCREvent.EventType.DELETE)) {
                    undoEntryDeleted(evt, entry);
                } else {
                    LOGGER.warn("Can't find method for an slot data handler for event type " + evt.getEventType());
                }
                return;
            }

            LOGGER.warn("Can't find method for " + SlotManager.ENTRY_TYPE + " for event type " + evt.getEventType());
        }
    }

    // This method does nothing. It is very useful for debugging events.
    public void doNothing(MCREvent evt, Object obj) {
        LOGGER.info(getClass().getName() + " does nothing on " + evt.getEventType() + " " + evt.getObjectType() + " "
                + obj.toString());
    }

    protected void handleSlotCreated(MCREvent evt, Slot slot) {
        doNothing(evt, slot);
    }

    protected void handleSlotUpdated(MCREvent evt, Slot slot) {
        doNothing(evt, slot);
    }

    protected void handleSlotDeleted(MCREvent evt, Slot slot) {
        doNothing(evt, slot);
    }

    protected void handleSlotInactivate(MCREvent evt, Slot slot) {
        doNothing(evt, slot);
    }

    protected void handleSlotReactivate(MCREvent evt, Slot slot) {
        doNothing(evt, slot);
    }

    protected void handleSlotOwnerTransfer(MCREvent evt, Slot slot) {
        doNothing(evt, slot);
    }

    protected void handleEntryCreated(MCREvent evt, SlotEntry<?> entry) {
        doNothing(evt, entry);
    }

    protected void handleEntryUpdated(MCREvent evt, SlotEntry<?> entry) {
        doNothing(evt, entry);
    }

    protected void handleEntryDeleted(MCREvent evt, SlotEntry<?> entry) {
        doNothing(evt, entry);
    }

    protected void undoSlotCreated(MCREvent evt, Slot slot) {
        doNothing(evt, slot);
    }

    protected void undoSlotUpdated(MCREvent evt, Slot slot) {
        doNothing(evt, slot);
    }

    protected void undoSlotDeleted(MCREvent evt, Slot slot) {
        doNothing(evt, slot);
    }

    protected void undoEntryCreated(MCREvent evt, SlotEntry<?> entry) {
        doNothing(evt, entry);
    }

    protected void undoEntryUpdated(MCREvent evt, SlotEntry<?> entry) {
        doNothing(evt, entry);
    }

    protected void undoEntryDeleted(MCREvent evt, SlotEntry<?> entry) {
        doNothing(evt, entry);
    }

}

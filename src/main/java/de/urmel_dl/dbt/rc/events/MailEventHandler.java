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

import org.mycore.common.events.MCREvent;

import de.urmel_dl.dbt.common.MailQueue;
import de.urmel_dl.dbt.rc.datamodel.slot.Slot;
import de.urmel_dl.dbt.rc.datamodel.slot.SlotEntry;

/**
 * @author Ren\u00E9 Adler (eagle)
 *
 */
public class MailEventHandler extends EventHandlerBase {

    private static final String MAIL_STYLESHEET = "layout/dbt-email-layout:xslStyle:rc/mail-events";

    /* (non-Javadoc)
     * @see de.urmel_dl.dbt.rc.events.EventHandlerBase#handleSlotCreated(org.mycore.common.events.MCREvent, de.urmel_dl.dbt.rc.datamodel.slot.Slot)
     */
    @Override
    protected void handleSlotCreated(MCREvent evt, Slot slot) {
        handleEvent(evt, slot);
    }

    /* (non-Javadoc)
     * @see de.urmel_dl.dbt.rc.events.EventHandlerBase#handleSlotUpdated(org.mycore.common.events.MCREvent, de.urmel_dl.dbt.rc.datamodel.slot.Slot)
     */
    @Override
    protected void handleSlotUpdated(MCREvent evt, Slot slot) {
        handleEvent(evt, slot);
    }

    /* (non-Javadoc)
     * @see de.urmel_dl.dbt.rc.events.EventHandlerBase#handleSlotDeleted(org.mycore.common.events.MCREvent, de.urmel_dl.dbt.rc.datamodel.slot.Slot)
     */
    @Override
    protected void handleSlotDeleted(MCREvent evt, Slot slot) {
        handleEvent(evt, slot);
    }

    /* (non-Javadoc)
     * @see de.urmel_dl.dbt.rc.events.EventHandlerBase#handleSlotInactivate(org.mycore.common.events.MCREvent, de.urmel_dl.dbt.rc.datamodel.slot.Slot)
     */
    @Override
    protected void handleSlotInactivate(MCREvent evt, Slot slot) {
        handleEvent(evt, slot);
    }

    /* (non-Javadoc)
     * @see de.urmel_dl.dbt.rc.events.EventHandlerBase#handleSlotReactivate(org.mycore.common.events.MCREvent, de.urmel_dl.dbt.rc.datamodel.slot.Slot)
     */
    @Override
    protected void handleSlotReactivate(MCREvent evt, Slot slot) {
        handleEvent(evt, slot);
    }

    /* (non-Javadoc)
     * @see de.urmel_dl.dbt.rc.events.EventHandlerBase#handleSlotOwnerTransfer(org.mycore.common.events.MCREvent, de.urmel_dl.dbt.rc.datamodel.slot.Slot)
     */
    @Override
    protected void handleSlotOwnerTransfer(MCREvent evt, Slot slot) {
        handleEvent(evt, slot);
    }

    /* (non-Javadoc)
     * @see de.urmel_dl.dbt.rc.events.EventHandlerBase#handleEntryCreated(org.mycore.common.events.MCREvent, de.urmel_dl.dbt.rc.datamodel.slot.SlotEntry)
     */
    @Override
    protected void handleEntryCreated(MCREvent evt, SlotEntry<?> entry) {
        handleEvent(evt, entry);
    }

    /* (non-Javadoc)
     * @see de.urmel_dl.dbt.rc.events.EventHandlerBase#handleEntryUpdated(org.mycore.common.events.MCREvent, de.urmel_dl.dbt.rc.datamodel.slot.SlotEntry)
     */
    @Override
    protected void handleEntryUpdated(MCREvent evt, SlotEntry<?> entry) {
        handleEvent(evt, entry);
    }

    /* (non-Javadoc)
     * @see de.urmel_dl.dbt.rc.events.EventHandlerBase#handleEntryDeleted(org.mycore.common.events.MCREvent, de.urmel_dl.dbt.rc.datamodel.slot.SlotEntry)
     */
    @Override
    protected void handleEntryDeleted(MCREvent evt, SlotEntry<?> entry) {
        handleEvent(evt, entry);
    }

    private void handleEvent(MCREvent evt, Slot slot) {
        final StringBuilder uri = new StringBuilder();

        uri.append("xslStyle:" + MAIL_STYLESHEET);
        uri.append("?action=" + evt.getEventType());
        uri.append("&type=" + evt.getObjectType());
        uri.append("&slotId=" + slot.getSlotId());

        uri.append(":notnull:slot:");
        uri.append("slotId=" + slot.getSlotId());

        if (evt.getEventType().equals(MCREvent.DELETE_EVENT)) {
            final String rev = (String) evt.get("revision");
            if (rev != null && !rev.isEmpty())
                uri.append("&revision=" + rev);
        }

        MailQueue.addJob(uri.toString());
    }

    private void handleEvent(MCREvent evt, SlotEntry<?> entry) {
        final StringBuilder uri = new StringBuilder();

        uri.append("xslStyle:" + MAIL_STYLESHEET);
        uri.append("?action=" + evt.getEventType());
        uri.append("&type=" + evt.getObjectType());
        uri.append("&slotId=" + evt.get("slotId"));
        uri.append("&entryId=" + entry.getId());

        uri.append(":notnull:slot:");
        uri.append("slotId=" + evt.get("slotId"));
        uri.append("&entryId=" + entry.getId());

        if (evt.getEventType().equals(MCREvent.DELETE_EVENT)) {
            final String rev = (String) evt.get("revision");
            if (rev != null && !rev.isEmpty())
                uri.append("&revision=" + rev);
        }

        MailQueue.addJob(uri.toString());
    }
}

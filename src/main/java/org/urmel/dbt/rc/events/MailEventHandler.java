/*
 * $Id$ 
 * $Revision$ $Date$
 *
 * This file is part of ***  M y C o R e  ***
 * See http://www.mycore.de/ for details.
 *
 * This program is free software; you can use it, redistribute it
 * and / or modify it under the terms of the GNU General Public License
 * (GPL) as published by the Free Software Foundation; either version 2
 * of the License or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program, in a file called gpl.txt or license.txt.
 * If not, write to the Free Software Foundation Inc.,
 * 59 Temple Place - Suite 330, Boston, MA  02111-1307 USA
 */
package org.urmel.dbt.rc.events;

import org.mycore.common.events.MCREvent;
import org.urmel.dbt.common.MailQueue;
import org.urmel.dbt.rc.datamodel.slot.Slot;
import org.urmel.dbt.rc.datamodel.slot.SlotEntry;

/**
 * @author Ren\u00E9 Adler (eagle)
 *
 */
public class MailEventHandler extends EventHandlerBase {

    private static final String MAIL_STYLESHEET = "rc/mail-events";

    /* (non-Javadoc)
     * @see org.urmel.dbt.rc.events.EventHandlerBase#handleSlotCreated(org.mycore.common.events.MCREvent, org.urmel.dbt.rc.datamodel.slot.Slot)
     */
    @Override
    protected void handleSlotCreated(MCREvent evt, Slot slot) {
        handleEvent(evt, slot);
    }

    /* (non-Javadoc)
     * @see org.urmel.dbt.rc.events.EventHandlerBase#handleSlotUpdated(org.mycore.common.events.MCREvent, org.urmel.dbt.rc.datamodel.slot.Slot)
     */
    @Override
    protected void handleSlotUpdated(MCREvent evt, Slot slot) {
        handleEvent(evt, slot);
    }

    /* (non-Javadoc)
     * @see org.urmel.dbt.rc.events.EventHandlerBase#handleSlotDeleted(org.mycore.common.events.MCREvent, org.urmel.dbt.rc.datamodel.slot.Slot)
     */
    @Override
    protected void handleSlotDeleted(MCREvent evt, Slot slot) {
        handleEvent(evt, slot);
    }

    /* (non-Javadoc)
     * @see org.urmel.dbt.rc.events.EventHandlerBase#handleEntryCreated(org.mycore.common.events.MCREvent, org.urmel.dbt.rc.datamodel.slot.SlotEntry)
     */
    @Override
    protected void handleEntryCreated(MCREvent evt, SlotEntry<?> entry) {
        handleEvent(evt, entry);
    }

    /* (non-Javadoc)
     * @see org.urmel.dbt.rc.events.EventHandlerBase#handleEntryUpdated(org.mycore.common.events.MCREvent, org.urmel.dbt.rc.datamodel.slot.SlotEntry)
     */
    @Override
    protected void handleEntryUpdated(MCREvent evt, SlotEntry<?> entry) {
        handleEvent(evt, entry);
    }

    /* (non-Javadoc)
     * @see org.urmel.dbt.rc.events.EventHandlerBase#handleEntryDeleted(org.mycore.common.events.MCREvent, org.urmel.dbt.rc.datamodel.slot.SlotEntry)
     */
    @Override
    protected void handleEntryDeleted(MCREvent evt, SlotEntry<?> entry) {
        handleEvent(evt, entry);
    }

    private void handleEvent(MCREvent evt, Slot slot) {
        final StringBuffer uri = new StringBuffer();

        uri.append("xslStyle:" + MAIL_STYLESHEET);
        uri.append("?action=" + evt.getEventType());
        uri.append("&type=" + evt.getObjectType());
        uri.append("&slotId=" + slot.getId());

        uri.append(":slot:");
        uri.append("slotId=" + evt.get("slotId"));

        MailQueue.addJob(uri.toString());
    }

    private void handleEvent(MCREvent evt, SlotEntry<?> entry) {
        final StringBuffer uri = new StringBuffer();

        uri.append("xslStyle:" + MAIL_STYLESHEET);
        uri.append("?action=" + evt.getEventType());
        uri.append("&type=" + evt.getObjectType());
        uri.append("&slotId=" + evt.get("slotId"));
        uri.append("&entryId=" + entry.getId());

        uri.append(":slot:");
        uri.append("slotId=" + evt.get("slotId"));
        uri.append("&entryId=" + entry.getId());

        MailQueue.addJob(uri.toString());
    }
}

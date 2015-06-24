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

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.mycore.common.MCRMailer;
import org.mycore.common.content.MCRContent;
import org.mycore.common.content.MCRJDOMContent;
import org.mycore.common.events.MCREvent;
import org.urmel.dbt.rc.datamodel.slot.SlotEntry;
import org.urmel.dbt.rc.utils.SlotEntryTransformer;

/**
 * @author Ren\u00E9 Adler (eagle)
 *
 */
public class MailEventHandler extends EventHandlerBase {

    private static final Logger LOGGER = Logger.getLogger(MailEventHandler.class);

    private static final String MAIL_STYLESHEET = "rc/mail-events";

    /* (non-Javadoc)
     * @see org.urmel.dbt.rc.events.EventHandlerBase#handleEntryCreated(org.mycore.common.events.MCREvent, org.urmel.dbt.rc.datamodel.slot.SlotEntry)
     */
    @Override
    protected void handleEntryCreated(MCREvent evt, SlotEntry<?> entry) {
        MCRContent xml = new MCRJDOMContent(SlotEntryTransformer.buildExportableXML(entry));
        handleEvent(evt, xml);
    }

    /* (non-Javadoc)
     * @see org.urmel.dbt.rc.events.EventHandlerBase#handleEntryUpdated(org.mycore.common.events.MCREvent, org.urmel.dbt.rc.datamodel.slot.SlotEntry)
     */
    @Override
    protected void handleEntryUpdated(MCREvent evt, SlotEntry<?> entry) {
        MCRContent xml = new MCRJDOMContent(SlotEntryTransformer.buildExportableXML(entry));
        handleEvent(evt, xml);
    }

    /* (non-Javadoc)
     * @see org.urmel.dbt.rc.events.EventHandlerBase#handleEntryDeleted(org.mycore.common.events.MCREvent, org.urmel.dbt.rc.datamodel.slot.SlotEntry)
     */
    @Override
    protected void handleEntryDeleted(MCREvent evt, SlotEntry<?> entry) {
        MCRContent xml = new MCRJDOMContent(SlotEntryTransformer.buildExportableXML(entry));
        handleEvent(evt, xml);
    }

    private void sendNotificationMail(MCREvent evt, MCRContent doc) throws Exception {
        LOGGER.info("Preparing mail for: " + doc.getSystemId());
        HashMap<String, String> parameters = new HashMap<String, String>();
        for (Map.Entry<String, Object> entry : evt.entrySet()) {
            parameters.put(entry.getKey(), entry.getValue().toString());
        }
        parameters.put("action", evt.getEventType());
        parameters.put("type", evt.getObjectType());

        MCRMailer.sendMail(doc.asXML(), MAIL_STYLESHEET, parameters);
    }

    private void handleEvent(MCREvent evt, MCRContent xml) {
        try {
            sendNotificationMail(evt, xml);
        } catch (Exception e) {
            LOGGER.error("Error while handling event: " + evt, e);
        }
    }
}

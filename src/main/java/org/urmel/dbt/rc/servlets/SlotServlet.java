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
package org.urmel.dbt.rc.servlets;

import org.apache.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.XMLOutputter;
import org.mycore.frontend.MCRFrontendUtil;
import org.mycore.frontend.servlets.MCRServlet;
import org.mycore.frontend.servlets.MCRServletJob;
import org.urmel.dbt.rc.datamodel.Slot;
import org.urmel.dbt.rc.datamodel.SlotEntry;
import org.urmel.dbt.rc.persistency.SlotManager;
import org.urmel.dbt.rc.utils.SlotEntryTransformer;

/**
 * @author Ren\u00E9 Adler (eagle)
 *
 */
public class SlotServlet extends MCRServlet {

    private static final long serialVersionUID = -3138681111200495882L;

    private static final Logger LOGGER = Logger.getLogger(SlotServlet.class);

    private static final SlotManager SLOT_MGR = SlotManager.instance();

    public void doGetPost(final MCRServletJob job) throws Exception {
        final Document doc = (Document) (job.getRequest().getAttribute("MCRXEditorSubmission"));
        if (doc != null) {
            final Element xml = doc.getRootElement();

            LOGGER.info(new XMLOutputter().outputString(xml));

            final String slotId = job.getRequest().getParameter("slotId");
            final String afterId = job.getRequest().getParameter("afterId");

            if (slotId != null) {
                final Slot slot = SLOT_MGR.getSlotById(slotId);

                final SlotEntry<?> slotEntry = SlotEntryTransformer.buildSlotEntry(xml);

                if (slot.getEntries() == null) {
                    LOGGER.debug("Add new entry: " + slotEntry);
                    slot.addEntry(slotEntry);
                } else {
                    final SlotEntry<?> se = slot.getEntryById(slotEntry.getId());
                    if (se != null) {
                        LOGGER.debug("Update entry: " + slotEntry);
                        slot.setEntry(slotEntry);
                    } else {
                        LOGGER.info("Add new entry after \"" + afterId + "\".");
                        slot.addEntry(slotEntry, afterId);
                    }
                }

                SLOT_MGR.saveOrUpdate(slot);

                job.getResponse()
                        .sendRedirect(
                                MCRFrontendUtil.getBaseURL() + "rc/" + slot.getSlotId() + "?XSL.Mode=edit#"
                                        + slotEntry.getId());
            }
        }
    }
}

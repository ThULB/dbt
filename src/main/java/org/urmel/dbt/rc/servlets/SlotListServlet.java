/*
 * $Id$ 
 * $Revision$
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

import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.XMLOutputter;
import org.mycore.access.MCRAccessManager;
import org.mycore.common.content.MCRJDOMContent;
import org.mycore.common.events.MCREvent;
import org.mycore.common.events.MCREventManager;
import org.mycore.datamodel.classifications2.MCRCategory;
import org.mycore.datamodel.classifications2.MCRCategoryDAO;
import org.mycore.datamodel.classifications2.MCRCategoryID;
import org.mycore.datamodel.classifications2.impl.MCRCategoryDAOImpl;
import org.mycore.frontend.MCRFrontendUtil;
import org.mycore.frontend.servlets.MCRServlet;
import org.mycore.frontend.servlets.MCRServletJob;
import org.mycore.mir.authorization.accesskeys.MIRAccessKeyManager;
import org.urmel.dbt.rc.datamodel.Attendee;
import org.urmel.dbt.rc.datamodel.RCCalendar;
import org.urmel.dbt.rc.datamodel.Status;
import org.urmel.dbt.rc.datamodel.slot.Slot;
import org.urmel.dbt.rc.persistency.SlotManager;
import org.urmel.dbt.rc.utils.AttendeeTransformer;
import org.urmel.dbt.rc.utils.SlotListTransformer;
import org.urmel.dbt.rc.utils.SlotTransformer;

/**
 * @author René Adler (eagle)
 *
 */
public class SlotListServlet extends MCRServlet {

    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = LogManager.getLogger(SlotListServlet.class);

    private static final SlotManager SLOT_MGR = SlotManager.instance();

    private static final MCRCategoryDAO DAO = new MCRCategoryDAOImpl();

    public void doGetPost(final MCRServletJob job) throws Exception {
        final HttpServletRequest req = job.getRequest();

        final Document doc = (Document) (job.getRequest().getAttribute("MCRXEditorSubmission"));

        if (doc != null) {
            final Element xml = doc.getRootElement();

            LOGGER.debug(new XMLOutputter().outputString(xml));

            final Slot slot = SlotTransformer.buildSlot(xml);

            final String action = req.getParameter("action");
            final String slotId = xml.getAttributeValue("id");
            final String location = xml.getChild("location") != null ? xml.getChild("location").getAttributeValue("id")
                    : null;

            MCREvent evt = null;

            if (location != null) {
                final MCRCategory category = DAO.getCategory(new MCRCategoryID(Slot.CLASSIF_ROOT_LOCATION, location),
                        0);
                slot.setLocation(category.getId());
            }

            if (slotId == null) {
                if (!MCRAccessManager.checkPermission(SlotManager.POOLPRIVILEGE_CREATE_SLOT)) {
                    job.getResponse().sendError(HttpServletResponse.SC_FORBIDDEN);
                    return;
                }

                SLOT_MGR.addSlot(slot);

                evt = new MCREvent(SlotManager.SLOT_TYPE, MCREvent.CREATE_EVENT);
                evt.put(SlotManager.SLOT_TYPE, slot);
            } else {
                final Slot s = SLOT_MGR.getSlotById(slotId);

                if (s.getMCRObjectID() != null
                        && !SlotManager.checkPermission(s.getMCRObjectID(), MCRAccessManager.PERMISSION_WRITE)) {
                    job.getResponse().sendError(HttpServletResponse.SC_FORBIDDEN);
                    return;
                }

                slot.setMCRObjectID(s.getMCRObjectID());

                if (s.getStatus() != slot.getStatus() && slot.getStatus() == Status.ARCHIVED) {
                    evt = new MCREvent(SlotManager.SLOT_TYPE, SlotManager.INACTIVATE_EVENT);
                } else if ("reactivateComplete".equals(action)) {
                    evt = new MCREvent(SlotManager.SLOT_TYPE, SlotManager.REACTIVATE_EVENT);
                    slot.setValidTo(
                            RCCalendar.getPeriodBySetable(slot.getLocation().toString(), new Date()).getToDate());
                } else
                    evt = new MCREvent(SlotManager.SLOT_TYPE, MCREvent.UPDATE_EVENT);

                // remove warning dates on new validTo date
                if (s.getValidToAsDate().before(slot.getValidToAsDate())) {
                    slot.setWarningDates(null);
                }

                SLOT_MGR.setSlot(slot);

                evt.put(SlotManager.SLOT_TYPE, slot);
            }

            SLOT_MGR.saveOrUpdate(slot);

            if (evt != null) {
                MCREventManager.instance().handleEvent(evt);
            }

            if (slot.getWriteKey() != null && !SlotManager.hasAdminPermission()) {
                MIRAccessKeyManager.addAccessKey(slot.getMCRObjectID(), slot.getWriteKey());
            }

            String redirectURL = job.getRequest().getParameter("url");
            if (redirectURL == null || redirectURL.length() == 0) {
                redirectURL = MCRFrontendUtil.getBaseURL() + "rc/" + slot.getSlotId();
            }

            job.getResponse().sendRedirect(redirectURL);
        } else {
            final String path = req.getPathInfo();

            if (path != null) {
                final StringTokenizer st = new StringTokenizer(path, "/");

                final String slotId = st.nextToken();
                final String option = st.hasMoreTokens() ? st.nextToken() : null;
                final Slot slot = SLOT_MGR.getSlotById(slotId);

                if (option != null) {
                    if ("attendees".equals(option) && SlotManager.hasAdminPermission()
                            || SlotManager.isOwner(slot.getMCRObjectID().toString())) {
                        List<Attendee> attendees = SLOT_MGR.getAttendees(slot);

                        getLayoutService().doLayout(job.getRequest(), job.getResponse(),
                                new MCRJDOMContent(AttendeeTransformer.buildExportableXML(slotId, attendees)));
                        return;
                    }
                }

                if (!SlotManager.checkPermission(slot.getMCRObjectID(), MCRAccessManager.PERMISSION_READ)
                        && !SlotManager.checkPermission(slot.getMCRObjectID(), MCRAccessManager.PERMISSION_WRITE)) {
                    getLayoutService().doLayout(job.getRequest(), job.getResponse(),
                            new MCRJDOMContent(SlotTransformer.buildExportableXML(slot.getBasicCopy())));
                    return;
                }

                getLayoutService().doLayout(job.getRequest(), job.getResponse(),
                        new MCRJDOMContent(SlotTransformer.buildExportableXML(slot)));
                return;
            }

            getLayoutService().doLayout(job.getRequest(), job.getResponse(),
                    new MCRJDOMContent(SlotListTransformer.buildExportableXML(SlotManager.hasAdminPermission()
                            ? SLOT_MGR.getBasicSlotList() : SLOT_MGR.getActiveSlotList())));
        }
    }
}

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
package de.urmel_dl.dbt.rc.servlets;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.apache.solr.client.solrj.SolrQuery.SortClause;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.XMLOutputter;
import org.mycore.access.MCRAccessManager;
import org.mycore.common.MCRException;
import org.mycore.common.MCRSessionMgr;
import org.mycore.common.MCRUserInformation;
import org.mycore.common.content.MCRJDOMContent;
import org.mycore.common.events.MCREvent;
import org.mycore.common.events.MCREventManager;
import org.mycore.datamodel.classifications2.MCRCategoryDAO;
import org.mycore.datamodel.classifications2.MCRCategoryID;
import org.mycore.datamodel.classifications2.impl.MCRCategoryDAOImpl;
import org.mycore.frontend.MCRFrontendUtil;
import org.mycore.frontend.servlets.MCRServlet;
import org.mycore.frontend.servlets.MCRServletJob;
import org.mycore.mir.authorization.accesskeys.MIRAccessKeyManager;

import de.urmel_dl.dbt.rc.datamodel.Attendee.Attendees;
import de.urmel_dl.dbt.rc.datamodel.PendingStatus;
import de.urmel_dl.dbt.rc.datamodel.RCCalendar;
import de.urmel_dl.dbt.rc.datamodel.Status;
import de.urmel_dl.dbt.rc.datamodel.slot.Slot;
import de.urmel_dl.dbt.rc.datamodel.slot.SlotList;
import de.urmel_dl.dbt.rc.persistency.SlotManager;
import de.urmel_dl.dbt.utils.EntityFactory;

/**
 * @author René Adler (eagle)
 *
 */
public class SlotListServlet extends MCRServlet {

    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = LogManager.getLogger(SlotListServlet.class);

    private static final SlotManager SLOT_MGR = SlotManager.instance();

    private static final MCRCategoryDAO DAO = new MCRCategoryDAOImpl();

    @Override
    public void doGetPost(final MCRServletJob job) throws Exception {
        final HttpServletRequest req = job.getRequest();

        final Document doc = (Document) (job.getRequest().getAttribute("MCRXEditorSubmission"));

        if (doc != null) {
            final Element xml = doc.getRootElement();

            LOGGER.debug(new XMLOutputter().outputString(xml));

            final Slot slot = new EntityFactory<>(Slot.class).fromElement(xml);

            final String action = req.getParameter("action");
            final String slotId = xml.getAttributeValue("id");
            final String location = xml.getChild("location") != null ? xml.getChild("location").getAttributeValue("id")
                : null;
            final String nId = xml.getChild("location") != null
                ? xml.getChild("location").getAttributeValue("newId") : null;
            final Integer newId = location != null && nId != null ? new Integer(nId) : null;

            MCREvent evt = null;

            if (slotId == null) {
                if (!MCRAccessManager.checkPermission(SlotManager.POOLPRIVILEGE_CREATE_SLOT)) {
                    job.getResponse().sendError(HttpServletResponse.SC_FORBIDDEN);
                    return;
                }

                slot.setLocation(getLocationId(location));

                SLOT_MGR.addSlot(slot);

                evt = new MCREvent(SlotManager.SLOT_TYPE, MCREvent.CREATE_EVENT);
                evt.put(SlotManager.SLOT_TYPE, slot);
            } else {
                final Slot s = SLOT_MGR.getSlotById(slotId);

                if (s.getMCRObjectID() != null
                    && !MCRAccessManager.checkPermission(s.getMCRObjectID(), MCRAccessManager.PERMISSION_WRITE)) {
                    job.getResponse().sendError(HttpServletResponse.SC_FORBIDDEN);
                    return;
                }

                slot.setMCRObjectID(s.getMCRObjectID());

                if (s.getStatus() != slot.getStatus() && slot.getStatus() == Status.ARCHIVED) {
                    evt = new MCREvent(SlotManager.SLOT_TYPE, SlotManager.INACTIVATE_EVENT);
                } else if (slot.getPendingStatus() == PendingStatus.OWNERTRANSFER
                    && s.getPendingStatus() != slot.getPendingStatus()) {

                    if (!MCRAccessManager.checkPermission(SlotManager.POOLPRIVILEGE_ADMINISTRATE_SLOT)) {
                        job.getResponse().sendError(HttpServletResponse.SC_FORBIDDEN);
                        return;
                    }

                    evt = new MCREvent(SlotManager.SLOT_TYPE, SlotManager.OWNER_TRANSFER_EVENT);

                    // rebuild new keys
                    String readKey = SlotManager.buildKey();
                    String writeKey = null;
                    // rebuild write key if match with read key
                    while ((writeKey = SlotManager.buildKey()).equals(readKey)) {
                        ;
                    }

                    slot.setReadKey(readKey);
                    slot.setWriteKey(writeKey);
                } else if ("reactivateComplete".equals(action)) {
                    evt = new MCREvent(SlotManager.SLOT_TYPE, SlotManager.REACTIVATE_EVENT);
                    slot.setValidTo(
                        RCCalendar.getPeriodBySetable(slot.getLocation().toString(), new Date()).getToDate());
                } else {
                    evt = new MCREvent(SlotManager.SLOT_TYPE, MCREvent.UPDATE_EVENT);
                }

                // remove warning dates on new validTo date
                if (s.getValidToAsDate().before(slot.getValidToAsDate())) {
                    slot.setWarningDates(null);
                }

                if (location != null && newId != null
                    && (!location.equals(s.getLocation().getID()) || newId != slot.getId())) {

                    if (!MCRAccessManager.checkPermission(SlotManager.POOLPRIVILEGE_ADMINISTRATE_SLOT)) {
                        job.getResponse().sendError(HttpServletResponse.SC_FORBIDDEN);
                        return;
                    }

                    if (SLOT_MGR.isFreeId(getLocationId(location), newId)) {
                        slot.setLocation(getLocationId(location));
                        slot.setId(newId);

                        SLOT_MGR.removeSlot(s);
                        SLOT_MGR.addSlot(slot);
                    } else {
                        throw new MCRException("Couldn't change slot location, because slot number not free.");
                    }
                } else {
                    SLOT_MGR.setSlot(slot);
                }

                evt.put(SlotManager.SLOT_TYPE, slot);
            }

            SLOT_MGR.saveOrUpdate(slot);

            if (evt != null) {
                MCREventManager.instance().handleEvent(evt);
            }

            if ("ownerTransfer".equals(action)) {
                SlotManager.setOwner(slot.getMCRObjectID().toString());
            }

            if (slot.getWriteKey() != null
                && !MCRAccessManager.checkPermission(SlotManager.POOLPRIVILEGE_ADMINISTRATE_SLOT)) {
                MIRAccessKeyManager.addAccessKey(slot.getMCRObjectID(), slot.getWriteKey());
            }

            String redirectURL = job.getRequest().getParameter("url");
            if (redirectURL == null || redirectURL.length() == 0) {
                redirectURL = MCRFrontendUtil.getBaseURL() + "rc/" + slot.getSlotId();
            } else {
                // fix changed slotId
                redirectURL = redirectURL.replaceAll(slotId, slot.getSlotId());
            }

            job.getResponse().sendRedirect(redirectURL);
        } else {
            final String path = req.getPathInfo();

            if (path != null) {
                final StringTokenizer st = new StringTokenizer(path, "/");

                final String slotId = st.nextToken();
                final String option = st.hasMoreTokens() ? st.nextToken() : null;
                final Slot slot = SLOT_MGR.getSlotById(slotId);

                if (option != null && ("attendees".equals(option)
                    && MCRAccessManager.checkPermission(SlotManager.POOLPRIVILEGE_ADMINISTRATE_SLOT)
                    || SlotManager.isOwner(slot.getMCRObjectID().toString()))) {
                    Attendees attendees = SLOT_MGR.getAttendees(slot);

                    getLayoutService().doLayout(job.getRequest(), job.getResponse(),
                        new MCRJDOMContent(new EntityFactory<>(attendees).toDocument()));
                    return;
                }

                if (!MCRAccessManager.checkPermission(slot.getMCRObjectID(), MCRAccessManager.PERMISSION_READ)
                    && !MCRAccessManager.checkPermission(slot.getMCRObjectID(),
                        MCRAccessManager.PERMISSION_WRITE)) {
                    getLayoutService().doLayout(job.getRequest(), job.getResponse(),
                        new MCRJDOMContent(new EntityFactory<>(slot.getBasicCopy()).toDocument()));
                    return;
                }

                getLayoutService().doLayout(job.getRequest(), job.getResponse(),
                    new MCRJDOMContent(new EntityFactory<>(slot).toDocument()));
                return;
            }

            final String filter = req.getParameter("Filter");
            final String page = req.getParameter("Page");
            final String numPerPage = req.getParameter("numPerPage");
            final String sortBy = req.getParameter("SortBy");
            final String sortOrder = req.getParameter("SortOrder");

            final Integer start = page != null && numPerPage != null
                ? (Integer.parseInt(page) - 1) * Integer.parseInt(numPerPage) : 0;
            final Integer rows = numPerPage != null ? Integer.parseInt(numPerPage) : null;

            final MCRUserInformation currentUser = MCRSessionMgr.getCurrentSession().getUserInformation();

            final List<SortClause> sortClauses = new ArrayList<>();
            sortClauses.add(new SortClause("if(exists(query({!v='createdby:" + currentUser.getUserID() + "'})),100,0)",
                "desc"));
            if (sortBy != null && !sortBy.isEmpty() && sortOrder != null && !sortOrder.isEmpty()) {
                sortClauses.add(new SortClause(sortBy, ORDER.valueOf(sortOrder)));
            } else {
                sortClauses.add(new SortClause("slot.lecturers", ORDER.asc));
            }

            final SlotList slotList = SLOT_MGR.getFilteredSlotList(filter,
                !MCRAccessManager.checkPermission(SlotManager.POOLPRIVILEGE_ADMINISTRATE_SLOT)
                    && !MCRAccessManager.checkPermission(SlotManager.POOLPRIVILEGE_EDIT_SLOT)
                        ? "slot.status:active or slot.status:pending or createdby:"
                            + currentUser.getUserID()
                        : null,
                start, rows, sortClauses);

            getLayoutService().doLayout(job.getRequest(), job.getResponse(),
                new MCRJDOMContent(new EntityFactory<>(slotList.getBasicSlots()).toDocument()));
        }

    }

    private MCRCategoryID getLocationId(final String location) {
        return DAO.getCategory(new MCRCategoryID(Slot.CLASSIF_ROOT_LOCATION, location), 0).getId();
    }
}

/*
 * $Id: SlotListServlet.java 2116 2014-10-01 12:14:43Z adler $ 
 */
package org.urmel.dbt.rc.servlets;

import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.XMLOutputter;
import org.mycore.access.MCRAccessManager;
import org.mycore.common.content.MCRJDOMContent;
import org.mycore.datamodel.classifications2.MCRCategory;
import org.mycore.datamodel.classifications2.MCRCategoryDAO;
import org.mycore.datamodel.classifications2.MCRCategoryID;
import org.mycore.datamodel.classifications2.impl.MCRCategoryDAOImpl;
import org.mycore.frontend.MCRFrontendUtil;
import org.mycore.frontend.servlets.MCRServlet;
import org.mycore.frontend.servlets.MCRServletJob;
import org.urmel.dbt.rc.datamodel.slot.Slot;
import org.urmel.dbt.rc.persistency.SlotManager;
import org.urmel.dbt.rc.utils.SlotListTransformer;
import org.urmel.dbt.rc.utils.SlotTransformer;

/**
 * @author René Adler (eagle)
 *
 */
public class SlotListServlet extends MCRServlet {

    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = Logger.getLogger(SlotListServlet.class);

    private static final SlotManager SLOT_MGR = SlotManager.instance();

    private static final MCRCategoryDAO DAO = new MCRCategoryDAOImpl();

    public void doGetPost(final MCRServletJob job) throws Exception {
        final Document doc = (Document) (job.getRequest().getAttribute("MCRXEditorSubmission"));
        if (doc != null) {
            final Element xml = doc.getRootElement();

            LOGGER.debug(new XMLOutputter().outputString(xml));

            final Slot slot = SlotTransformer.buildSlot(xml);

            final String slotId = xml.getAttributeValue("id");
            final String location = xml.getChild("location") != null ? xml.getChild("location").getAttributeValue("id")
                    : null;

            if (location != null) {
                final MCRCategory category = DAO
                        .getCategory(new MCRCategoryID(Slot.CLASSIF_ROOT_LOCATION, location), 0);
                slot.setLocation(category.getId());
            }

            if (slotId == null) {
                if (!MCRAccessManager.checkPermission(SlotManager.POOLPRIVILEGE_CREATE_SLOT)) {
                    job.getResponse().sendError(HttpServletResponse.SC_FORBIDDEN);
                    return;
                }

                SLOT_MGR.addSlot(slot);
            } else {
                final Slot s = SLOT_MGR.getSlotById(slotId);

                if (s.getMCRObjectID() != null
                        && !SlotManager.checkPermission(s.getMCRObjectID(), MCRAccessManager.PERMISSION_WRITE)) {
                    job.getResponse().sendError(HttpServletResponse.SC_FORBIDDEN);
                    return;
                }

                s.setTitle(slot.getTitle());
                s.setLecturers(slot.getLecturers());
                s.setOnlineOnly(slot.isOnlineOnly());
                s.setStatus(slot.getStatus());
                s.setPendingStatus(slot.getPendingStatus());
                s.setValidTo(slot.getValidToAsDate());
                s.setComment(slot.getComment());

                if (slot.getReadKey() != null)
                    s.setReadKey(slot.getReadKey());
                if (slot.getWriteKey() != null)
                    s.setWriteKey(slot.getWriteKey());

                slot.setMCRObjectID(s.getMCRObjectID());
            }

            SLOT_MGR.saveList();

            job.getResponse().sendRedirect(MCRFrontendUtil.getBaseURL() + "rc/" + slot.getSlotId());
        } else {
            final HttpServletRequest req = job.getRequest();

            final String path = req.getPathInfo();

            if (path != null) {
                final StringTokenizer st = new StringTokenizer(path, "/");

                final String slotId = st.nextToken();
                final Slot slot = SLOT_MGR.getSlotById(slotId);

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
                    new MCRJDOMContent(SlotListTransformer.buildExportableXML(SLOT_MGR.getActiveSlotList())));
        }
    }
}

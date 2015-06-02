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

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.apache.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.XMLOutputter;
import org.mycore.access.MCRAccessManager;
import org.mycore.common.content.MCRContent;
import org.mycore.frontend.MCRFrontendUtil;
import org.mycore.frontend.servlets.MCRServlet;
import org.mycore.frontend.servlets.MCRServletJob;
import org.urmel.dbt.opc.datamodel.Catalog;
import org.urmel.dbt.opc.datamodel.Catalogues;
import org.urmel.dbt.rc.datamodel.slot.Slot;
import org.urmel.dbt.rc.datamodel.slot.SlotEntry;
import org.urmel.dbt.rc.datamodel.slot.entries.FileEntry;
import org.urmel.dbt.rc.persistency.FileEntryManager;
import org.urmel.dbt.rc.persistency.SlotManager;
import org.urmel.dbt.rc.utils.SlotEntryTransformer;

/**
 * @author Ren\u00E9 Adler (eagle)
 *
 */
@MultipartConfig
public class SlotServlet extends MCRServlet {

    private static final long serialVersionUID = -3138681111200495882L;

    private static final Logger LOGGER = Logger.getLogger(SlotServlet.class);

    private static final SlotManager SLOT_MGR = SlotManager.instance();

    @SuppressWarnings("unchecked")
    public void doGetPost(final MCRServletJob job) throws Exception {
        final HttpServletRequest req = job.getRequest();
        final HttpServletResponse res = job.getResponse();

        // checks path and return the file content.
        final String path = req.getPathInfo();

        if (path != null) {
            final StringTokenizer st = new StringTokenizer(path, "/");

            final String slotId = st.nextToken();
            final String entryId = st.nextToken();
            final String fileName = st.nextToken();

            if (slotId != null && entryId != null && fileName != null) {
                final Slot slot = SLOT_MGR.getSlotById(slotId);

                if (!MCRAccessManager.checkPermission(slot.getMCRObjectID(), MCRAccessManager.PERMISSION_READ)
                        || !MCRAccessManager.checkPermission(slot.getMCRObjectID(), MCRAccessManager.PERMISSION_WRITE)) {
                    res.sendError(HttpServletResponse.SC_FORBIDDEN);
                    return;
                }

                final SlotEntry<FileEntry> slotEntry = (SlotEntry<FileEntry>) slot.getEntryById(entryId);

                if (slotEntry != null) {
                    final FileEntry fileEntry = (FileEntry) slotEntry.getEntry();
                    if (fileEntry != null && fileName.equals(fileEntry.getName())) {
                        MCRContent content = FileEntryManager.retrieve(slot, slotEntry);
                        content.sendTo(job.getResponse().getOutputStream());
                        return;
                    }
                }
            }

            job.getResponse().sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // edit slot entries
        final String action = getParameter(req, "action");
        final String slotId = getParameter(req, "slotId");
        final String afterId = getParameter(req, "afterId");

        Element xml = null;
        final Document doc = (Document) (req.getAttribute("MCRXEditorSubmission"));
        if (doc != null) {
            xml = doc.getRootElement();
            LOGGER.debug(new XMLOutputter().outputString(xml));
        }

        if (slotId != null) {
            final Slot slot = SLOT_MGR.getSlotById(slotId);

            if (!MCRAccessManager.checkPermission(slot.getMCRObjectID(), MCRAccessManager.PERMISSION_WRITE)) {
                res.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }

            final Element firstChild = xml != null && xml.getChildren().size() > 0 ? xml.getChildren().get(0) : null;

            if (firstChild != null && "search".equals(firstChild.getName())) {
                final String catalogId = req.getParameter("catalogId");
                final Catalog catalog = Catalogues.instance().getCatalogById(catalogId);

                final Map<String, String> params = new HashMap<String, String>();
                params.put("slotId", slotId);
                params.put("afterId", afterId);

                job.getResponse()
                        .sendRedirect(
                                MCRFrontendUtil.getBaseURL()
                                        + "opc/"
                                        + (catalog != null && catalog.getISIL() != null && catalog.getISIL().size() > 0 ? catalog
                                                .getISIL().get(0) : catalogId) + "/search/" + firstChild.getTextTrim()
                                        + toQueryString(params, true));
            } else {
                SlotEntry<?> slotEntry = xml != null ? SlotEntryTransformer.buildSlotEntry(xml) : null;

                boolean success = true;

                if (slotEntry == null && "upload".equals(action)) {
                    if (getParameter(req, "cancel") != null) {
                        job.getResponse().sendRedirect(
                                MCRFrontendUtil.getBaseURL() + "rc/" + slot.getSlotId() + "?XSL.Mode=edit");
                        return;
                    }

                    final Part filePart = req.getPart("file");
                    final String fileName = getFilename(filePart);

                    slotEntry = new SlotEntry<FileEntry>();

                    final FileEntry fe = new FileEntry();
                    fe.setName(fileName);
                    fe.setContent(filePart.getInputStream());
                    fe.setComment(getParameter(req, "comment"));

                    ((SlotEntry<FileEntry>) slotEntry).setEntry(fe);
                }

                if ("delete".equals(action)) {
                    final SlotEntry<?> se = slot.getEntryById(slotEntry.getId());
                    if (se != null) {
                        LOGGER.debug("Remove entry: " + se);
                        success = slot.removeEntry(se);
                    }
                } else if (slot.getEntries() == null) {
                    LOGGER.debug("Add new entry: " + slotEntry);
                    success = slot.addEntry(slotEntry);
                } else {
                    final SlotEntry<?> se = slot.getEntryById(slotEntry.getId());
                    if (se != null) {
                        LOGGER.debug("Update entry: " + slotEntry);
                        slot.setEntry(slotEntry);
                    } else {
                        LOGGER.debug("Add new entry after \"" + afterId + "\".");
                        success = slot.addEntry(slotEntry, afterId);
                    }
                }

                if (success)
                    SLOT_MGR.saveOrUpdate(slot);

                job.getResponse()
                        .sendRedirect(
                                MCRFrontendUtil.getBaseURL() + "rc/" + slot.getSlotId() + "?XSL.Mode=edit#"
                                        + slotEntry.getId());
            }
        }
    }

    private static String toQueryString(final Map<String, String> parameters, final boolean withXSLPrefix) {
        StringBuffer queryStr = new StringBuffer();
        for (String name : parameters.keySet()) {
            if (parameters.get(name) != null) {
                if (queryStr.length() > 0) {
                    queryStr.append("&");
                }
                queryStr.append((withXSLPrefix ? "XSL." : "") + name + "=" + parameters.get(name));
            }
        }
        return queryStr.toString().length() > 0 ? "?" + queryStr.toString() : queryStr.toString();
    }

    private static String getFilename(final Part part) {
        for (String cd : part.getHeader("content-disposition").split(";")) {
            if (cd.trim().startsWith("filename")) {
                final String filename = cd.substring(cd.indexOf('=') + 1).trim().replace("\"", "");
                return filename.substring(filename.lastIndexOf('/') + 1).substring(filename.lastIndexOf('\\') + 1); // MSIE fix.
            }
        }
        return null;
    }

    private static String getParameter(final HttpServletRequest req, final String name) {
        if (req.getContentType() != null && req.getContentType().toLowerCase().indexOf("multipart/form-data") > -1) {
            try {
                Part part = req.getPart(name);

                if (part == null)
                    return null;

                InputStream is = part.getInputStream();
                try (java.util.Scanner s = new java.util.Scanner(is)) {
                    return s.useDelimiter("\\A").hasNext() ? s.next() : "";
                } finally {
                    is.close();
                }
            } catch (IOException | ServletException e) {
                return null;
            }
        }

        return req.getParameter(name);
    }
}

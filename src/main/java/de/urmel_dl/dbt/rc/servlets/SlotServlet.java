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

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.XMLOutputter;
import org.mycore.access.MCRAccessManager;
import org.mycore.common.content.MCRContent;
import org.mycore.common.events.MCREvent;
import org.mycore.common.events.MCREventManager;
import org.mycore.frontend.MCRFrontendUtil;
import org.mycore.frontend.servlets.MCRServlet;
import org.mycore.frontend.servlets.MCRServletJob;

import de.urmel_dl.dbt.opc.datamodel.Catalog;
import de.urmel_dl.dbt.opc.datamodel.Catalogues;
import de.urmel_dl.dbt.rc.datamodel.slot.Slot;
import de.urmel_dl.dbt.rc.datamodel.slot.SlotEntry;
import de.urmel_dl.dbt.rc.datamodel.slot.entries.FileEntry;
import de.urmel_dl.dbt.rc.datamodel.slot.entries.FileEntry.FileEntryProcessingException;
import de.urmel_dl.dbt.rc.datamodel.slot.entries.OPCRecordEntry;
import de.urmel_dl.dbt.rc.persistency.FileEntryManager;
import de.urmel_dl.dbt.rc.persistency.SlotManager;
import de.urmel_dl.dbt.utils.EntityFactory;

/**
 * @author Ren\u00E9 Adler (eagle)
 *
 */
@MultipartConfig
public class SlotServlet extends MCRServlet {

    private static final long serialVersionUID = -3138681111200495882L;

    private static final Logger LOGGER = LogManager.getLogger(SlotServlet.class);

    private static final SlotManager SLOT_MGR = SlotManager.instance();

    @Override
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
                    && !MCRAccessManager.checkPermission(slot.getMCRObjectID(),
                        MCRAccessManager.PERMISSION_WRITE)) {
                    res.sendError(HttpServletResponse.SC_FORBIDDEN);
                    return;
                }

                final SlotEntry<FileEntry> slotEntry = (SlotEntry<FileEntry>) slot.getEntryById(entryId);

                if (slotEntry != null) {
                    final FileEntry fileEntry = slotEntry.getEntry();
                    if (fileEntry != null && fileName.equals(fileEntry.getName())) {
                        FileEntryManager.retrieve(slot, slotEntry);
                        MCRContent content = fileEntry.getContent();
                        content.sendTo(res.getOutputStream());
                        return;
                    }
                }
            }

            res.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // edit slot entries
        final String action = getParameter(req, "action");
        final String entry = getParameter(req, "entry");
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

                final Map<String, String> params = new HashMap<>();
                params.put("slotId", slotId);
                params.put("afterId", afterId);

                res.sendRedirect(MCRFrontendUtil.getBaseURL() + "opc/"
                    + (catalog != null && catalog.getISIL() != null && catalog.getISIL().size() > 0
                        ? catalog.getISIL().get(0) : catalogId)
                    + "/search/" + URLEncoder.encode(firstChild.getTextTrim(), "UTF-8")
                    + toQueryString(params, true));
            } else {
                SlotEntry<?> slotEntry = xml != null ? new EntityFactory<>(SlotEntry.class).fromElement(xml) : null;

                boolean success = true;

                if (slotEntry == null && "upload".equals(action)) {
                    if (getParameter(req, "cancel") != null) {
                        res.sendRedirect(MCRFrontendUtil.getBaseURL() + "rc/" + slot.getSlotId() + "?XSL.Mode=edit");
                        return;
                    }

                    final Map<String, String> params = new HashMap<>();
                    params.put("entry", entry);
                    params.put("slotId", slotId);
                    params.put("afterId", afterId);
                    params.put("invalid", "true");

                    final Part filePart = req.getPart("file");

                    slotEntry = new SlotEntry<FileEntry>();
                    try {
                        final FileEntry fe = FileEntry.createFileEntry(slotEntry.getId(), getFilename(filePart),
                            getParameter(req, "comment"), Boolean.parseBoolean(getParameter(req, "copyrighted")),
                            filePart.getInputStream());
                        ((SlotEntry<FileEntry>) slotEntry).setEntry(fe);
                    } catch (FileEntryProcessingException pe) {
                        params.put("errorcode", Integer.toString(pe.getErrorCode()));

                        res.sendRedirect(MCRFrontendUtil.getBaseURL() + "content/rc/entry-file.xml"
                            + toQueryString(params, false));
                        return;
                    }
                }

                MCREvent evt = null;

                if ("order".equals(action)) {
                    final String items = getParameter(req, "items");
                    final StringTokenizer st = new StringTokenizer(items, ",");

                    final List<SlotEntry<?>> sortedEntries = new ArrayList<>();
                    while (st.hasMoreTokens()) {
                        final String id = st.nextToken();
                        sortedEntries.add(slot.getEntryById(id));
                    }
                    slot.setEntries(sortedEntries);

                    SLOT_MGR.saveOrUpdate(slot);

                    res.sendError(HttpServletResponse.SC_OK);
                    return;
                } else if ("delete".equals(action)) {
                    final SlotEntry<?> se = slot.getEntryById(slotEntry.getId());
                    if (se != null) {
                        if (se.getEntry() instanceof OPCRecordEntry
                            && !MCRAccessManager.checkPermission(SlotManager.POOLPRIVILEGE_ADMINISTRATE_SLOT)
                            && ((OPCRecordEntry) se.getEntry()).getEPN() != null) {
                            LOGGER.debug("Set deletion mark: " + se);
                            ((OPCRecordEntry) se.getEntry()).setDeletionMark(true);
                            slot.setEntry(se);
                        } else {
                            LOGGER.debug("Remove entry: " + se);
                            success = slot.removeEntry(se);
                        }

                        evt = new MCREvent(SlotManager.ENTRY_TYPE, MCREvent.DELETE_EVENT);
                        evt.put(SlotManager.ENTRY_TYPE, se);
                    }
                } else if (slot.getEntries() == null) {
                    LOGGER.debug("Add new entry: " + slotEntry);
                    success = slot.addEntry(slotEntry);

                    evt = new MCREvent(SlotManager.ENTRY_TYPE, MCREvent.CREATE_EVENT);
                    evt.put(SlotManager.ENTRY_TYPE, slotEntry);
                } else {
                    final SlotEntry<?> se = slot.getEntryById(slotEntry.getId());
                    if (se != null) {
                        LOGGER.debug("Update entry: " + slotEntry);
                        slot.setEntry(slotEntry);

                        evt = new MCREvent(SlotManager.ENTRY_TYPE, MCREvent.UPDATE_EVENT);
                        evt.put(SlotManager.ENTRY_TYPE, slotEntry);
                    } else {
                        LOGGER.debug("Add new entry after \"" + afterId + "\".");
                        success = slot.addEntry(slotEntry, afterId);

                        evt = new MCREvent(SlotManager.ENTRY_TYPE, MCREvent.CREATE_EVENT);
                        evt.put(SlotManager.ENTRY_TYPE, slotEntry);
                    }
                }

                if (success) {
                    // put svn revision on event (needed on deletion)
                    if (evt != null) {
                        evt.put("revision", Long.toString(SLOT_MGR.getLastRevision(slot)));
                    }

                    SLOT_MGR.saveOrUpdate(slot);

                    if (evt != null) {
                        if (evt.getObjectType().equals(SlotManager.ENTRY_TYPE)) {
                            evt.put("slotId", slot.getSlotId());
                        }

                        if (MCREvent.DELETE_EVENT.equals(evt.getEventType())) {
                            MCREventManager.instance().handleEvent(evt, MCREventManager.BACKWARD);
                        } else {
                            MCREventManager.instance().handleEvent(evt);
                        }
                    }
                }

                res.sendRedirect(MCRFrontendUtil.getBaseURL() + "rc/" + slot.getSlotId() + "?XSL.Mode=edit#"
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
        return queryStr.length() > 0 ? "?" + queryStr.toString() : queryStr.toString();
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
        if (req.getContentType() != null
            && req.getContentType().toLowerCase(Locale.ROOT).indexOf("multipart/form-data") > -1) {
            try {
                Part part = req.getPart(name);

                if (part == null) {
                    return null;
                }

                InputStream is = part.getInputStream();
                try (java.util.Scanner s = new java.util.Scanner(is, StandardCharsets.UTF_8.name())) {
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

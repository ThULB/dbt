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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.apache.log4j.Logger;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdfwriter.COSWriter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.pdmodel.encryption.BadSecurityHandlerException;
import org.apache.pdfbox.pdmodel.encryption.StandardProtectionPolicy;
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

    private static final int ERROR_EMPTY_FILE = 1000;

    private static final int ERROR_PAGE_LIMIT_EXCEEDED = 1001;

    private static final int ERROR_NOT_SUPPORTED = 1002;

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

                if (!SlotManager.checkPermission(slot.getMCRObjectID(), MCRAccessManager.PERMISSION_READ)
                        || !SlotManager.checkPermission(slot.getMCRObjectID(), MCRAccessManager.PERMISSION_WRITE)) {
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

            if (!SlotManager.checkPermission(slot.getMCRObjectID(), MCRAccessManager.PERMISSION_WRITE)) {
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

                    final Map<String, String> params = new HashMap<String, String>();
                    params.put("entry", entry);
                    params.put("slotId", slotId);
                    params.put("afterId", afterId);
                    params.put("invalid", "true");

                    final Part filePart = req.getPart("file");
                    final String fileName = getFilename(filePart);
                    final boolean isCopyrighted = Boolean.parseBoolean(getParameter(req, "copyrighted"));

                    if (fileName == null || fileName.length() == 0) {
                        params.put("errorcode", Integer.toString(ERROR_EMPTY_FILE));

                        job.getResponse().sendRedirect(
                                MCRFrontendUtil.getBaseURL() + "content/rc/entry-file.xml"
                                        + toQueryString(params, false));
                        return;
                    }

                    slotEntry = new SlotEntry<FileEntry>();

                    final FileEntry fe = new FileEntry();
                    fe.setName(fileName);
                    fe.setCopyrighted(isCopyrighted);

                    if (isCopyrighted && "application/pdf".equals(filePart.getContentType())) {
                        ByteArrayOutputStream pdfCopy = null;
                        ByteArrayOutputStream pdfEncrypted = null;

                        try {
                            final int numPages = getNumPagesFromPDF(filePart.getInputStream());

                            LOGGER.info("Check num pages for \"" + fileName + "\": " + numPages);
                            if (numPages == -1 || numPages > 50) {
                                params.put("errorcode", Integer.toString(ERROR_PAGE_LIMIT_EXCEEDED));

                                job.getResponse().sendRedirect(
                                        MCRFrontendUtil.getBaseURL() + "content/rc/entry-file.xml"
                                                + toQueryString(params, false));
                                return;
                            }

                            LOGGER.info("Make an supported copy for \"" + fileName + "\".");
                            pdfCopy = new ByteArrayOutputStream();
                            copyPDF(filePart.getInputStream(), pdfCopy);

                            LOGGER.info("Encrypt \"" + fileName + "\".");
                            pdfEncrypted = new ByteArrayOutputStream();
                            encryptPDF(slotEntry.getId(), new ByteArrayInputStream(pdfCopy.toByteArray()), pdfEncrypted);

                            fe.setContent(pdfEncrypted.toByteArray());
                        } catch (Exception e) {
                            params.put("errorcode", Integer.toString(ERROR_NOT_SUPPORTED));

                            job.getResponse().sendRedirect(
                                    MCRFrontendUtil.getBaseURL() + "content/rc/entry-file.xml"
                                            + toQueryString(params, false));
                            return;
                        } finally {
                            if (pdfCopy != null) {
                                pdfCopy.close();
                            }
                            if (pdfEncrypted != null) {
                                pdfEncrypted.close();
                            }
                        }
                    } else
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

    private static int getNumPagesFromPDF(InputStream fileStream) throws IOException {
        PDDocument doc = PDDocument.load(fileStream);
        return doc.getNumberOfPages();
    }

    private static void copyPDF(InputStream pdfInput, OutputStream pdfOutput) throws IOException, COSVisitorException {
        COSWriter writer = null;
        try {
            PDFParser parser = new PDFParser(pdfInput);
            parser.parse();

            COSDocument doc = parser.getDocument();

            writer = new COSWriter(pdfOutput);

            writer.write(doc);
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    private static void encryptPDF(String entryID, InputStream pdfInput, OutputStream pdfOutput) throws IOException,
            BadSecurityHandlerException, COSVisitorException {
        PDDocument doc = PDDocument.load(pdfInput);

        AccessPermission ap = new AccessPermission();

        ap.setCanAssembleDocument(false);
        ap.setCanExtractContent(false);
        ap.setCanExtractForAccessibility(false);
        ap.setCanFillInForm(false);
        ap.setCanModify(false);
        ap.setCanModifyAnnotations(false);
        ap.setCanPrint(false);
        ap.setCanPrintDegraded(false);
        ap.setReadOnly();

        if (!doc.isEncrypted()) {
            StandardProtectionPolicy spp = new StandardProtectionPolicy(entryID, null, ap);
            doc.protect(spp);

            doc.save(pdfOutput);
        }
    }
}

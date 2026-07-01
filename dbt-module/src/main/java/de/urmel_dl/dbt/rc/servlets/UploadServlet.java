/*
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
package de.urmel_dl.dbt.rc.servlets;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.concurrent.ThreadPoolExecutor;

import jakarta.servlet.AsyncContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mycore.access.MCRAccessManager;
import org.mycore.common.MCRSessionMgr;
import org.mycore.common.events.MCREvent;
import org.mycore.common.events.MCREventManager;
import org.mycore.frontend.MCRFrontendUtil;
import org.mycore.frontend.servlets.MCRServlet;
import org.mycore.frontend.servlets.MCRServletJob;
import org.mycore.util.concurrent.MCRTransactionableRunnable;

import de.urmel_dl.dbt.rc.datamodel.UploadResponse;
import de.urmel_dl.dbt.rc.datamodel.slot.Slot;
import de.urmel_dl.dbt.rc.datamodel.slot.SlotEntry;
import de.urmel_dl.dbt.rc.datamodel.slot.entries.FileEntry;
import de.urmel_dl.dbt.rc.datamodel.slot.entries.FileEntry.FileEntryProcessingException;
import de.urmel_dl.dbt.rc.persistency.SlotManager;
import de.urmel_dl.dbt.rc.servlets.listener.UploadContextListener;
import de.urmel_dl.dbt.utils.EntityFactory;

/**
 * @author René Adler (eagle)
 *
 */
public class UploadServlet extends MCRServlet {

    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = LogManager.getLogger();

    private static final SlotManager SLOT_MGR = SlotManager.instance();

    @Override
    protected void doPost(MCRServletJob job) throws Exception {
        final HttpServletRequest req = job.getRequest();
        final HttpServletResponse res = job.getResponse();

        final String slotId = req.getParameter("slotId");

        if (slotId != null) {
            final Slot slot = SLOT_MGR.getSlotById(slotId);

            if (!MCRAccessManager.checkPermission(slot.getMCRObjectID(), MCRAccessManager.PERMISSION_WRITE)) {
                res.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }

            if (req.getParameter("cancel") != null) {
                res.sendRedirect(MCRFrontendUtil.getBaseURL() + "rc/" + slot.getSlotId() + "?XSL.Mode=edit");
                return;
            }

            Part filePart = req.getPart("file");

            AsyncContext asyncCtx = req.startAsync();
            asyncCtx.setTimeout(600000);

            ThreadPoolExecutor executor = (ThreadPoolExecutor) req.getServletContext()
                .getAttribute(UploadContextListener.ATTR_EXECUTER);
            executor
                .execute(new MCRTransactionableRunnable(
                    new UploadProcessor(asyncCtx, slot, filePart.getInputStream(), filePart.getSubmittedFileName()),
                    MCRSessionMgr.getCurrentSession()));
        }
    }

    public static class UploadProcessor implements Runnable {

        private AsyncContext asyncContext;

        private Slot slot;

        private InputStream fileStream;

        private String fileName;

        public UploadProcessor() {
        }

        public UploadProcessor(AsyncContext asyncCtx, Slot slot, InputStream fileStream, String fileName) {
            this.asyncContext = asyncCtx;
            this.slot = slot;
            this.fileStream = fileStream;
            this.fileName = fileName;
        }

        @Override
        public void run() {
            HttpServletResponse res = (HttpServletResponse) asyncContext.getResponse();
            HttpServletRequest req = (HttpServletRequest) asyncContext.getRequest();

            boolean success = true;

            final String slotId = req.getParameter("slotId");
            final String afterId = req.getParameter("afterId");

            LOGGER.info("Process uploaded {} for slot {}...", fileName, slotId);

            try {
                SlotEntry<FileEntry> slotEntry = new SlotEntry<FileEntry>();
                try {
                    final FileEntry fe = FileEntry.createFileEntry(slotEntry.getId(),
                        fileName,
                        req.getParameter("comment"),
                        Boolean.parseBoolean(req.getParameter("copyrighted")),
                        fileStream);
                    ((SlotEntry<FileEntry>) slotEntry).setEntry(fe);
                } catch (FileEntryProcessingException pe) {
                    uploadResponse(res, HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE,
                        new UploadResponse(pe.getErrorCode(), pe.getMessage(), null));

                    asyncContext.complete();
                    return;
                }

                MCREvent evt = null;

                if (slot.getEntries() == null) {
                    LOGGER.debug("Add new entry: " + slotEntry);
                    success = slot.addEntry(slotEntry);

                    evt = MCREvent.customEvent(SlotManager.ENTRY_TYPE, MCREvent.EventType.CREATE);
                    evt.put(SlotManager.ENTRY_TYPE, slotEntry);
                } else {
                    final SlotEntry<?> se = slot.getEntryById(slotEntry.getId());
                    if (se != null) {
                        LOGGER.debug("Update entry: " + slotEntry);
                        slot.setEntry(slotEntry);

                        evt = MCREvent.customEvent(SlotManager.ENTRY_TYPE, MCREvent.EventType.UPDATE);
                        evt.put(SlotManager.ENTRY_TYPE, slotEntry);
                    } else {
                        LOGGER.debug("Add new entry after \"" + afterId + "\".");
                        success = slot.addEntry(slotEntry, afterId);

                        evt = MCREvent.customEvent(SlotManager.ENTRY_TYPE, MCREvent.EventType.CREATE);
                        evt.put(SlotManager.ENTRY_TYPE, slotEntry);
                    }
                }

                if (success) {
                    SLOT_MGR.saveOrUpdate(slot);

                    if (evt != null) {
                        evt.put("slotId", slot.getSlotId());
                        MCREventManager.getInstance().handleEvent(evt);
                    }
                }

                uploadResponse(res, HttpServletResponse.SC_OK,
                    new UploadResponse(200, null, MCRFrontendUtil.getBaseURL() + "rc/"
                        + slot.getSlotId() + "?XSL.Mode=edit#" + slotEntry.getId()));
            } catch (Exception ex) {
                LOGGER.error(ex.getMessage(), ex);
                uploadResponse(res, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    new UploadResponse(null, ex.getMessage(), null));
            }

            //complete the processing
            asyncContext.complete();
        }

        private void uploadResponse(HttpServletResponse res, int status, UploadResponse response) {
            res.setStatus(status);
            res.setContentType("application/json");
            try {
                res.getWriter().print(new EntityFactory<>(response).toJSON());
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }

        }

    }

}

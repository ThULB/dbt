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
package de.urmel_dl.dbt.rc.events;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;

import org.mycore.common.events.MCREvent;

import de.urmel_dl.dbt.media.MediaService;
import de.urmel_dl.dbt.rc.datamodel.slot.SlotEntry;
import de.urmel_dl.dbt.rc.datamodel.slot.entries.FileEntry;
import de.urmel_dl.dbt.rc.persistency.FileEntryManager;

/**
 * @author Ren\u00E9 Adler (eagle)
 *
 */
public class MediaEventHandler extends EventHandlerBase {

    @SuppressWarnings("unchecked")
    @Override
    protected void handleEntryCreated(MCREvent evt, SlotEntry<?> entry) {
        if (entry.getEntry() instanceof FileEntry) {
            encodeMediaFile((SlotEntry<FileEntry>) entry);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void handleEntryDeleted(MCREvent evt, SlotEntry<?> entry) {
        if (entry.getEntry() instanceof FileEntry) {
            deleteMediaFile((SlotEntry<FileEntry>) entry);
        }
    }

    private String buildId(SlotEntry<FileEntry> entry) {
        return entry.getSlot().getSlotId() + "_" + entry.getId() + "_" + entry.getEntry().getName();
    }

    private void encodeMediaFile(SlotEntry<FileEntry> entry) {
        try {
            Path mediaFile = FileEntryManager.getLocalPath(entry.getSlot(), entry);

            if (mediaFile != null && MediaService.isMediaSupported(mediaFile)) {
                MediaService.encodeMediaFile(buildId(entry), mediaFile, 0);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void deleteMediaFile(SlotEntry<FileEntry> entry) {
        try {
            String id = MediaService.buildInternalId(buildId(entry));
            if (MediaService.hasMediaFiles(id)) {
                MediaService.deleteMediaFiles(id);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

}

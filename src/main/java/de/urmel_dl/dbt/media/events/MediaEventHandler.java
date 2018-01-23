/*
 * This file is part of the Digitale Bibliothek Thüringen
 * Copyright (C) 2000-2017
 * See <https://www.db-thueringen.de/> and <https://github.com/ThULB/dbt/>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.urmel_dl.dbt.media.events;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

import org.mycore.common.MCRSessionMgr;
import org.mycore.common.events.MCREvent;
import org.mycore.common.events.MCREventHandlerBase;
import org.mycore.datamodel.niofs.MCRPath;

import de.urmel_dl.dbt.media.MediaService;

/**
 * @author Ren\u00E9 Adler (eagle)
 *
 */
public class MediaEventHandler extends MCREventHandlerBase {

    /* (non-Javadoc)
     * @see org.mycore.common.events.MCREventHandlerBase#handlePathUpdated(org.mycore.common.events.MCREvent, java.nio.file.Path, java.nio.file.attribute.BasicFileAttributes)
     */
    @Override
    protected void handlePathUpdated(MCREvent evt, Path path, BasicFileAttributes attrs) {
        if (!(path instanceof MCRPath)) {
            return;
        }

        handlePathDeleted(evt, path, attrs);
        MCRSessionMgr.getCurrentSession().onCommit(() -> encodeMediaFile(MCRPath.toMCRPath(path), 0));
    }

    /* (non-Javadoc)
     * @see org.mycore.common.events.MCREventHandlerBase#handlePathDeleted(org.mycore.common.events.MCREvent, java.nio.file.Path, java.nio.file.attribute.BasicFileAttributes)
     */
    @Override
    protected void handlePathDeleted(MCREvent evt, Path path, BasicFileAttributes attrs) {
        if (!(path instanceof MCRPath)) {
            return;
        }

        if (attrs.isDirectory()) {
            return;
        }

        MCRSessionMgr.getCurrentSession().onCommit(() -> deleteMediaFile(MCRPath.toMCRPath(path)));
    }

    /* (non-Javadoc)
     * @see org.mycore.common.events.MCREventHandlerBase#handlePathCreated(org.mycore.common.events.MCREvent, java.nio.file.Path, java.nio.file.attribute.BasicFileAttributes)
     */
    @Override
    protected void handlePathCreated(MCREvent evt, Path path, BasicFileAttributes attrs) {
        if (!(path instanceof MCRPath)) {
            return;
        }
        MCRSessionMgr.getCurrentSession().onCommit(() -> encodeMediaFile(MCRPath.toMCRPath(path), 10));
    }

    private void deleteMediaFile(MCRPath path) {
        try {
            String id = MediaService
                .buildInternalId(MCRPath.toMCRPath(path).getOwner() + "_" + path.getFileName().toString());
            if (MediaService.hasMediaFiles(id)) {
                MediaService.deleteMediaFiles(id);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void encodeMediaFile(MCRPath path, int priority) {
        if (MediaService.isMediaSupported(path)) {
            MediaService.encodeMediaFile(path.getOwner() + "_" + path.getFileName().toString(), path, priority);
        }
    }
}

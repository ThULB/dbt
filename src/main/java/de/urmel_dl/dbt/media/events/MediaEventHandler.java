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
import java.util.Optional;

import org.mycore.common.MCRSessionMgr;
import org.mycore.common.events.MCREvent;
import org.mycore.common.events.MCREventHandlerBase;
import org.mycore.datamodel.niofs.MCRPath;

import de.urmel_dl.dbt.media.MediaService;

/**
 * @author René Adler (eagle)
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
        MCRSessionMgr.getCurrentSession().onCommit(() -> handleFile(MCRPath.ofPath(path), 0));
    }

    /* (non-Javadoc)
     * @see org.mycore.common.events.MCREventHandlerBase#handlePathDeleted(org.mycore.common.events.MCREvent, java.nio.file.Path, java.nio.file.attribute.BasicFileAttributes)
     */
    @Override
    protected void handlePathDeleted(MCREvent evt, Path path, BasicFileAttributes attrs) {
        if (!(path instanceof MCRPath)) {
            return;
        }

        MCRSessionMgr.getCurrentSession().onCommit(() -> deleteFile(MCRPath.ofPath(path)));
    }

    /* (non-Javadoc)
     * @see org.mycore.common.events.MCREventHandlerBase#handlePathCreated(org.mycore.common.events.MCREvent, java.nio.file.Path, java.nio.file.attribute.BasicFileAttributes)
     */
    @Override
    protected void handlePathCreated(MCREvent evt, Path path, BasicFileAttributes attrs) {
        if (!(path instanceof MCRPath)) {
            return;
        }
        MCRSessionMgr.getCurrentSession().onCommit(() -> handleFile(MCRPath.ofPath(path), 10));
    }

    private void deleteFile(MCRPath path) {
        try {
            if (MediaService.isSubtitleSupported(path)) {
                Optional<Path> mediaFile = MediaService.findMediaFile(path);

                if (mediaFile.isPresent()) {
                    MediaService.deleteImportedSubtitleFile(internalMediaId(path, mediaFile.get()));
                }

                return;
            }

            String id = internalMediaId(path, path);

            // an imported subtitle can exist even if the media file was never encoded
            MediaService.deleteImportedSubtitleFile(id);

            if (MediaService.hasMediaFiles(id)) {
                MediaService.deleteMediaFiles(id);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void handleFile(MCRPath path, int priority) {
        try {
            if (MediaService.isMediaSupported(path)) {

                MediaService.encodeMediaFile( mediaId(path, path), path, priority);

                // the subtitle may have been uploaded before the media file
                Optional<Path> subtitleFile = MediaService.findSubtitleFile(path);

                if (subtitleFile.isPresent()) {
                    MediaService.importSubtitleFile(internalMediaId(path, path), subtitleFile.get());
                }
            } else if (MediaService.isSubtitleSupported(path)) {
                Optional<Path> mediaFile = MediaService.findMediaFile(path);

                if (mediaFile.isPresent()) {
                    MediaService.importSubtitleFile(internalMediaId(path, mediaFile.get()), path);
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Returns the media id of given media file, e.g. <code>dbt_derivate_00000001_video.mp4</code>.
     * It's used by the encoder service as job id. The derivate is taken from <code>path</code>, so
     * a sibling file can be passed too.
     */
    private static String mediaId(MCRPath path, Path mediaFile) {
        return path.getOwner() + "_" + mediaFile.getFileName().toString();
    }

    /**
     * Returns the internal media id of given media file, that is used to address the media, thumb
     * and subtitle store.
     */
    private static String internalMediaId(MCRPath path, Path mediaFile) {
        return MediaService.buildInternalId(mediaId(path, mediaFile));
    }

}
